package api;

import db.PhotoDBUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PhotoUploadProcessor extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            String userId = Integer.toString((int) req.getSession().getAttribute("user_id"));
            if (!ServletFileUpload.isMultipartContent(req)) {
                throw new Exception("Upload request is not multipart");
            }
            JSONArray photos = new JSONArray();
            File tempDir = new File(PhotoDBUtil.tempPath);
            DiskFileItemFactory diskCache = new DiskFileItemFactory();
            diskCache.setSizeThreshold(10 * 1024 * 1024);
            diskCache.setRepository(new File(PhotoDBUtil.cachePath));
            ServletFileUpload uploadHandler = new ServletFileUpload(diskCache);
            uploadHandler.setFileSizeMax(100 * 1024 * 1024);
            List<FileItem> uploadFiles = uploadHandler.parseRequest(req);
            for (FileItem item:uploadFiles) {
                if (!item.isFormField()) {
                    File file = File.createTempFile(userId, "photo", tempDir);
                    item.write(file);
                    JSONObject photo = new JSONObject();
                    photo.put("filename", item.getName());
                    photo.put("temp_filename", file.getName());
                    photos.put(photo);
                }
            }
            response.put("photos", photos);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);

    }
}
