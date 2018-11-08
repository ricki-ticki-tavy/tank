package org.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.Device;
import org.core.device.data.EnginesInfo;
import org.core.device.data.SysInfo;
import org.core.device.secure.Roles;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.sql.Driver;

/**
 * Created by jane on 07.01.17.
 */
public class DriveServlet extends HttpServlet {

    private EnginesInfo parseEnginesInfo(String source) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.fromJson(source, EnginesInfo.class);
    }

    private String prepareEnginesInfo(EnginesInfo enginesInfo) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(enginesInfo);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String data = req.getParameter("engines");

        PrintWriter out = resp.getWriter();

        if ((data == null) || (data.isEmpty())) {
            throw new RuntimeException("bad param engines");
        }

        EnginesInfo enginesInfo = parseEnginesInfo(data);
        Principal user = req.getUserPrincipal();
        out.write(prepareEnginesInfo(Device.getDeviceInstance().driveWith(enginesInfo, req.isUserInRole(Roles.ROLE_DRIVER))));

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
