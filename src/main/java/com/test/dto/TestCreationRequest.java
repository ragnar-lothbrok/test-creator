package com.test.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.test.enums.QuestionType;
import com.test.enums.TestLevelType;

public class TestCreationRequest {

	private List<QuestionDetail> questionDetails = new ArrayList<QuestionDetail>();
	private String[] headers = { "This paper consists {0} questions.", "{0} {1} questions of {2} marks." };

	public String[] getHeaders() {
		return headers;
	}

	public void setHeaders(String[] headers) {
		this.headers = headers;
	}

	public List<QuestionDetail> getQuestionDetails() {
		return questionDetails;
	}

	public void setQuestionDetails(List<QuestionDetail> questionDetails) {
		this.questionDetails = questionDetails;
	}

	@Override
	public String toString() {
		return "TestCreationRequest [questionDetails=" + questionDetails + "]";
	}

	public static class QuestionDetail implements Serializable {
		private static final long serialVersionUID = -2484464298956296374L;

		private QuestionType questionType;
		private Integer count;
		private TestLevelType testLevelType;
		private Integer marks;

		public Integer getMarks() {
			return marks;
		}

		public void setMarks(Integer marks) {
			this.marks = marks;
		}

		public TestLevelType getTestLevelType() {
			return testLevelType;
		}

		public void setTestLevelType(TestLevelType testLevelType) {
			this.testLevelType = testLevelType;
		}

		public QuestionType getQuestionType() {
			return questionType;
		}

		public void setQuestionType(QuestionType questionType) {
			this.questionType = questionType;
		}

		public Integer getCount() {
			return count;
		}

		public void setCount(Integer count) {
			this.count = count;
		}

		public QuestionDetail() {
			super();
		}

		public QuestionDetail(QuestionType questionType, Integer count, TestLevelType testLevelType, Integer marks) {
			super();
			this.questionType = questionType;
			this.count = count;
			this.testLevelType = testLevelType;
			this.marks = marks;
		}

		@Override
		public String toString() {
			return "QuestionType [questionType=" + questionType + ", count=" + count + "]";
		}

	}
}
