package io.liquichain.api.config;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

public class ContactInfo extends Script {
	
  	private String result;
  	public String getResult(){return this.result;}
      
	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
		super.execute(parameters);
      
      	result = "{\"data\":[\n"
            + "{\"email\": \"email@address.com\",\n"
            + "\"phoneNumber\": \"+221 1111111\",\n"
            + "\"chatId\": \"helpMe\",\n"          
            + "\"name\":\"Agent01\"}\n"
            + "]\n"
            + "}";
	}
	
}