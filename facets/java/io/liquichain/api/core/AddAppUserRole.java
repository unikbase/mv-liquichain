package io.liquichain.api.core;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAppUserRole extends Script {
  private static final Logger LOG = LoggerFactory.getLogger(AddAppUserRole.class);
	
	@Override
	public void execute(Map<String, Object> parameters) throws BusinessException {
      	LOG.info("=================================================");
		LOG.info("=============== ADD APP USER ROLE ===============");
      	LOG.info("=================================================");
	}
	
}