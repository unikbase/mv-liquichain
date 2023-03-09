package io.liquichain.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import org.meveo.model.customEntities.UserConfiguration;

import org.meveo.commons.utils.StringUtils;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import com.google.gson.Gson;

public class GetUserConfigsByWalletId extends Script {
    private String walletId;
    private String result;

    private static final Logger log = LoggerFactory.getLogger(GetUserConfigsByWalletId.class);
    private final UserUtils userUtils = new UserUtils();

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    private String returnError(String errorCode, String message) {
        return "{\"error\": { \"code\" : \"" + errorCode + "\" , \"message\" : \"" + message + "\"}}";
    }

    public String getResult() {
        return this.result;
    }


    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        try {
            log.debug("Load user settings by walletId: {}", walletId);

            if (StringUtils.isBlank(walletId)) {
                result = returnError("REQUIRED_WALLET_ID", "Wallet id is required.");
                return;
            }

            walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
            UserConfiguration configs = userUtils.loadUserConfigByWalletId(walletId);

            result = new Gson().toJson(configs);
        } catch (Exception e) {
            String errorMessage = "Encountered error while retrieving user configuration.";
            log.error(errorMessage, e);
            result = returnError("USER_CONFIG_NOT_FOUND", errorMessage);
        }
    }
}
