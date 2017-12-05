package api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

public class RpcParser {

    public static void checkSignOut(HttpServletRequest req) throws Exception {
        HttpSession session = req.getSession(false);
        if (session != null && req.isRequestedSessionIdValid()) {
            throw new Exception("You have signed in");
        }
    }

    public static void checkSignIn(HttpServletRequest req) throws Exception {
        HttpSession session = req.getSession(false);
        if (session == null || !req.isRequestedSessionIdValid()) {
            throw new Exception("You haven't signed in");
        }
    }

    public static void checkKeys(JSONObject obj, String... keys) throws Exception{
        ArrayList<String> missedKeys = new ArrayList<String>();
        for (String key: keys) {
            if (!obj.has(key)) {
                missedKeys.add(key);
            }
        }
        if (!missedKeys.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(missedKeys.size() == 1 ? "Key" : "Keys");
            for (String key: missedKeys) {
                sb.append(" '");
                sb.append(key);
                sb.append("',");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(missedKeys.size() == 1 ? " misses" : " miss");
            throw new Exception(sb.toString());
        }
    }

    public static JSONObject parseInput(HttpServletRequest req) throws Exception {
        JSONObject requestDict = null;
        HttpSession session = req.getSession(false);
        int userId = session == null || !req.isRequestedSessionIdValid() ? 1 : (int) session.getAttribute("user_id");
        if (req.getMethod().equals("GET")) {
            requestDict = new JSONObject();
            Map<String, String[]> parameters = req.getParameterMap();
            for (Map.Entry<String, String[]> parameter: parameters.entrySet()) {
                if (parameter.getValue().length == 1) {
                    requestDict.put(parameter.getKey(), parameter.getValue()[0]);
                }
            }
        } else if (req.getMethod().equals("POST") || req.getMethod().equals("PUT")) {
            StringBuffer jb = new StringBuffer();
            String line = null;
            try {
                BufferedReader reader = req.getReader();
                while ((line = reader.readLine()) != null) {
                    jb.append(line);
                }
                reader.close();
                requestDict = new JSONObject(jb.toString());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(e.getMessage());
            }
        }
        requestDict.put("user_id", userId);
        requestDict.put("url_prefix", req.getServerName() + ":" + Integer.toString(req.getServerPort()));
        return requestDict;
    }
    public static void writeOutput(HttpServletResponse response, JSONObject obj) {
        try {
            response.setContentType("application/json");
            response.addHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = response.getWriter();
            out.print(obj);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeOutput(HttpServletResponse response, JSONArray array) {
        try {
            response.setContentType("application/json");
            response.addHeader("Access-Control-Allow-Origin", "*");
            PrintWriter out = response.getWriter();
            out.print(array);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
