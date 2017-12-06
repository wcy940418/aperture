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

public class Search extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "type","load_rows", "offset");
            if (request.getString("type").equals("photo")) {
                RpcParser.checkKeys(request, "keyword", "scope", "days");
                JSONArray photos = conn.searchPhoto(request.getInt("user_id"),
                        request.getString("keyword"),
                        request.getString("scope"),
                        request.getInt("days"),
                        request.getInt("load_rows"),
                        request.getInt("offset"));
                response.put("photos", photos);
            } else if (request.getString("type").equals("collection")) {
                RpcParser.checkKeys(request,"keyword");
                JSONArray collections = conn.searchCollection(request.getInt("user_id"),
                        request.getString("keyword"),
                        request.getInt("load_rows"),
                        request.getInt("offset"));
                response.put("collections", collections);
            } else if (request.getString("type").equals("event")) {
                JSONArray events = null;
                if (request.has("keyword")) {
                    events = conn.searchEvent(request.getInt("user_id"),
                            request.getString("keyword"),
                            request.getInt("load_rows"),
                            request.getInt("offset"));
                } else if (request.has("center") && request.has("max_distance")) {
                    events = conn.searchEventByLoc(request.getInt("user_id"),
                            request,
                            request.getInt("load_rows"),
                            request.getInt("offset"));
                } else if (request.has("city") || request.has("street")) {
                    events = conn.searchEventByAddress(request.getInt("user_id"),
                            request,
                            request.getInt("load_rows"),
                            request.getInt("offset"));
                }
                response.put("events", events);
            } else {
                resp.setStatus(400);
                response.put("status", "Unsupported search");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
