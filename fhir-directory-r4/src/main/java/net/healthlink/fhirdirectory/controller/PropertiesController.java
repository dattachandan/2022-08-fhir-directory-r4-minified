package net.healthlink.fhirdirectory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import net.healthlink.fhirdirectory.configuration.dtos.ConfigurationProperties;

@RestController
public class PropertiesController {
	
	@Autowired
	private ConfigurationProperties configurationProperties;
	
	@GetMapping
    public String getPoperties() {
        return "Properties: " + configurationProperties.toString();
    }
	
	

}
