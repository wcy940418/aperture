package api;

import db.DBConnection;
import db.MySQLDBConnection;
import db.PhotoDBUtil;
import org.imgscalr.Scalr;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Avatar extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "temp_filename");
            File file = new File(PhotoDBUtil.tempPath, request.getString("temp_filename"));
            if (!file.canRead()) {
                throw new Exception("Unable to find uploaded file");
            }
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                BufferedImage thumb = Scalr.resize(img, 300);
                File thumbFile = new File (PhotoDBUtil.avatarPrefix,
                        Integer.toString(request.getInt("user_id")) + PhotoDBUtil.avatarFormat);
                ImageIO.write(thumb, PhotoDBUtil.avatarFormat.substring(1), thumbFile);
            }
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
