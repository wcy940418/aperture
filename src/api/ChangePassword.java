package api;

import db.DBConnection;
import db.MySQLDBConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChangePassword extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject request = RpcParser.parseInput(req);
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            RpcParser.checkKeys(request, "old_password", "new_password");
            conn.changePassword(request.getInt("user_id"),
                    request.getString("old_password"),
                    request.getString("new_password"));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
