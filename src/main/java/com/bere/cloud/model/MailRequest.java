package com.bere.cloud.model;

import com.google.gson.JsonObject;

public class MailRequest extends Mail{
	
	private String templateId;
	private JsonObject params;
	
	public MailRequest() {
		// TODO Auto-generated constructor stub
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public JsonObject getParams() {
		return params;
	}

	public void setParams(JsonObject params) {
		this.params = params;
	}
		
	
}
