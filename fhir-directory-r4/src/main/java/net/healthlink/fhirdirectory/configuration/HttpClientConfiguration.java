/*
 * Copyright (c) 2020 HealthLink Limited.
 *
 * This document is copyright. Except for the purpose of fair reviewing, no part
 * of this publication may be reproduced or transmitted in any form or by any
 * means, electronic or mechanical, including photocopying, recording, or any
 * information storage and retrieval system, without permission in writing from
 * the publisher. Infringers of copyright render themselves liable for
 * prosecution.
 * 
 * @author Sajith Jamal
 */

package net.healthlink.fhirdirectory.configuration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileUrlResource;


import org.apache.http.ssl.SSLContexts;

import java.io.IOException;

import javax.net.ssl.SSLContext;

import org.springframework.core.io.Resource;

@Configuration
public class HttpClientConfiguration {
	
	private PoolingHttpClientConnectionManager connectionManager;
	
	@Value("${connectionpool.maxTotal}")
	private int maxTotal;
	
	@Value("${connectionpool.defaultMaxPerRoute}")
	private int defaultMaxPerRoute;
	
	@Value("${connectionpool.idleConnectionValidateTime}")
	private int idleConnectionValidateTime;
	
	@Bean
	public HttpClient httpClient() throws IOException
	{
		connectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
		connectionManager.setMaxTotal(maxTotal);//configurable through app.properties
        // Increase default max connection per route to 50
		connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);//configurable through app.properties
		//manage idleconnection open time
		connectionManager.setValidateAfterInactivity(idleConnectionValidateTime);//configurable through app.properties
		

        SSLContext sslContext = SSLContexts.createSystemDefault();
		
		
		return HttpClients.custom()
				.setSSLContext(sslContext)
                .setConnectionManager(connectionManager) .build();
	}
	
	
	String getFilePath(Resource resource) throws IOException {
		return ((FileUrlResource) resource).getFile().getAbsolutePath();
        
    }

}
