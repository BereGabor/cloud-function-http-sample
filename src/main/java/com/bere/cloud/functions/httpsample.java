package com.bere.cloud.functions;

import com.bere.cloud.model.Mail;
import com.bere.cloud.model.MailRequest;
import com.bere.cloud.model.MailTemplate;
import com.google.api.core.ApiFuture;
import com.google.api.pathtemplate.ValidationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import com.google.cloud.pubsub.v1.Publisher;

public class httpsample implements HttpFunction {
	//
	private static final Logger logger = LoggerFactory.getLogger(httpsample.class);
	private static final Gson gson = new Gson();
	private static String projectId="MLFF-SB";
	private static String topicName="mlff-notification-email";
	private static String mailCollection="sample-mails";
	private static String mailTemplateCollection="sample-mail-templates";
	
	
    private Firestore db = null;
	
	 
	private static void testLogger() {
		logger.trace("Trace log");
		logger.debug("Debug log");
		logger.info ("Info log");
		logger.warn ("Warning log");
		logger.error("Error log");
	}
	
	private String sendMessageToTopic(String projectId, String topicName, String msg) {
		String resp = "";
		ByteString byteStr = ByteString.copyFrom(msg, StandardCharsets.UTF_8);
	    PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();


	    // Attempt to publish the message
	    try {
	    	Publisher publisher = Publisher.newBuilder(
	            ProjectTopicName.of(projectId, topicName)).build();

	    	publisher.publish(pubsubApiMessage).get();
	    	publisher.shutdown();
	        resp += "\n Message published.";
	    } catch (InterruptedException | ExecutionException | IOException | ValidationException e) {
	      logger.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
	      resp += "\n Error publishing Pub/Sub message; see logs for more info.";
	    }
	    return resp;
		
	}


	  public void initFirestore() throws Exception {
		  if (db != null) {
			  logger.info("FireStore connection already initialized!");
			  return;
		  }
		  else {
			  logger.info("Init FireStore connection!");
		  }
	    // [START firestore_setup_client_create]
	    // Option 1: Initialize a Firestore client with a specific `projectId` and
	    //           authorization credential.
	    // [START fs_initialize_project_id]
	    // [START firestore_setup_client_create_with_project_id]
	    FirestoreOptions firestoreOptions =
	        FirestoreOptions.getDefaultInstance().toBuilder()
	            .setProjectId(projectId)
	            .setCredentials(GoogleCredentials.getApplicationDefault())
	            .build();
	    //db = firestoreOptions.getService();
	    db = FirestoreOptions.getDefaultInstance().getService();
	    // [END fs_initialize_project_id]
	    // [END firestore_setup_client_create_with_project_id]
	    // [END firestore_setup_client_create]
	  }
	  
	private MailTemplate getMailTemplate(String collection, String templateId) throws Exception{
	    initFirestore();
	    DocumentReference docRef = db.collection(collection).document(templateId);
	 // asynchronously retrieve the document
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    // ...
	    // future.get() blocks on response
	    DocumentSnapshot document = future.get();
	    if (!document.exists())	{
	    	logger.warn("Template not exists templateId:" + templateId);
	    	return null;
	    }
	    else {
	    	return document.toObject(MailTemplate.class);
	    }
		
	}
	
	private void saveMailTemplate(String collection, String templateId, MailTemplate mailTemplate) throws Exception{
	    initFirestore();
	    DocumentReference docRef = db.collection(collection).document(templateId);
	    ApiFuture<WriteResult> result = docRef.set(mailTemplate);
	    logger.info("Store MailTemplate success: " + mailTemplate.toString() + "Update time : " + result.get().getUpdateTime() + " id:" + templateId);
		
	}
	  
	private String storeRequestToFireStore(String collection, Mail mail) {
		String res = "";
		try {
		    initFirestore();
		    try {
		    	ApiFuture<DocumentReference> addedDocRef = db.collection(collection).add(mail);
		    	logger.info("Store mail in firestore success obj id:" + addedDocRef.get().getId());
		    }
		    catch (JsonSyntaxException e) {
				res += "\n Parse to Mail object failed: " +e.getMessage(); 
				logger.error("Parse to Mail object failed: " +e.getMessage(), e);
		    }
		}
		catch (Exception e) {
			res += "\n init Firestore db failed: " +e.getMessage(); 
			logger.error("Firestore db failed: " +e.getMessage(), e);
		}
		
		return res;
	}
	
	private MailTemplate prepareTemplate(MailRequest request) {
		MailTemplate template = null;
		if (request.getTemplateId() != null) {
			String templateId=request.getTemplateId();
    		try {
    			template = getMailTemplate(mailTemplateCollection, templateId);
    			if (template == null) {
    				// use request as template
    				template = new MailTemplate( request.getFrom(), request.getSubject(), request.getBody());
        			try {
        				saveMailTemplate(mailTemplateCollection, templateId, template);
        			}
        			catch (Exception e2) {
        				logger.error("Save template failed: " + e2.getMessage(), e2);
        			}
    				
    			}
    			logger.info("Template found in collection:" + mailTemplateCollection + " templateId:" + templateId);
    		}
    		catch (Exception e) {
    		}
		}
		return template;
	}
	
	public Mail prepareMailFromTemplate(MailTemplate template, MailRequest request) {
		// init mail from template
		Mail mail = new Mail( 
				template.getFrom(), 
				request.getTo(), 
				template.getSubject(),
				template.getBody());
		
		// if params exists evaluate body as velocity template text
		if (request.getParams() != null) {
			VelocityContext context = new VelocityContext();
			JsonObject params = request.getParams();
			Iterator<String> keys = params.keySet().iterator();

			while(keys.hasNext()) {
			    String key = keys.next();
			    if (params.get(key) instanceof JsonElement) {
			      context.put(key, params.get(key));       
			    }
			}
	        StringWriter swOut = new StringWriter();
	        String templateStr = template.getBody();
	        
	        /**
	         * Merge data and template
	         */
	        Velocity.evaluate( context, swOut, "Prepare mail body template", templateStr);
	 		
			mail.setBody(swOut.toString());
		}
		return mail;
	}

	
    @Override
    public void service(HttpRequest request, HttpResponse response)
    		throws IOException {
    	String resp = "";
    	try {
    		MailRequest mailRequest = gson.fromJson(request.getReader(), MailRequest.class);
    		String bodyString = gson.toJson(mailRequest);
    		Mail mail = mailRequest;
    		resp += "Json parsed: " + bodyString;
    		MailTemplate template = prepareTemplate(mailRequest);
    		if (template != null) {
    			mail =  prepareMailFromTemplate(template, mailRequest);
    			resp += "\n" + sendMessageToTopic(projectId, topicName, gson.toJson(mail));
    		}
    		else{
    			resp += "\n" + sendMessageToTopic(projectId, topicName, bodyString);
    		}
    		resp += "\n" + storeRequestToFireStore(mailCollection, mail);
    		
    	}
    	catch (Exception e) {
    		logger.error("Can't parse request json: " + e.getMessage(), e);
    		resp += "\n" + "Can't parse request json: " + e.getLocalizedMessage();
    	}
    	
    	BufferedWriter writer = response.getWriter();
        writer.write(resp);
        
        //testLogger();
        
    }
}