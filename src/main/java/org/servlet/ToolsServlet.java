package org.servlet;

import org.core.device.config.ManualConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by jane on 07.02.17.
 */
public class ToolsServlet extends HttpServlet {

    public static final String READ_CONFIG = "readConfig";
    public static final String WRITE_CONFIG = "writeConfig";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String action = req.getParameter("action");

        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        switch (action) {
            case READ_CONFIG: {
                out.write(ManualConfig.getInstance().getAsJson());
                break;
            }
            case WRITE_CONFIG: {
                String data = req.getParameter("data");
                ManualConfig.getInstance().setFromJson(data);
                out.write(ManualConfig.getInstance().getAsJson());
                break;
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
