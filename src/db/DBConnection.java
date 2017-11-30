package db;

import com.IDException;
import java.util.*;
import javafx.util.Pair;
import org.json.*;

public interface DBConnection {

    public void close();

    /**
     * Sign up a user
     * @param username
     * @param email
     * @param password
     * @return userid
     * @throws IDException
     */
    public int signUp(String username, String email, String password) throws IDException;

    /**
     * Verify sign in
     * @param username_email
     * @param password
     * @return userid
     * @throws IDException
     */
    public int verifySignIn(String username_email, String password) throws IDException;
    /**
     * Get friend list
     * @param userid
     * @return JSONObject of {userid: first_name + last_name}
     * @throws IDException
     */
    public JSONObject getFriendList(int userid) throws IDException;
    /**
     * Get message list with a specific user
     * @param userid
     * @param friend_userid
     * @param column_count
     * @param offset
     * @return JSONArray of {message_id, to/from, time_stamp, is_read, content}
     */
    public JSONObject getMessageList(int userid, int friend_userid, int column_count, int offset) throws IDException;
    /**
     * Get invitation list
     * @param userid
     * @return JSONArray(time desc) of {friend_userid, message_id, time_stamp}
     */
    public JSONArray getInvitationList(int userid) throws IDException;
    /**
     * Get FOFs
     * @param userid
     * @return JSONObject of {userid: first_name + last_name}
     */
    public JSONObject getFOF(int userid) throws IDException;
    /**
     * Get photos
     * @param userid
     * @param friend_userid
     * @param column_count
     * @param offset
     * @return JSONArray of {photo_id, uploader, category, title, description, lon, lat, country, city, street, zip,
     *                      time_cap, time_upload}
     */
    public JSONArray getPhoto(int userid, int friend_userid, int column_count, int offset) throws IDException;

}
