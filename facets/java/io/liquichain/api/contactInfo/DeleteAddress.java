package io.liquichain.api.contactInfo;

import java.util.HashMap;
import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Address;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAddress extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(DeleteAddress.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String uuid;
    private final Map<String, Object> result = new HashMap<>();

    public Map<String, Object> getResult() {
        return result;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
        if (uuid == null) {
            String errorMessage = "uuid is required.";
            LOG.error(errorMessage);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        Address address;
        try {
            address = crossStorageApi.find(defaultRepo, uuid, Address.class);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve address with uuid: " + uuid;
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        if (address != null) {
            try {
                crossStorageApi.remove(defaultRepo, uuid, Address.class);
            } catch (Exception e) {
                String errorMessage = "Failed to delete address with uuid: " + uuid;
                LOG.error(errorMessage, e);
                result.put("status", "fail");
                result.put("result", errorMessage);
                return;
            }
        } else {
            String errorMessage = "Address with uuid: " + uuid + " does not exist.";
            LOG.error(errorMessage);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        result.put("status", "success");
        result.put("result", "Address with uuid: " + uuid + " deleted.");
    }

}
