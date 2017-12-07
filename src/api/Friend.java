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

public class Friend extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();

    /**
     * Set friend visibility
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "friend_user_id", "visibility");
            conn.setFriendshipVisibility(request.getInt("user_id"),
                    request.getInt("friend_user_id"),
                    request.getString("visibility"));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }

    /**
     * Get friend list
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
            RpcParser.checkKeys(request, "to_see_user_id");
            JSONArray friends = conn.getFriendList(request.getInt("user_id"),
                    request.getInt("to_see_user_id"));
            response.put("friends", friends);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
