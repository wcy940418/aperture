package api;

import db.DBConnection;
import db.MySQLDBConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SignIn extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();

    /**
     * Sign in
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignOut(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "username_or_email", "password", "remember");
            try {
                int userId = conn.verifySignIn(request.getString("username_or_email"),
                        request.getString("password"));
                HttpSession session = req.getSession();
                session.setAttribute("user_id", userId);
                if (request.getBoolean("remember")) {
                    session.setMaxInactiveInterval(60 * 60 * 24 * 15);
                } else {
                    session.setMaxInactiveInterval(60 * 60);
                }
                response.put("user_id", userId);
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(400);
                response.put("status", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }

    /**
     * Sign in (get status)
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
                RpcParser.checkSignIn(req);
                JSONObject request = RpcParser.parseInput(req);
                response.put("user_id", request.getInt("user_id"));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
