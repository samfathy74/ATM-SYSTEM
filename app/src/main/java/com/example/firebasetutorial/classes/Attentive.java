package com.example.firebasetutorial.classes;

public class Attentive {
    String Answer, DateTime, Question, Status, UserAnswer, Email;

    public Attentive() {
    }

    public Attentive(String answer, String dateTime, String question, String status, String userAnswer, String email) {
        Answer = answer;
        DateTime = dateTime;
        Question = question;
        Status = status;
        UserAnswer = userAnswer;
        Email = email;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUserAnswer() {
        return UserAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        UserAnswer = userAnswer;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
