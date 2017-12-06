package api;

import db.PhotoDBUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;

public class PhotoFileService extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filename = URLDecoder.decode(req.getPathInfo().substring(1), "UTF-8");
        String imageClass = req.getServletPath().split("/")[2];
        String parentDir = null;
        if (imageClass.equals("photo")) {
            parentDir = PhotoDBUtil.fullPhotoPrefix;
        } else if (imageClass.equals("thumb")) {
            parentDir = PhotoDBUtil.thumbnailPrefix;
        } else if (imageClass.equals("avatar")) {
            parentDir = PhotoDBUtil.avatarPrefix;
        } else {
            parentDir = PhotoDBUtil.othersPrefix;
            filename = "404.png";
        }
        File file = new File(parentDir, filename);
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
