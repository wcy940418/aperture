package api;

import db.*;
import org.imgscalr.Scalr;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Photo extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();
    private static final PublicResourceCache pubRes = PublicResourceCache.getInstance();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "category", "title", "description", "lon", "lat", "country", "state",
                    "city", "street", "zip", "time_captured", "visibility", "temp_filename");
            int photoId = conn.uploadPhoto(request.getInt("user_id"), request);
            if (request.getString("visibility").equals("Public")) {
                pubRes.addPublicPhoto(photoId);
            }
            File file = new File(PhotoDBUtil.tempPath, request.getString("temp_filename"));
            if (!file.canRead()) {
                throw new Exception("Unable to find uploaded file");
            }
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                BufferedImage thumb = Scalr.resize(img, 300);
                File thumbFile = new File (PhotoDBUtil.thumbnailPrefix,
                        Integer.toString(photoId) + PhotoDBUtil.thumbnailFormat);
                ImageIO.write(thumb, PhotoDBUtil.thumbnailFormat.substring(1), thumbFile);
            }
            file.renameTo(new File(PhotoDBUtil.fullPhotoPrefix,
                    Integer.toString(photoId) + PhotoDBUtil.fullPhotoFormat));
            response.put("photo_id", photoId);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            if (request.has("friend_user_id")) {
                RpcParser.checkKeys(request, "friend_user_id", "load_rows", "offset");
                JSONArray photos = conn.getPhotos(request.getInt("user_id"),
                        request.getInt("friend_user_id"),
                        request.getInt("load_rows"),
                        request.getInt("offset"));
                for (int i = 0; i < photos.length(); ++i) {
                    JSONObject photo = photos.getJSONObject(i);
                    photo.put("full_url",
                            request.getString("url_prefix") +
                            "/file/photo/" +
                            Integer.toString(photo.getInt("photo_id")) +
                            PhotoDBUtil.fullPhotoFormat);
                    photo.put("thumbnail_url",
                            request.getString("url_prefix") +
                            "/file/thumb/" +
                            Integer.toString(photo.getInt("photo_id")) +
                            PhotoDBUtil.thumbnailFormat);
                }
                response.put("photos", photos);
            } else if (request.has("photo_id")) {
                RpcParser.checkKeys(request, "photo_id");
                JSONObject photo = conn.getPhoto(request.getInt("user_id"), request.getInt("photo_id"));
                if (photo.has("photo_id")) {
                    photo.put("full_url",
                            request.getString("url_prefix") +
                                    "/file/photo/" +
                                    Integer.toString(photo.getInt("photo_id")) +
                                    PhotoDBUtil.fullPhotoFormat);
                    photo.put("thumbnail_url",
                            request.getString("url_prefix") +
                                    "/file/thumb/" +
                                    Integer.toString(photo.getInt("photo_id")) +
                                    PhotoDBUtil.thumbnailFormat);
                    response = photo;
                } else {
                    resp.setStatus(400);
                    response.put("status", "No such photo");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "photo_id", "category", "title", "description", "lon", "lat", "country",
                    "state", "city", "street", "zip", "time_captured", "visibility");
            conn.editPhoto(request.getInt("user_id"),
                    request.getInt("photo_id"),
                    request);
            if (request.getString("visibility").equals("Public")) {
                pubRes.addPublicPhoto(request.getInt("photo_id"));
            } else {
                pubRes.delPublicPhoto(request.getInt("photo_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
