package io.liquichain.api.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Notification;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetNotificationsByWallet extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetNotificationsByWallet.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
    private final Map<String, Object> result = new HashMap<>() {{
        put("status", "fail");
    }};

    public void setWalletId(String walletId) {
        this.walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        ;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        Wallet wallet;
        try {
            wallet = crossStorageApi.find(defaultRepo, walletId, Wallet.class);
        } catch (Exception e) {
            String errorMessage = "Wallet does not exist: " + walletId;
            LOG.error(errorMessage, e);
            result.put("result", errorMessage);
            return;
        }

        if (wallet == null) {
            String errorMessage = "Wallet does not exist: " + walletId;
            result.put("result", errorMessage);
            return;
        }

        List<Notification> notifications = crossStorageApi.find(defaultRepo, Notification.class)
                                                          .by("recipient", wallet.getUuid())
                                                          .getResults();
        result.put("status", "success");
        result.put("result", notifications);
    }
}
