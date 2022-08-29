package io.liquichain.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.Instant;

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

public class BlockUser extends Script {
    private static final Logger log = LoggerFactory.getLogger(BlockUser.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
	private String targetWalletId;
  	private Boolean blocked;
  
    private String result;

    public String getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public void setTargetWalletId(String targetWalletId) {
        this.targetWalletId = targetWalletId;
    }

 	public void setBlocked(Boolean blocked){
    	this.blocked = blocked;  
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
      
      	log.info("block user - walletId = {} , targetWalletId= {}, blocked={}",walletId,targetWalletId,blocked);
		walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        Wallet user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult(); 
        if(user == null){
        	result = returnError("USER_NOT_FOUND", "user not found against provided walletId.");
			return;
        }
      
      	targetWalletId = (targetWalletId.startsWith("0x") ? targetWalletId.substring(2) : targetWalletId).toLowerCase();
        Wallet targetUser = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", targetWalletId).getResult(); 
      	if(targetUser == null){
        	result = returnError("USER_NOT_FOUND", "target user not found against provided targetWalletId.");
			return;
        }
      
      	BlockedUser blockedUser = crossStorageApi.find(defaultRepo, BlockedUser.class).by("targetWallet",targetUser).by("wallet", user).getResult();
		//== unblock/delete the user from the BlockedUser CET
      	if(!blocked){
        	if(blockedUser == null){
        		result = "{ \"status\" : \"success\" , \"message\": \"user is already unblocked.\"}";
				return;  
            } else {
            	crossStorageApi.remove(defaultRepo,blockedUser.getUuid(),BlockedUser.class);
              	result = "{ \"status\" : \"success\" , \"message\": \"user has been unblocked successfully.\"}";
              	return;
            }
        }
      
      	//== block the user
      	if(blockedUser != null){
        	result = "{ \"status\" : \"success\" , \"message\": \"user is already blocked.\"}";
          	return;
        }
        
      	blockedUser = new BlockedUser();
      	blockedUser.setWallet(user);
        blockedUser.setTargetWallet(targetUser);
      	blockedUser.setBlockDate(Instant.now());
        
      	try{
			crossStorageApi.createOrUpdate(defaultRepo, blockedUser);
          	result = "{ \"status\" : \"success\" , \"message\": \"user has been blocked successfully.\"}";
        } catch(Exception ex){
        	log.error("Error in blocking the user : {}",ex);
          	throw new BusinessException("Unable to block user.");
        }
    }

}