package io.liquichain.communication;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.ChatConversation;
import org.meveo.model.customEntities.ChatConversationParticipant;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.service.storage.RepositoryService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetChatConversations extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetChatConversations.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        walletId = normalizeHash(walletId);
        List<ChatConversation> chatConversations = null;		
          
        try {
            List<ChatConversationParticipant> participants = crossStorageApi.find(defaultRepo, ChatConversationParticipant.class)
                    .by("participant", walletId)
                    .select("chatConversation")
                    .getResults();

            List<String> chatConversationIds = participants.stream().map(participant -> participant.getChatConversation().getUuid()).collect(Collectors.toList());
			
          	if(chatConversationIds != null && !chatConversationIds.isEmpty()){
            	chatConversations = crossStorageApi.find(defaultRepo, ChatConversation.class)
                    	.by("inList uuid", chatConversationIds)
                    	.getResults();
                
            }
        } catch (Exception ex) {
            String errorMessage = "Failed to find ChatConversationParticipant with hash = " + walletId;
            LOG.error(errorMessage, ex);
        }

      	
        if (chatConversations != null && !chatConversations.isEmpty()) {
            Gson gson = new GsonBuilder()
    					.serializeNulls()
               			.setPrettyPrinting()
    					.create();
            this.result = gson.toJson((new ChatConversationDetail()).getOutputChatConversationList(chatConversations));
        } else {
            this.result = "[]";
        }
    }
  
    private String normalizeHash(String hash) {
        return hash.startsWith("0x") ? hash.substring(2).toLowerCase() : hash.toLowerCase();
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getResult() {
        return this.result;
    }
  
}