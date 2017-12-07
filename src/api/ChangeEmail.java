package api;

import db.DBConnection;
import db.MySQLDBConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChangeEmail extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();

    /**
     * Change email
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
            RpcParser.checkKeys(request, "email");
            conn.changeEmail(request.getInt("user_id"), request.getString("email"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
