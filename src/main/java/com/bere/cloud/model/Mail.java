package com.bere.cloud.model;

public class Mail extends MailTemplate{
	
	private String to;	
	
	public Mail(String from, String to, String subject, String body) {
		super(from, subject, body);
		setTo(to);
	}
	
	public Mail() {
		
	}
	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Mail( ");
	    if (getFrom()!=null) {
	    	sb.append("from:" + getFrom());
	    }
	    if (getTo()!=null) {
	    	sb.append("to:" + getTo());
	    }
	    if (getSubject()!=null) {
	    	sb.append(" subject:" + getSubject());
	    }
	    if (getBody()!=null) {
	    	sb.append(" body:" + getBody());
	    }
	    sb.append(")");
	    return sb.toString();
	}



}
