package io.liquichain.communication;

import java.util.Map;

import com.google.gson.Gson;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.EntityDoesNotExistsException;

import org.meveo.model.customEntities.ChatConversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetChatConversation extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetChatConversation.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String uuId;
    private String result;

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        uuId = normalizeHash(uuId);

        try {
            ChatConversation chatConversation = crossStorageApi.find(defaultRepo, uuId, ChatConversation.class);
            result = new Gson().toJson(chatConversation);

        } catch (EntityDoesNotExistsException ex) {
            String errorMessage = "Failed to find ChatConversation with hash = " + uuId;
            result = "{\"status\": \"failed\", \"result\": \"" + errorMessage + "\"}";
            LOG.error(errorMessage, ex);
            return;
        }
    }

    private String normalizeHash(String hash) {
        return hash.startsWith("0x") ? hash.substring(2).toLowerCase() : hash.toLowerCase();
    }

    public void setUuId(String uuId) {
        this.uuId = uuId;
    }

    public String getResult() {
        return this.result;
    }

}