package io.liquichain.api.config;

import java.util.HashMap;
import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.UserConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfigurationScript extends Script {
    private static final Logger log = LoggerFactory.getLogger(UserConfigurationScript.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
  	private Map<String,String> configurations = new HashMap<>();
    private String result;

    public String getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public void setConfigurations(Map<String,String> configurations) {
        this.configurations = configurations;
    }

  	private String returnError(String errorCode, String message) {
        String res = "{\n";
        res += "  \"error\": { \"code\" : \"" + errorCode + "\" , \"message\" : \"" + message + "\"}\n";
        res += "}";
        return res;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
      
		walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        Wallet user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult(); 
        if(user == null){
        	result = returnError("USER_NOT_FOUND", "user not found against provided walletId.");
			return;
        }
      
      	UserConfiguration configs = new UserConfiguration();
      	configs.setUser(user);
      	configs.setConfigurations(configurations);
      
      	try{
			crossStorageApi.createOrUpdate(defaultRepo, configs);
          	result = "{ \"status\" : \"success\" , \"message\": \"user settings has been updated successfully.\"}";
        } catch(Exception ex){
        	log.error("Error in paytech payment transaction creation : {}",ex);
          	throw new BusinessException("Unable to create update user settings");
        }
    }

}