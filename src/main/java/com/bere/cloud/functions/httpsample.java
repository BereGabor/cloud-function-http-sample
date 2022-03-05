package com.bere.cloud.functions;

import com.bere.cloud.model.Mail;
import com.google.api.core.ApiFuture;
import com.google.api.pathtemplate.ValidationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import org.apache.http.util.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
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
	private static String topicName="mlff-notifictiona-email";
	private static String mailCollection="sample-mails";
	
    private static Firestore db = null;
	
	 
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
	        resp += "\n Message published.";
	    } catch (InterruptedException | ExecutionException | IOException | ValidationException e) {
	      logger.error("Error publishing Pub/Sub message: " + e.getMessage(), e);
	      resp += "\n Error publishing Pub/Sub message; see logs for more info.";
	    }
	    return resp;
		
	}


	  public static void initFirestore() throws Exception {
		  if (db != null) {
			  return;
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
	  
	private String storeRequestToFireStore(String collection, JsonObject obj) {
		String res = "";
		try {
		    initFirestore();
		    try {
		    	Mail mail = gson.fromJson(obj, Mail.class);
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

	
    @Override
    public void service(HttpRequest request, HttpResponse response)
    		throws IOException {
    	String resp = "";
    	try {
    		JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
    		String bodyString = gson.toJson(body);
    		resp += "Json parsed: " + bodyString;
    		
    		resp += "\n" + sendMessageToTopic(projectId, topicName, bodyString);
    		
    		resp += "\n" + storeRequestToFireStore(mailCollection, body);
    		
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