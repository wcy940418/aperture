package api;

import db.DBConnection;
import db.MySQLDBConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SignUp extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject request = RpcParser.parseInput(req);
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkKeys(request, "username", "email", "password", "first_name", "last_name");
            if (request.getInt("user_id") != 1) {
                response.put("status", "You have to sign out first and then sign up");
                resp.setStatus(400);
            } else {
                try {
                    int userId = conn.signUp(request.getString("username"),
                            request.getString("email"),
                            request.getString("password"),
                            request.getString("first_name"),
                            request.getString("last_name"));
                    response.put("user_id", userId);
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.setStatus(400);
                    response.put("status", e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
