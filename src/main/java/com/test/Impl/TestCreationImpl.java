package com.test.Impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.test.api.ITestCreation;
import com.test.dto.QuestionDto;
import com.test.dto.TestCreationRequest;
import com.test.dto.TestCreationRequest.QuestionDetail;
import com.test.enums.QuestionType;
import com.test.enums.TestLevelType;

@Service
public class TestCreationImpl implements ITestCreation {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestCreationImpl.class);

	@Value("${server.questionpaper.creation.filepath}")
	private String questionPprFilePath;

	@Value("${server.answerkeys.creation.filepath}")
	private String answerkeyFilePath;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void createTestpaper() {
		TestCreationRequest testCreationRequest = new TestCreationRequest();
		testCreationRequest.getQuestionDetails()
				.add(new QuestionDetail(QuestionType.objective, 10, TestLevelType.medium, 2));
		testCreationRequest.getQuestionDetails().add(new QuestionDetail(QuestionType.sql, 1, TestLevelType.medium, 5));
		testCreationRequest.getQuestionDetails()
				.add(new QuestionDetail(QuestionType.coding, 2, TestLevelType.medium, 10));
		testCreationRequest.getQuestionDetails()
				.add(new QuestionDetail(QuestionType.puzzles, 1, TestLevelType.medium, 5));
		testCreationRequest.getQuestionDetails()
				.add(new QuestionDetail(QuestionType.design, 1, TestLevelType.medium, 10));

		createTest(testCreationRequest);
	}

	public List<QuestionDto> getAllQuestions() {
		List<QuestionDto> questions = new ArrayList<QuestionDto>();

		questions.addAll(getQuestionFromFolder(applicationContext.getResource("classpath:easy/"), TestLevelType.easy));

		// Get medium questions
		questions.addAll(
				getQuestionFromFolder(applicationContext.getResource("classpath:medium/"), TestLevelType.medium));

		// Get tough questions
		questions
				.addAll(getQuestionFromFolder(applicationContext.getResource("classpath:tough/"), TestLevelType.tough));
		return questions;
	}

	public List<QuestionDto> getQuestionFromFolder(Resource resource, TestLevelType testLevelType) {
		List<QuestionDto> questions = new ArrayList<QuestionDto>();
		try {
			for (final File fileEntry : resource.getFile().listFiles()) {
				if (!fileEntry.isDirectory()) {
					BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileEntry), "UTF-8"));
						while (true) {
							String line = reader.readLine();
							if (line == null)
								break;
							StringBuilder sb = new StringBuilder();
							while (!line.trim().startsWith("========")) {
								if (line.trim().length() > 0)
									sb.append(line + "\n");
								line = reader.readLine();
							}

							if (sb.toString().trim().length() > 0) {
								QuestionDto questionDto = new QuestionDto();
								questionDto.setQuestion(sb.toString());
								questionDto.setTestLevelType(testLevelType);
								QuestionType questionType = getQuestionType(fileEntry.getName());
								if (questionType != null) {
									questionDto.setQuestionType(questionType);
								} else {
									LOGGER.info("File name is not correct.");
								}
								if (!questions.contains(questionDto))
									questions.add(questionDto);
							}
						}
					} catch (Exception e) {
						LOGGER.error("Exception occured while reading file = {} ", e);
					} finally {
						try {
							reader.close();
						} catch (Exception e2) {
							LOGGER.error("Exception occured while closing file = {} ", e2);
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured while listing files = {} ", e);
		}
		return questions;
	}

	private QuestionType getQuestionType(String fileName) {
		for (QuestionType questionType : QuestionType.values()) {
			if (fileName.indexOf(questionType.name().toLowerCase()) != -1) {
				return questionType;
			}
		}
		return null;
	}

	Map<TestLevelType, Map<QuestionType, List<QuestionDto>>> constructMap(List<QuestionDto> questionDtos) {
		Map<TestLevelType, Map<QuestionType, List<QuestionDto>>> questionMap = new HashMap<TestLevelType, Map<QuestionType, List<QuestionDto>>>();

		for (TestLevelType testLevelType : TestLevelType.values()) {
			questionMap.put(testLevelType, new HashMap<QuestionType, List<QuestionDto>>());
			for (QuestionType questionType : QuestionType.values()) {
				questionMap.get(testLevelType).put(questionType, new ArrayList<QuestionDto>());
			}
		}

		for (QuestionDto questionDto : questionDtos) {
			if (questionMap.get(questionDto.getTestLevelType()) == null) {
				questionMap.put(questionDto.getTestLevelType(), new HashMap<QuestionType, List<QuestionDto>>());
			}
			if (questionMap.get(questionDto.getTestLevelType()).get(questionDto.getQuestionType()) == null) {
				questionMap.get(questionDto.getTestLevelType()).put(questionDto.getQuestionType(),
						new ArrayList<QuestionDto>());
			}
			questionMap.get(questionDto.getTestLevelType()).get(questionDto.getQuestionType()).add(questionDto);
		}
		return questionMap;
	}

	public List<QuestionDto> finalQuestions(TestCreationRequest testCreationRequest) {
		List<QuestionDto> questions = new ArrayList<QuestionDto>();
		List<QuestionDto> questionDtos = getAllQuestions();
		Map<TestLevelType, Map<QuestionType, List<QuestionDto>>> questionMap = constructMap(questionDtos);
		if (testCreationRequest.getQuestionDetails() != null) {
			for (QuestionDetail questionDetail : testCreationRequest.getQuestionDetails()) {
				List<QuestionDto> availableQuestions = questionMap.get(questionDetail.getTestLevelType())
						.get(questionDetail.getQuestionType());
				questions.addAll(availableQuestions.subList(0, (questionDetail.getCount() > availableQuestions.size()
						? availableQuestions.size() : questionDetail.getCount())));
			}
		}
		return questions;
	}

	public String createTest(TestCreationRequest testCreationRequest) {
		LOGGER.info("Request received for test creation = {} ", testCreationRequest);
		XWPFDocument document = null;
		FileOutputStream out = null;
		try {
			// get and construct Map
			List<QuestionDto> questionDtos = finalQuestions(testCreationRequest);

			document = new XWPFDocument();
			// Write the Document in file system
			out = new FileOutputStream(
					new File(questionPprFilePath + "question" + System.currentTimeMillis() + ".docx"));

			Map<QuestionType, Integer> marks = new HashMap<QuestionType, Integer>();
			if (testCreationRequest.getQuestionDetails() != null) {
				for (QuestionDetail questionDetail : testCreationRequest.getQuestionDetails()) {
					marks.put(questionDetail.getQuestionType(), questionDetail.getMarks());
				}
			}

			List<String> headers = new ArrayList<String>();
			headers.add("This paper consists " + questionDtos.size() + " questions.");
			Map<QuestionType, Integer> typeCount = new HashMap<QuestionType, Integer>();
			for (QuestionDto questionDto : questionDtos) {
				if (typeCount.get(questionDto.getQuestionType()) == null) {
					typeCount.put(questionDto.getQuestionType(), 1);
				} else {
					typeCount.put(questionDto.getQuestionType(), typeCount.get(questionDto.getQuestionType()) + 1);
				}
			}

			for (java.util.Map.Entry<QuestionType, Integer> entry : typeCount.entrySet()) {
				headers.add(entry.getValue() + " " + entry.getKey().name() + " questions " + marks.get(entry.getKey())
						+ " marks each.");
			}

			for (String header : headers) {
				// Headers
				XWPFParagraph paragraph = document.createParagraph();
				XWPFRun run = paragraph.createRun();
				run.setText(header);
				run.setBold(true);
				run.setCapitalized(true);
				run.addBreak();
			}

			for (int j = 0; j < questionDtos.size(); j++) {
				QuestionDto questionDto = questionDtos.get(j);
				String splits[] = questionDto.getQuestion().split("\n");
				for (int i = 0; i < splits.length; i++) {
					String value = splits[i];
					XWPFParagraph paragraph = document.createParagraph();
					XWPFRun run = paragraph.createRun();
					if (i == 0) {
						value = (j + 1) + ". " + value;
						run.setBold(true);
					}
					int spaceIndex = 0;
					while (value.charAt(spaceIndex) == ' ') {
						spaceIndex++;
					}
					run.setText(value, 0);
					if (i == splits.length - 1) {
						run.addBreak();
					}
				}
			}
			document.write(out);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("Exception occured while closing file = {} ", e);
		} finally {
			try {
				out.close();
			} catch (Exception e2) {
				LOGGER.error("Exception occured while closing file = {} ", e2);
			}
			try {
				document.close();
			} catch (Exception e2) {
				LOGGER.error("Exception occured while closing word document = {} ", e2);
			}
		}
		LOGGER.info("createdocument.docx written successully");
		return null;
	}

}
