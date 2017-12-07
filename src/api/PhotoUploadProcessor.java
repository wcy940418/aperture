package api;

import db.PhotoDBUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.imgscalr.Scalr;
import org.json.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.Buffer;
import java.util.List;

public class PhotoUploadProcessor extends HttpServlet{

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
//            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseUrlParm(req);
            RpcParser.checkKeys(request, "del_photo");
            JSONArray photos = new JSONArray();
            File fullPhotoFile = new File(PhotoDBUtil.tempPath, request.getString("del_photo"));
            if (fullPhotoFile.exists()) {
                boolean status = fullPhotoFile.delete();
                JSONObject photo = new JSONObject();
                photo.put(request.getString("del_photo"), status);
                photos.put(photo);
            }
            response.put("files", photos);
            RpcParser.writeOutput(resp, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getParameter("thumb") != null && !req.getParameter("thumb").isEmpty()) {
            File file = new File(PhotoDBUtil.tempPath, req.getParameter("thumb"));
            if (file.exists()) {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    BufferedImage thumb = Scalr.resize(img, 300);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ImageIO.write(thumb, "png", outputStream);
                    resp.setContentType("image/png");
                    ServletOutputStream sos = resp.getOutputStream();
                    resp.setContentLength(outputStream.size());
                    resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"" );
                    outputStream.writeTo(sos);
                    sos.flush();
                    sos.close();
                }
            }
        } else {
            PrintWriter writer = resp.getWriter();
            writer.write("Unsupported get method");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
//            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseUrlParm(req);
            String userPrefix = "user-" + Integer.toString(request.getInt("user_id")) + "-";
            if (!ServletFileUpload.isMultipartContent(req)) {
                throw new Exception("Upload request is not multipart");
            }
            JSONArray photos = new JSONArray();
            File photoDir = new File(PhotoDBUtil.tempPath);
            DiskFileItemFactory diskCache = new DiskFileItemFactory();
            diskCache.setSizeThreshold(10 * 1024 * 1024);
            diskCache.setRepository(new File(PhotoDBUtil.cachePath));
            ServletFileUpload uploadHandler = new ServletFileUpload(diskCache);
            uploadHandler.setFileSizeMax(100 * 1024 * 1024);
            List<FileItem> uploadFiles = uploadHandler.parseRequest(req);
            for (FileItem item:uploadFiles) {
                if (!item.isFormField()) {
                    File file = File.createTempFile(userPrefix, PhotoDBUtil.fullPhotoFormat, photoDir);
                    JSONObject photo = new JSONObject();
                    photo.put("name", item.getName());
                    photo.put("size", item.getSize());
                    try {
                        item.write(file);
                        photo.put("thumbnailUrl", request.getString("url_prefix") +
                                req.getServletPath() +
                                "?thumb=" +
                                file.getName());
                        photo.put("deleteUrl", request.getString("url_prefix") +
                                req.getServletPath() +
                                "?del_photo=" +
                                file.getName());
                        photo.put("deleteType", "DELETE");
                    } catch (Exception e) {
                        photo.put("error", "Unable to write file");
                    }
                    photo.put("temp_filename", file.getName());
                    photos.put(photo);
                }
            }
            response.put("files", photos);
            RpcParser.writeOutput(resp, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
