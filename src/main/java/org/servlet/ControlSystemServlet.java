package org.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.Device;
import org.core.device.PeripherialsMonitor;
import org.core.device.data.CompositeTelemetric;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.data.Rights;
import org.core.device.data.SysInfo;
import org.core.device.network.ConnectionsController;
import org.core.device.secure.Roles;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jane on 07.01.17.
 */
public class ControlSystemServlet extends HttpServlet {

    public static final String ATTR_NAME_TEMP = "temp";
    public static final String ATTR_NAME_FREQ = "freq";
    public static final String ATTR_NAME_CAM_STATUS = "cam";
    public static final String ATTR_NAME_ALL = "all";
    public static final String ATTR_NAME_HARDWARE = "hardwareOptions";
    public static final String ATTR_NAME_SYSTEM_SHUTDOWN = "systemShutdown";
    public static final String ATTR_NAME_RIGHTS = "rights";
    public static final String ATTR_NAME_LOGOUT = "logout";


    private String prepareAllSysInfo() {
        SysInfo sysInfo = new SysInfo();
        Device device = Device.getDeviceInstance();
        sysInfo.cameraFound = device.isCameraFound();
        sysInfo.dmaPwmActive = device.isDmaPwmActiove();
        CompositeTelemetric telemetric = new CompositeTelemetric();
        telemetric.sysInfo = sysInfo;
        telemetric.hardwareStatus = device.getHardwareStatus();
        telemetric.peripherial = device.getPeripherialsInfo();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(telemetric);
    }

    private String serializeObjToJson(Object obj){
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(obj);
    }

    private String prepareHardwareOptions() {
        return serializeObjToJson(HardwareSystemOptions.getInstance());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String option = req.getParameter("thing");

        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        switch (option) {
            case ATTR_NAME_TEMP: {
                out.write(PeripherialsMonitor.getInstance().getPeripherialsInfo().cpuTempStr);
                break;
            }
            case ATTR_NAME_FREQ: {
                out.write(PeripherialsMonitor.getInstance().getPeripherialsInfo().cpuFreqStr);
                break;
            }
            case ATTR_NAME_CAM_STATUS: {
                out.write(Device.getDeviceInstance().isCameraFound() + "");
                break;
            }
            case ATTR_NAME_ALL: {
                ConnectionsController.getInstance().updateLastAccess();
                out.write(prepareAllSysInfo());
                break;
            }
            case ATTR_NAME_HARDWARE: {
                out.write(prepareHardwareOptions());
                break;
            }
            case ATTR_NAME_RIGHTS: {
                out.write(serializeObjToJson(new Rights(req)));
                break;
            }
            case ATTR_NAME_LOGOUT: {
                resp.setContentType("text/html");
                Cookie[] cookies = req.getCookies();
                if(cookies != null){
                    for(Cookie cookie : cookies){
                        if(cookie.getName().equals("JSESSIONID")){
                            cookie.setValue("1");
                        }
                        cookie.setMaxAge(0);
                        resp.addCookie(cookie);
                    }
                }
                //invalidate the session if exists
                HttpSession session = req.getSession(false);
                if(session != null){
                    session.invalidate();
                }
                resp.sendRedirect("");
                break;
            }
            case ATTR_NAME_SYSTEM_SHUTDOWN: {
                if (req.isUserInRole(Roles.ROLE_SHUTDOWN)) {
                    Device.getDeviceInstance().shutdown();
                    Device.getDeviceInstance().shutdownSystem();
                    out.write("Ok");
                } else {
                    out.write("denied");
                }
                break;
            }
            default: {
                out.write("bad param " + option);
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
