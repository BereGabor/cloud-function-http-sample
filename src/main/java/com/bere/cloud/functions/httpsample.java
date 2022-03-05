package com.bere.cloud.functions;

import com.google.api.pathtemplate.ValidationException;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import com.google.cloud.pubsub.v1.Publisher;

public class httpsample implements HttpFunction {
	//
	private static final Logger logger = LoggerFactory.getLogger(httpsample.class);
	private static final Gson gson = new Gson();
	private static String projectId="MLFF-SB";
	private static String topicName="projects/mlff-sb/topics/mlff-notifictiona-email";
	
	 
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

	// Simple function to return "Hello World"
    @Override
    public void service(HttpRequest request, HttpResponse response)
    		throws IOException {
    	String resp = "";
    	try {
    		JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
    		String bodyString = gson.toJson(body);
    		resp += "Json parsed: " + bodyString;
    		
    		resp += "\n" + sendMessageToTopic(projectId, topicName, bodyString);
    		
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