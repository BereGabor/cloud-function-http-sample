package com.bere.cloud.functions;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class httpsample implements HttpFunction {
	//
	private static final Logger logger = LoggerFactory.getLogger(httpsample.class);

	private static void testLogger() {
		logger.trace("Trace log");
		logger.debug("Debug log");
		logger.info ("Info log");
		logger.warn ("Warning log");
		logger.error("Error log");
	}

	// Simple function to return "Hello World"
    @Override
    public void service(HttpRequest request, HttpResponse response)
    		throws IOException {
        BufferedWriter writer = response.getWriter();
        writer.write("Hello World!");
        testLogger();
    }
}