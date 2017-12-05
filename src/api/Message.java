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

public class Message extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();
    /**
     * Get message list
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
            RpcParser.checkKeys(request, "friend_user_id", "load_rows", "offset");
            JSONArray messages = conn.getMessageList(request.getInt("user_id"),
                    request.getInt("friend_user_id"),
                    request.getInt("load_rows"),
                    request.getInt("offset"));
            response.put("messages", messages);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }

    /**
     * Send a message
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
            RpcParser.checkKeys(request, "to_user_id", "content");
            int messageId = conn.sendMessage(request.getInt("user_id"), request);
            response.put("message_id", messageId);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
