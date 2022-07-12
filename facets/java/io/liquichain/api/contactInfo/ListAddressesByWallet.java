package io.liquichain.api.contactInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Address;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListAddressesByWallet extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(GetAddress.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String walletId;
    private final Map<String, Object> result = new HashMap<>();

    public Map<String, Object> getResult() {
        return result;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    private Map<String, Object> mapAddress(Address address) {
        Map<String, Object> addressDetails = new HashMap<>();
        addressDetails.put("uuid", address.getUuid());
        addressDetails.put("name", address.getName());
        addressDetails.put("streetAddress", address.getStreetAddress());
        addressDetails.put("city", address.getCity());
        addressDetails.put("state", address.getState());
        addressDetails.put("countryCode", address.getCountryCode());
        addressDetails.put("dialCode", address.getDialCode());
        addressDetails.put("postalCode", address.getPostalCode());
        addressDetails.put("longitude", address.getLongitude());
        addressDetails.put("latitude", address.getLatitude());
        addressDetails.put("walletId", address.getWallet().getUuid());
        addressDetails.put("phoneNumber",
            address.getPhoneNumber() != null ? address.getPhoneNumber().getPhoneNumber() : null);

        return addressDetails;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);

        if (walletId == null) {
            String errorMessage = "walletId is required.";
            LOG.error(errorMessage);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        Wallet wallet;
        try {
            wallet = crossStorageApi.find(defaultRepo, walletId, Wallet.class);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve wallet with id: " + walletId;
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }
        if (wallet == null) {
            String errorMessage = "Wallet with id: " + walletId + " not found.";
            LOG.error(errorMessage);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        List<Address> addresses = null;
        try {
            addresses = crossStorageApi.find(defaultRepo, Address.class)
                                       .by("wallet", wallet)
                                       .getResults();
        } catch (Exception e) {
            String errorMessage = "Failed to list addresses for wallet with id: " + walletId;
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
        }

        List<Map<String, Object>> addressList = addresses == null
            ? new ArrayList<>()
            : addresses.stream()
                       .map(this::mapAddress)
                       .collect(Collectors.toList());
        result.put("status", "success");
        result.put("result", addressList);
    }

}
