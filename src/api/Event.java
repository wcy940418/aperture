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

public class Event extends HttpServlet{
    private static final DBConnection conn = new MySQLDBConnection();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "title", "description", "limitation", "lon", "lat", "country", "city",
                    "street", "zip", "time_happened", "visibility");
            int eventId = conn.createEvent(request.getInt("user_id"), request);
            response.put("event_id", eventId);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            if (request.has("friend_user_id")) {
                RpcParser.checkKeys(request, "friend_user_id", "load_rows", "offset");
                JSONArray events = conn.getEvents(request.getInt("user_id"),
                        request.getInt("friend_user_id"),
                        request.getInt("load_rows"),
                        request.getInt("offset"));
                for (int i = 0; i < events.length(); ++i) {
                    JSONObject event = events.getJSONObject(i);
                    event.put("event_url",
                            request.getString("url_prefix") +
                                    "/event/" +
                                    Integer.toString(event.getInt("event_id")));
                }
                response.put("events", events);
            } else if (request.has("event_id")) {
                RpcParser.checkKeys(request, "event_id");
                JSONObject event = conn.getEvent(request.getInt("user_id"), request.getInt("event_id"));
                if (event.has("event_id")) {
                    event.put("event_url",
                            request.getString("url_prefix") +
                                    "/event/" +
                                    Integer.toString(event.getInt("event_id")));
                    response = event;
                } else {
                    resp.setStatus(400);
                    response.put("status", "No such event");
                }
            }
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
            RpcParser.checkKeys(request, "event_id", "title", "description", "limitation", "lon", "lat",
                    "country", "city", "street", "zip", "time_happened", "visibility");
            conn.editEvent(request.getInt("user_id"),
                    request.getInt("event_id"),
                    request);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }
        RpcParser.writeOutput(resp, response);
    }
}
