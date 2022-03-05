package com.bere.cloud.model;

public class Mail {
	
	private String from;
	private String to;	
	private String subject;
	private String body;
	
	public Mail(String from, String to, String subject, String body) {
		this.setTo(to);
		this.from = from;
		this.subject = subject;
		this.body = body;
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
	    sb.append("Mail( ");
	    if (from!=null) {
	    	sb.append("from:" + from);
	    }
	    if (subject!=null) {
	    	sb.append(" subject:" + subject);
	    }
	    if (body!=null) {
	    	sb.append(" body:" + body);
	    }
	    sb.append(")");
	    return sb.toString();
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

}
