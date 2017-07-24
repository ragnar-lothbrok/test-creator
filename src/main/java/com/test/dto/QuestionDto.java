package com.test.dto;

import java.io.Serializable;

import com.test.enums.QuestionType;
import com.test.enums.TestLevelType;

public class QuestionDto implements Serializable {

	private static final long serialVersionUID = -8389291853243614337L;

	private String question;
	private String answer;
	private TestLevelType testLevelType;
	private QuestionType questionType;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuestionDto other = (QuestionDto) obj;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
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

}
