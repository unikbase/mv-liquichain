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

public class GetAddress extends Script {
	private static final Logger LOG = LoggerFactory.getLogger(GetAddress.class);
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

			result.put("status", "success");
			result.put("result", addressDetails);
		} else {
			String errorMessage = "Failed to retrieve address with uuid: " + uuid;
			LOG.error(errorMessage);
			result.put("status", "fail");
			result.put("result", errorMessage);
		}
	}
	
}
