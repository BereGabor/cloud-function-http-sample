package com.bere.cloud.functions;

import com.bere.cloud.model.Mail;
import com.bere.cloud.model.MailRequest;
import com.bere.cloud.model.MailTemplate;
import com.google.api.client.http.HttpMethods;
import com.google.api.core.ApiFuture;
import com.google.api.pathtemplate.ValidationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
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
import java.util.UUID;
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
	private static final String projectId="MLFF-SB";
	private static final String topicName="mlff-notification-email";
	private static final String mailCollection="sample-mails";
	private static final String mailTemplateCollection="sample-mail-templates";
	private static final UUID instanceId = UUID.randomUUID();
	
	//global scope clients
    private static final Firestore db = initFirestore();
	private static final Publisher publisher = initPubSubPublisher();

	/*
	public httpsample() {
		super();
		try {
			long start = System.currentTimeMillis();
			logger.debug("Cold initialization start!");
			initFirestore();
			initPubSubPublisher();
			logger.debug("Cold init duration: " + String.valueOf(System.currentTimeMillis() - start));
		}
		catch (Exception e) {
			logger.error("Cold init failed: " + e.getMessage(), e);
		}
		
	}
	*/
	
	public static Publisher initPubSubPublisher(){
		
		try {
			long start = System.currentTimeMillis();
			Publisher pub = Publisher.newBuilder(
			        ProjectTopicName.of(projectId, topicName)).build();
		    logger.info("Init Publisher duration: " + String.valueOf(System.currentTimeMillis() - start));
		    return pub;
		} catch (IOException e) {
			logger.error("Error on init Publisher: " + e.getMessage());
			return null;
		}	
	}


	  public static Firestore initFirestore(){
		long start = System.currentTimeMillis();
		Firestore db = null;
	    try {
			FirestoreOptions firestoreOptions =
			        FirestoreOptions.getDefaultInstance().toBuilder()
			            .setProjectId(projectId)
			            .setCredentials(GoogleCredentials.getApplicationDefault()).build();
			
		    db = firestoreOptions.getService();
		    
		    try {
				db.document(mailTemplateCollection+"/simple-mail").get().get();
			} catch (Exception e) {
				logger.error("Error on get default doc: " + e.getMessage(), e);
			}
		    
		    logger.info("Init FireStore DB duration: " + String.valueOf(System.currentTimeMillis() - start));
		} catch (IOException e) {
			logger.error("Error on initialize FireStore db connection: " + e.getMessage(), e);
		}
	    return db;
	    // [END fs_initialize_project_id]
	    // [END firestore_setup_client_create_with_project_id]
	    // [END firestore_setup_client_create]
	  }
	  
	private static void listCollectionDocuments(Firestore db, String collection) {
	    logger.debug("Mail Templates in coillection:" + collection);
	    try {
		    for (DocumentReference docRef : db.collection(collection).listDocuments()) {
		    	logger.debug(docRef.getPath()); 
				
			} 
	    }
	    catch (Exception e) {
	    	logger.error("Error on get doc paths:" + e.getMessage());
	    }
	}
	
	
	private static void testLogger() {
		logger.trace("Trace log");
		logger.debug("Debug log");
		logger.info ("Info log");
		logger.warn ("Warning log");
		logger.error("Error log");
	}
	
	private String sendMessageToTopic(String projectId, String topicName, String msg) throws IOException {
		String resp = "";
		ByteString byteStr = ByteString.copyFrom(msg, StandardCharsets.UTF_8);
	    PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();


	    // Attempt to publish the message
		long start = System.currentTimeMillis();
		publisher.publish(pubsubApiMessage);
        resp += "\n Message published.";
		logger.debug("Publish message duration: " + String.valueOf(System.currentTimeMillis() - start));
	    return resp;
		
	}
	
	private CollectionReference getCollection(String collection) {
		long start = System.currentTimeMillis();
		CollectionReference ref = db.collection(collection);
		logger.debug("Get collection duration: " + String.valueOf(System.currentTimeMillis() - start));
		return ref;
	}
	  
	private MailTemplate getMailTemplate(String collection, String templateId) throws Exception{
	    DocumentReference docRef = getCollection(collection).document(templateId);
	 // asynchronously retrieve the document
	    ApiFuture<DocumentSnapshot> future = docRef.get();
	    // ...
	    // future.get() blocks on response
		long start = System.currentTimeMillis();
	    DocumentSnapshot document = future.get();
		logger.debug("Get document duration: " + String.valueOf(System.currentTimeMillis() - start));
	    if (!document.exists())	{
	    	logger.warn("Template not exists templateId:" + templateId);
	    	return null;
	    }
	    else {
	    	return document.toObject(MailTemplate.class);
	    }
		
	}
	
	private void saveMailTemplate(String collection, String templateId, MailTemplate mailTemplate) throws Exception{
	    DocumentReference docRef = db.collection(collection).document(templateId);
	    ApiFuture<WriteResult> result = docRef.set(mailTemplate);
	    logger.info("Store MailTemplate success: " + mailTemplate.toString() + "Update time : " + result.get().getUpdateTime() + " id:" + templateId);
		
	}
	  
	private String storeRequestToFireStore(String collection, Mail mail) {
		String res = "";
		try {
		    try {
				long start = System.currentTimeMillis();
		    	ApiFuture<DocumentReference> addedDocRef = db.collection(collection).add(mail);
		    	logger.info("Store mail in firestore success obj id:" + addedDocRef.get().getId());
				logger.debug("Save request to FireStore duration: " + String.valueOf(System.currentTimeMillis() - start));
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
    			long start = System.currentTimeMillis();
    			template = getMailTemplate(mailTemplateCollection, templateId);
    			logger.debug("Get template duration:" + String.valueOf(System.currentTimeMillis() - start));
    			if (template == null) {
    				// use request as template
    				start = System.currentTimeMillis();
    				template = new MailTemplate( request.getFrom(), request.getSubject(), request.getBody());
        			try {
        				saveMailTemplate(mailTemplateCollection, templateId, template);
        			}
        			catch (Exception e2) {
        				logger.error("Save template failed: " + e2.getMessage(), e2);
        			}
        			logger.debug("Save template duration:" + String.valueOf(System.currentTimeMillis() - start));
    				
    			}
    			logger.info("Template found in collection:" + mailTemplateCollection + " templateId:" + templateId);
    		}
    		catch (Exception e) {
    			logger.error("Exception during prepareTemplate:" + e.getMessage(), e);
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
    	long start = System.currentTimeMillis();
    	String resp = "";
    	try {
    		MailRequest mailRequest = gson.fromJson(request.getReader(), MailRequest.class);
    		if (request.getMethod() != HttpMethods.POST) {
    			logger.error("HTTP Methode not supported: " + request.getMethod() + " Use HTTP POST!");
    			resp = "HTTP Methode not supported: " + request.getMethod();
    			return;
    		}
    		if (mailRequest == null) {
    			logger.error("Empty request!");
    			resp = "Empty request!";
    			return;
    		}
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
    	finally {
    		resp += "\n Service execution: " + String.valueOf(System.currentTimeMillis() - start + "ms");
    		resp += "\n InstanceId" + instanceId.toString();
			BufferedWriter writer = response.getWriter();
		    writer.write(resp);
    	}
        //testLogger();
        
    }
}