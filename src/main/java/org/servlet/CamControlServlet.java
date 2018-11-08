package org.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.Device;
import org.core.device.data.CameraRotationCoords;
import org.core.device.config.HardwareSystemOptions;
import org.core.device.interalServices.InternalServiceManager;
import org.core.device.interalServices.ServiceUids;
import org.core.device.secure.Roles;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jane on 07.01.17.
 */
public class CamControlServlet extends HttpServlet {

    public static final String IR_LIGHT = "irLight";
    public static final String CAM_ACTIVE = "camActive";
    public static final String CAM_ROTATION = "camRotate";
    public static final String CAM_RECORD = "camRecord";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String option = req.getParameter("option");
        final String value = req.getParameter("value");


        PrintWriter out = resp.getWriter();

        if ((option == null) || (option.isEmpty())) {
            throw new RuntimeException("bad param option");
        }

        String rslt = "not supported";

        HardwareSystemOptions options = HardwareSystemOptions.getInstance();

        switch (option) {
            case IR_LIGHT: {
                int val;
                if (req.isUserInRole(Roles.ROLE_IR_CONTROL)) {
                    val = Integer.parseInt(value);
                    Device.getDeviceInstance().setIrLightActive(val > 0);
                } else {
                    val = 0;
                }
                rslt = "" + val;
                break;
            }
            case CAM_ACTIVE: {
                boolean val = Boolean.parseBoolean(value);
                final String camIdString = req.getParameter("cameraId");
                int camId = Integer.parseInt(camIdString);
                rslt = "" + Device.getDeviceInstance().setCameraActive(camId, val, false, req.isUserInRole(Roles.ROLE_CAM_CONTROL));
                break;
            }
            case CAM_ROTATION: {
                if (req.isUserInRole(Roles.ROLE_CAM_CONTROL)) {
                    Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .create();
                    CameraRotationCoords coords = gson.fromJson(value, CameraRotationCoords.class);

                    coords = Device.getDeviceInstance().setCameraRotation(coords);
                    rslt = gson.toJson(coords);
                } else {
                    rslt = "{horiz : 0, vert : 0}";
                }
                break;
            }
            case CAM_RECORD : {
                boolean val = Boolean.parseBoolean(value);
                boolean recRslt = InternalServiceManager.getInstance().setServiceActive(ServiceUids.OPENCV_VIDEO_RECORDER, val);
                if (!val) {
                    rslt = "false";
                } else {
                    rslt = "" + recRslt;
                }
                break;
            }
        }

        out.write(rslt);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }


}
