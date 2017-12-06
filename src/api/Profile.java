package api;

import db.DBConnection;
import db.MySQLDBConnection;
import db.PhotoDBUtil;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Profile extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "to_see_user_id");
            JSONObject profile = conn.getProfile(request.getInt("user_id"), request.getInt("to_see_user_id"));
            if (profile.has("user_id")) {
                profile.put("profile_url", request.getString("url_prefix") + "/profile/" +
                        Integer.toString(profile.getInt("user_id")));
                profile.put("avatar_url", request.getString("url_prefix") + "/avatar/" +
                        Integer.toString(profile.getInt("user_id")) + PhotoDBUtil.avatarFormat);
            }
            response = profile;
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "first_name", "last_name", "DOB", "country", "introduction", "gender");
            conn.editProfile(request.getInt("user_id"), request);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
