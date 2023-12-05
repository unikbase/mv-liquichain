package io.liquichain.api.config;

import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.UserConfiguration;
import org.meveo.service.script.Script;
import org.meveo.service.script.ScriptInstanceService;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetUserConfigsByWalletId extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetUserConfigsByWalletId.class);

    private final ScriptInstanceService scriptInstanceService = getCDIBean(ScriptInstanceService.class);
    private final UserUtils userUtils = (UserUtils) scriptInstanceService.getExecutionEngine("UserUtils", null);

    private String walletId;
    private String result;

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
            LOG.debug("Load user settings by walletId: {}", walletId);

            if (StringUtils.isBlank(walletId)) {
                result = returnError("REQUIRED_WALLET_ID", "Wallet id is required.");
                return;
            }

            walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
            UserConfiguration configs = userUtils.loadUserConfigByWalletId(walletId);

            result = new Gson().toJson(configs);
        } catch (Exception e) {
            String errorMessage = "Encountered error while retrieving user configuration.";
            LOG.error(errorMessage, e);
            result = returnError("USER_CONFIG_NOT_FOUND", errorMessage);
        }
    }
}
