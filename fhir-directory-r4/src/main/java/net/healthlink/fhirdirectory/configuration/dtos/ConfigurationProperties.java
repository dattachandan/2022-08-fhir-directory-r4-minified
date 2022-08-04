package net.healthlink.fhirdirectory.configuration.dtos;

@org.springframework.boot.context.properties.ConfigurationProperties

public class ConfigurationProperties {

	private String springMainAllowCircularReferences;
	
	public String getSpringMainAllowCircularReferences() {
		return springMainAllowCircularReferences;
	}
	public void setSpringMainAllowCircularReferences(String springMainAllowCircularReferences) {
		this.springMainAllowCircularReferences = springMainAllowCircularReferences;
	}
	
	@Override
	public String toString() {
		return "springMainAllowCircularReferences:"+springMainAllowCircularReferences;
	}
	
}
