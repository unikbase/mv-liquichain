package io.liquichain.api.config;

import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.model.customEntities.UserConfiguration;
import org.meveo.model.customEntities.Wallet;
import org.meveo.commons.utils.StringUtils;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils extends Script {
	
    private static final Logger log = LoggerFactory.getLogger(UserUtils.class);
  	private Wallet user;

    private CrossStorageApi crossStorageApi;
    private Repository defaultRepo;

    public UserUtils() {
        super();
    }

    public UserUtils(CrossStorageApi crossStorageApi, Repository defaultRepo) {
        this.crossStorageApi = crossStorageApi;
        this.defaultRepo = defaultRepo;
    }
  
  	public UserUtils(CrossStorageApi crossStorageApi, Repository defaultRepo, Wallet user) {
        this.crossStorageApi = crossStorageApi;
        this.defaultRepo = defaultRepo;
      	this.user = user;
    }
  
  
  	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
		super.execute(parameters);
	}
	
  	public UserConfiguration getUserConfigurationsByWalletId(String walletId){
		return loadUserConfigurationByWalletId(walletId);
    }
  	
  	public boolean isUserEmailNotificationsAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsEmailNotificationsEnabled()?true:false; 
    }
  
  	public boolean isUserOrderUpdatesAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsOrderUpdatesEnabled()?true:false; 
    }

  	public boolean isUserSellerInfoUpdatesAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsSellerInfoUpdatesEnabled()?true:false; 
    }
  
  	public boolean isUserChatNotificationsAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsChatNotificationsEnabled()?true:false; 
    }
  
  	public boolean isUserChatFromProfilePageAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsChatEnabledFromProfilePage()?true:false; 
    }
  
  	public boolean isUserAutoReplyMessageAllowed(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig != null && userConfig.getIsAutoReplyEnabled()?true:false; 
    }  
  
  	public String getUserAutoReplyMessage(String walletId){
   		UserConfiguration userConfig = loadUserConfigurationByWalletId(walletId);
    	return userConfig.getAutoReplyMessage(); 
    }
  
  	private UserConfiguration loadUserConfigurationByWalletId(String walletId){
      	try{
        	if(StringUtils.isBlank(walletId)){
              	log.error("Failed to retrieve user's configuration. walletId is not provided.");
          		return null;
        	}

            if(user == null){
        		walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
				user = crossStorageApi.find(defaultRepo, Wallet.class).by("uuid", walletId).getResult();
              	if(user == null){
            		log.error("User not found against walletId = {}",walletId);  
              		return null;
                }
            }
          
          	UserConfiguration configs = crossStorageApi.find(defaultRepo, UserConfiguration.class)
            		.by("user", user)
                	.getResult();
          	//== loading the default configurations
          	if(configs == null){
            	configs = new UserConfiguration();
              	configs.setIsEmailNotificationsEnabled(true);
              	configs.setIsOrderUpdatesEnabled(true);
 				configs.setIsSellerInfoUpdatesEnabled(true);
              	configs.setIsChatNotificationsEnabled(true);
              	configs.setIsChatEnabledFromProfilePage(true);
              	configs.setIsAutoReplyEnabled(true);
            }
          	configs.setUser(user);
          
          	return configs;
        }catch(Exception ex){
        	log.error("Failed to retrieve the user's configurations for wallet id :"+walletId+" errorMessage: "+ex);
        }
      	return null;
    }
  	
}