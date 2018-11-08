package org.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.core.device.config.ManualConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by jane on 16.04.17.
 */
public class VideoRecordsServlet extends HttpServlet {

    private static final String GET_RECORDS_LIST = "getRecordsList";
    private static final String DELETE_RECORD = "deleteRecord";
    private static final String DOWNLOAD_RECORD = "downloadRecord";

    private String getRecordsList() {
        Set<RecordInfo> data = new TreeSet();
        File recordsCatalog = new File(ManualConfig.getSettings().openCvVideoRecorderFilePath);
        for (File foundFile : recordsCatalog.listFiles()){
            if (foundFile.isFile() && (foundFile.getName().startsWith("record"))) {
                data.add(new RecordInfo(foundFile.getName().replace("record", ""), "00:00"));
            }
        }
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(data);
    }

    private void deleteRecord(String recordName){
        File fileToDelete = new File(ManualConfig.getSettings().openCvVideoRecorderFilePath + "/record" + recordName);
        if (fileToDelete.exists()){
            fileToDelete.delete();
        }
    }

    private void downloadFile(String recordName, HttpServletResponse resp){
        try {
            File downloadFile = new File(ManualConfig.getSettings().openCvVideoRecorderFilePath + "/record" + recordName);
            FileInputStream inStream = new FileInputStream(downloadFile);

            // obtains ServletContext
//            ServletContext context = getServletContext();

            // gets MIME type of the file
//            String mimeType = context.getMimeType(downloadFile.getName());
//            if (mimeType == null) {
//                // set to binary type if MIME mapping not found
//                mimeType = "application/octet-stream";
//            }

            // modifies response
            resp.setContentType("video/mp4");
            resp.setContentLength((int) downloadFile.length());

            // forces download
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName() + ".mp4");
            resp.setHeader(headerKey, headerValue);

            // obtains response's output stream
            OutputStream outStream = resp.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }

            inStream.close();
            outStream.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String action = req.getParameter("action");
        final String value = req.getParameter("value");

        switch (action) {
            case GET_RECORDS_LIST: {
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.write(getRecordsList());
                break;
            }
            case DELETE_RECORD: {
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                deleteRecord(value);
                out.write("OK");
                break;
            }
            case DOWNLOAD_RECORD: {
                downloadFile(value, resp);
                break;
            }
        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    class RecordInfo implements Comparable<RecordInfo> {
        public String name;
        public String len;

        public RecordInfo(String name, String len) {
            this.name = name;
            this.len = len;
        }

        @Override
        public int compareTo(RecordInfo o) {
            return name.compareTo(o.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
