package api;

import db.DBConnection;
import db.MySQLDBConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Invitation extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();

    /**
     * Get invitiation list
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
            JSONArray invitations = conn.getInvitationList(request.getInt("user_id"));
            response.put("invitations", invitations);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }

    /**
     * Send invitation / accept invitation
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            if (request.has("message_id")) {
                RpcParser.checkKeys(request, "message_id");
                conn.acceptInvitation(request.getInt("message_id"));
            } else if (request.has("to_user_id")) {
                RpcParser.checkKeys(request, "to_user_id", "content");
                int messageId = conn.sendInvitation(request.getInt("user_id"),
                        request.getInt("to_user_id"),
                        request.getString("content"));
                response.put("message_id", messageId);
            } else {
                resp.setStatus(400);
                response.put("status", "Invalid request type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
