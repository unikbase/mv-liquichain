package io.liquichain.api.config;

import java.util.List;
import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.customEntities.BlockedUser;
import org.meveo.commons.utils.StringUtils;

import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;


public class GetBlockUser extends Script {
    private static final Logger log = LoggerFactory.getLogger(GetBlockUser.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
  
    private String result;

    public String getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
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
      
      	log.info("get blocked users of - walletId = {}",walletId);
		walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        Wallet user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult(); 
        if(user == null){
        	result = returnError("USER_NOT_FOUND", "user not found against provided walletId.");
			return;
        }
            
      	List<BlockedUser> blockedUsers = crossStorageApi.find(defaultRepo, BlockedUser.class).by("wallet", user).getResults();
      	if(blockedUsers==null || blockedUsers.isEmpty()){
        	result = "{ \"status\" : \"success\" , \"message\": \"user's block list is empty.\"}";
          	return;
        }
      
      	Gson gson = new Gson();
      	JsonArray responseObj = new JsonArray();
      	for(BlockedUser blockedUser : blockedUsers){
      		JsonObject userObj = gson.toJsonTree(blockedUser).getAsJsonObject();
          	userObj.remove("wallet");
          	responseObj.add(userObj);
        }
      	
      	result = "{\"status\" : \"success\", \"result\" : " + responseObj.toString() + "}";
    }

}