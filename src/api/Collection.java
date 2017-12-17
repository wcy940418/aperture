package api;

import db.DBConnection;
import db.MySQLDBConnection;
import db.PublicResourceCache;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Collection extends HttpServlet {
    private static final DBConnection conn = new MySQLDBConnection();
    private static final PublicResourceCache pubRes = PublicResourceCache.getInstance();
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "title", "description", "visibility");
            int collectionId = conn.createCollection(request.getInt("user_id"), request);
            if (request.getString("visibility").equals("Public")) {
                pubRes.addPublicCollection(collectionId);
            }
            response.put("collection_id", collectionId);
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
                JSONArray collections = conn.getCollections(request.getInt("user_id"),
                        request.getInt("friend_user_id"),
                        request.getInt("load_rows"),
                        request.getInt("offset"));
                for (int i = 0; i < collections.length(); ++i) {
                    JSONObject collection = collections.getJSONObject(i);
                    collection.put("host_name", pubRes.getUserName(collection.getInt("host_id")));
                    collection.put("collection_url",
                            request.getString("url_prefix") +
                                    "/collection/" +
                                    Integer.toString(collection.getInt("collection_id")));
                }
                response.put("collections", collections);
            } else if (request.has("collection_id")) {
                RpcParser.checkKeys(request, "collection_id");
                JSONObject collection = conn.getCollection(request.getInt("user_id"), request.getInt("collection_id"));
                if (collection.has("collection_id")) {
                    collection.put("host_name", pubRes.getUserName(collection.getInt("host_id")));
                    collection.put("collection_url",
                            request.getString("url_prefix") +
                                    "/collection/" +
                                    Integer.toString(collection.getInt("collection_id")));
                    response = collection;
                } else {
                    resp.setStatus(400);
                    response.put("status", "No such collection");
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONObject response = new JSONObject();
        try {
            RpcParser.checkSignIn(req);
            JSONObject request = RpcParser.parseInput(req);
            RpcParser.checkKeys(request, "edit_type","collection_id");
            if (request.getString("edit_type").equals("metadata")) {
                RpcParser.checkKeys(request, "title", "description", "visibility");
                conn.editCollectionMetadata(request.getInt("user_id"), request.getInt("collection_id"), request);
                if (request.getString("visibility").equals("Public")) {
                    pubRes.addPublicCollection(request.getInt("collection_id"));
                } else {
                    pubRes.delPublicCollection(request.getInt("collection_id"));
                }
            } else if (request.getString("edit_type").equals("photos")) {
                conn.editCollectionPhotos(request.getInt("user_id"), request.getInt("collection_id"), request);
            } else {
                resp.setStatus(400);
                response.put("status", "Unsupported method");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(400);
            response.put("status", e.getMessage());
        }

        RpcParser.writeOutput(resp, response);
    }
}
