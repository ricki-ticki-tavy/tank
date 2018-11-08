package org.servlet;

import org.core.device.drive.Calibrator;
import org.core.device.secure.Roles;
import org.core.device.utils.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jane on 15.02.17.
 */
public class CalibrateEnginesServlet extends HttpServlet {

    public static final String CALIBRATE_START = "calibrateStart";
    public static final String CALIBRATE_PAUSE = "calibratePause";
    public static final String CALIBRATE_RESUME = "calibrateResume";
    public static final String CALIBRATE_COMMIT = "calibrateCommit";
    public static final String CALIBRATE_PARTIAL_COMMIT = "calibratePartialCommit";
    public static final String CALIBRATE_ROLLBACK = "calibrateRollback";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String action = req.getParameter("action");

        PrintWriter out = resp.getWriter();

        if (req.isUserInRole(Roles.ROLE_ENGINEER)){
            switch (action) {
                case CALIBRATE_START : {
                    if (!Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().start(3);
                    }
                    out.write(Utils.createJsonAnswer(true, "", ""));
                    break;
                }
                case CALIBRATE_PAUSE : {
                    if (Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().pause();
                        out.write(Utils.createJsonAnswer(true, "", ""));
                    } else {
                        out.write(Utils.createJsonAnswer(false, "", "Not active"));
                    }
                    break;
                }
                case CALIBRATE_RESUME : {
                    if (Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().resume();
                        out.write(Utils.createJsonAnswer(true, "", ""));
                    } else {
                        out.write(Utils.createJsonAnswer(false, "", "Not active"));
                    }
                    break;
                }
                case CALIBRATE_ROLLBACK : {
                    if (Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().rollback();
                        out.write(Utils.createJsonAnswer(true, "", ""));
                    } else {
                        out.write(Utils.createJsonAnswer(false, "", "Not active"));
                    }
                    break;
                }
                case CALIBRATE_COMMIT : {
                    if (Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().commit();
                        out.write(Utils.createJsonAnswer(true, "", ""));
                    } else {
                        out.write(Utils.createJsonAnswer(false, "", "Not active"));
                    }
                    break;
                }
                case CALIBRATE_PARTIAL_COMMIT : {
                    if (Calibrator.getInstance().isActive()){
                        Calibrator.getInstance().partialCommit();
                        out.write(Utils.createJsonAnswer(true, "", ""));
                    } else {
                        out.write(Utils.createJsonAnswer(false, "", "Not active"));
                    }
                    break;
                }
                default:{
                    out.write(Utils.createJsonAnswer(false, "", "Unknown command"));
                }
            }
        } else {
            out.write(Utils.createJsonAnswer(false, "", "Access denied"));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
