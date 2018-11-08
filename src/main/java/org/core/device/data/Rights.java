package org.core.device.data;

import org.core.device.secure.Roles;

import javax.management.relation.Role;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by jane on 25.01.17.
 */
public class Rights {
    public boolean driver;
    public boolean camControl;
    public boolean irControl;
    public boolean shutdownEnabled;

    public Rights(HttpServletRequest req){
        driver = req.isUserInRole(Roles.ROLE_DRIVER);
        camControl = req.isUserInRole(Roles.ROLE_CAM_CONTROL);
        irControl = req.isUserInRole(Roles.ROLE_IR_CONTROL);
        shutdownEnabled = req.isUserInRole(Roles.ROLE_SHUTDOWN);

    }
}
