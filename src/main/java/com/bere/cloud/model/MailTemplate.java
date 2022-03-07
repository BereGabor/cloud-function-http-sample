package com.bere.cloud.model;

public class MailTemplate {
	
	private String from;
	private String subject;
	private String body;
	
	public MailTemplate(String from, String subject, String body) {
		setBody(body);
		setFrom(from);
		setSubject(subject);
	}
	
	public MailTemplate() {
		
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" from: " + getFrom());
		sb.append(" subject: " + getSubject());
		sb.append(" bodyTemplate: " + getBody());
		return super.toString();
	}

}
