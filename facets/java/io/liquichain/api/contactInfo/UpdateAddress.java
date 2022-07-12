package io.liquichain.api.contactInfo;

import java.util.HashMap;
import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.customEntities.Address;
import org.meveo.model.customEntities.VerifiedPhoneNumber;
import org.meveo.model.customEntities.Wallet;
import org.meveo.model.storage.Repository;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateAddress extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateAddress.class);
    private final CrossStorageApi crossStorageApi = getCDIBean(CrossStorageApi.class);
    private final RepositoryService repositoryService = getCDIBean(RepositoryService.class);
    private final Repository defaultRepo = repositoryService.findDefaultRepository();

    private String uuid;
    private String name;
    private String streetAddress;
    private String city;
    private String state;
    private String countryCode;
    private String dialCode;
    private String postalCode;
    private double longitude;
    private double latitude;
    private String walletId;
    private String phoneNumber;
    private final Map<String, Object> result = new HashMap<>();

    public Map<String, Object> getResult() {
        return result;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setDialCode(String dialCode) {
        this.dialCode = dialCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setWalletId(String walletId) {
        if (walletId == null) {
            this.walletId = walletId;
        } else {
            this.walletId = (walletId.startsWith("0x") ? walletId.substring(2) : walletId).toLowerCase();
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        super.execute(parameters);
        Address address;
        try {
            address = crossStorageApi.find(defaultRepo, uuid, Address.class);
        } catch (Exception e) {
            String errorMessage = String.format("Address with id %s not found", uuid);
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
        }

        Wallet wallet;
        try {
            wallet = crossStorageApi.find(defaultRepo, walletId, Wallet.class);
        } catch (Exception e) {
            String errorMessage = String.format("Wallet with id %s not found", walletId);
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        VerifiedPhoneNumber verifiedPhoneNumber = null;
        if (phoneNumber != null) {
            try{
                verifiedPhoneNumber = crossStorageApi.find(defaultRepo, VerifiedPhoneNumber.class)
                                                     .by("walletId", walletId)
                                                     .by("phoneNumber", phoneNumber)
                                                     .getResult();
            } catch (Exception e) {
                verifiedPhoneNumber = new VerifiedPhoneNumber();
            }

            try {
                verifiedPhoneNumber.setPhoneNumber(phoneNumber);
                verifiedPhoneNumber.setWalletId(walletId);
                String uuid = crossStorageApi.createOrUpdate(defaultRepo, verifiedPhoneNumber);
                verifiedPhoneNumber.setUuid(uuid);
            } catch (Exception e) {
                String errorMessage = "Failed to save phone number: " + phoneNumber;
                LOG.error(errorMessage, e);
                result.put("status", "fail");
                result.put("result", errorMessage);
                return;
            }
        }

        address = new Address();
        address.setName(name);
        address.setStreetAddress(streetAddress);
        address.setCity(city);
        address.setState(state);
        address.setCountryCode(countryCode);
        address.setDialCode(dialCode);
        address.setPostalCode(postalCode);
        address.setLongitude(longitude);
        address.setLatitude(latitude);
        address.setWallet(wallet);
        address.setPhoneNumber(verifiedPhoneNumber);

        try {
            String uuid = crossStorageApi.createOrUpdate(defaultRepo, address);
            address.setUuid(uuid);
        } catch (Exception e) {
            String errorMessage = "Failed to save address: " + address;
            LOG.error(errorMessage, e);
            result.put("status", "fail");
            result.put("result", errorMessage);
            return;
        }

        Map<String, Object> addressDetails = new HashMap<>();
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

        result.put("status", "success");
        result.put("result", addressDetails);
    }

}
