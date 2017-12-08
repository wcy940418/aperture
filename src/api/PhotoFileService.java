package api;

import db.DBConnection;
import db.MySQLDBConnection;
import db.PhotoDBUtil;
import db.PublicResourceCache;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

public class PhotoFileService extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();
    private static final PublicResourceCache publicRes = PublicResourceCache.getInstance();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filename = URLDecoder.decode(req.getPathInfo().substring(1), "UTF-8");
        String imageClass = req.getServletPath().split("/")[2];
        String parentDir = null;
        File file = null;
        try {
            JSONObject request = RpcParser.parseUrlParm(req);
            if (imageClass.equals("photo")) {
                int photoId = Integer.valueOf(FilenameUtils.removeExtension(filename));
                if (publicRes.isPublicPhoto(photoId) || conn.canView(request.getInt("user_id"), photoId)) {
                    parentDir = PhotoDBUtil.fullPhotoPrefix;
                } else {
                    throw new Exception("Unauthorized to view");
                }
            } else if (imageClass.equals("thumb")) {
                int photoId = Integer.valueOf(FilenameUtils.removeExtension(filename));
                if (publicRes.isPublicPhoto(photoId) || conn.canView(request.getInt("user_id"), photoId)) {
                    parentDir = PhotoDBUtil.thumbnailPrefix;
                } else {
                    throw new Exception("Unauthorized to view");
                }
            } else if (imageClass.equals("avatar")) {
                parentDir = PhotoDBUtil.avatarPrefix;
            } else {
                parentDir = PhotoDBUtil.othersPrefix;
                filename = "404.png";
            }
        } catch (Exception e) {
            parentDir = PhotoDBUtil.othersPrefix;
            filename = "404.png";

        } finally {
            file = new File(parentDir, filename);
            if (!file.canRead()) {
                parentDir = PhotoDBUtil.othersPrefix;
                filename = "404.png";
                file = new File(parentDir, filename);
                if (!file.canRead()) {
                    resp.setStatus(404);
                    return;
                }
            }
            resp.setHeader("Content-Type", getServletContext().getMimeType(filename));
            resp.setHeader("Content-Length", String.valueOf(file.length()));
            resp.setHeader("Content-Disposition", "inline; filename=\"" + "aperture-" + file.getName() + "\"");
            Files.copy(file.toPath(), resp.getOutputStream());
        }
    }
}
