package io.liquichain.api.core;

import java.util.Map;

import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddAppUserRole extends Script {
    private static final Logger LOG = LoggerFactory.getLogger(AddAppUserRole.class);
    private final RoleService roleService = getCDIBean(RoleService.class);

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        Role role = null;
        try {
            role = roleService.findByName("APP_USER");
        } catch (Exception e) {
            // do nothing, we will create if it does not exist.
        }

        if (role == null) {
            role = new Role();
            role.setName("APP_USER");
            role.setDescription("Role for Liquichain App secured endpoints ");

            try {
                roleService.create(role);
            } catch (Exception e) {
                LOG.error("Failed to add APP_USER role.", e);
            }
        } else {
            LOG.info("APP_USER role already exists.");
        }
    }
}
