package io.liquichain.api.config;

import java.util.Map;

import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.model.storage.Repository;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserUtils extends Script {
	
    private static final Logger log = LoggerFactory.getLogger(UserUtils.class);

    private CrossStorageApi crossStorageApi;
    private Repository defaultRepo;

    public UserUtils() {
        super();
    }

    public UserUtils(CrossStorageApi crossStorageApi, Repository defaultRepo) {
        this.crossStorageApi = crossStorageApi;
        this.defaultRepo = defaultRepo;
    }
  
  
  	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
		super.execute(parameters);
	}
	
  	
}