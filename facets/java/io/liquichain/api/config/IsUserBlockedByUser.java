package io.liquichain.api.config;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

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


public class IsUserBlockedByUser extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(IsUserBlockedByUser.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

	private Gson gson = new Gson();
  
    private String walletId;
  	private List<String> blockerWalletIds;
  
    private String result;

    public String getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
    public void setBlockerWalletIds(List<String> blockerWalletIds) {
        this.blockerWalletIds = blockerWalletIds;
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
      	
      	LOG.info("verify blockers of user - walletId = {}",walletId);
      	if(StringUtils.isBlank(walletId)){
        	result = returnError("WALLET_ID_NOT_FOUND", "walletId not found.");
			return;        	
        }
      
      	if(blockerWalletIds==null || blockerWalletIds.isEmpty()){
        	result = returnError("BLOCKER_WALLET_IDS_NOT_FOUND", "blocker walletIds not found.");
			return;          
        }
      
		walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        Wallet user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult(); 
        if(user == null){
        	result = returnError("USER_NOT_FOUND", "user not found against provided walletId.");
			return;
        }
         
      	for(int i=0; i<blockerWalletIds.size();i++){
          	String blockerWalletId = blockerWalletIds.get(i);
			blockerWalletIds.set(i,(blockerWalletId.startsWith("0x") ? blockerWalletId.substring(2) : blockerWalletId).toLowerCase());          
        }
    	  
        /*List<Wallet> blockers = crossStorageApi.find(defaultRepo, Wallet.class).by("inList uuid", blockerWalletIds).getResults(); 
        if(blockers == null || blockers.size()==0){
        	result = returnError("BLOCKERS_NOT_FOUND", "blockers not found against provided blockerWalletIds.");
			return;
        }
      	LOG.info("total valid blocker needs to verify = {}",blockers.size());
      	*/
      	List<BlockedUser> blockedUsers = crossStorageApi.find(defaultRepo, BlockedUser.class)
          .by("inList wallet", blockerWalletIds)
          .by("targetWallet", user)
          .getResults();

      
      	JsonArray responseObj = new JsonArray();
      	//== preparing the response
      	for(String blockerWalletId : blockerWalletIds){
      		JsonObject userObj = new JsonObject();
          	
          	userObj.addProperty("blockerWalletId",blockerWalletId);
          	userObj.addProperty("isBlocked",blockedUsers.stream().anyMatch(u -> u.getWallet().getUuid().equals(blockerWalletId)));
          
          	responseObj.add(userObj);
        }
      	      
      	result = "{\"status\" : \"success\", \"result\" : " + responseObj.toString() + "}";
    }

}