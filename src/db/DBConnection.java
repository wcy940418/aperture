package db;

import com.FileException;
import com.IDException;
import org.json.*;
import java.sql.SQLException;

public interface DBConnection {
    /**
     * Close database connection
     */
    public void close();

    /**
     * Sign up a user
     * @param username
     * @param email
     * @param password
     * @return userId
     * @throws IDException e
     * @throws SQLException
     */
    public int signUp(String username, String email, String password) throws IDException, SQLException;

    /**
     * Verify sign in
     * @param usernameEmail
     * @param password
     * @return int userId
     * @throws IDException e
     * @throws SQLException
     */
    public int verifySignIn(String usernameEmail, String password) throws IDException, SQLException;
    /**
     * Get friend list
     * @param userId
     * @return JSONObject of {userId, first_name + last_name}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getFriendList(int userId) throws IDException, SQLException;
    /**
     * Get message list with a specific user
     * @param userId
     * @param friendUserId
     * @param columnCount
     * @param offset
     * @return JSONArray of {messageId, to/from, timeStamp, isRead, content}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getMessageList(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException;
    /**
     * Get invitation list
     * @param userId
     * @return JSONArray(time desc) of {friendUserId, messageId, timeStamp}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getInvitationList(int userId) throws IDException, SQLException;
    /**
     * Get FOFs
     * @param userId
     * @return JSONObject of {userId, first_name + last_name, messageId}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getFOF(int userId) throws IDException, SQLException;
    /**
     * Get photos
     * @param userId
     * @param friendUserId
     * @param columnCount
     * @param offset
     * @return JSONArray of {photoId, uploaderName, category, title, description, lon, lat, country, city, street, zip,
     *                      time_cap, time_upload}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getPhotos(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException;
    /**
     * Get events
     * @param userId
     * @param friendUserId
     * @param columnCount
     * @param offset
     * @return JSONArray(time desc) of {eventId, hostName, title, description, lon, lat, timeCreated, timeHappend, country, city, street, zip}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getEvents(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException;
    /**
     * Create event
     * @param userId
     * @param metadata
     * @return int eventId
     * @throws IDException e
     * @throws SQLException
     */
    public int createEvent(int userId, JSONObject metadata) throws IDException, SQLException;
    /**
     * Edit event
     * @param userId
     * @param eventId
     * @param metadata
     * @throws IDException e
     * @throws SQLException
     */
    public void editEvent(int userId, int eventId, JSONObject metadata) throws IDException, SQLException;

    /**
     * Get event
     * @param userId
     * @param eventId
     * @return eventData
     * @throws IDException
     * @throws SQLException
     */
    public JSONObject getEvent(int userId, int eventId) throws IDException, SQLException;

    /**
     * Upload photo
     * @param userId
     * @param metadata
     * @return int photoId
     * @throws IDException e
     * @throws FileException e
     * @throws SQLException
     */
    public int uploadPhoto(int userId, JSONObject metadata) throws IDException, FileException, SQLException;
    /**
     * Edit photo
     * @param userId
     * @param photoId
     * @param metadata
     * @throws IDException e
     * @throws SQLException
     */
    public void editPhoto(int userId, int photoId, JSONObject metadata) throws IDException, SQLException;
    /**
     * Get photo
     * @param userId
     * @param photoId
     * @return photo metadata
     * @throws IDException
     * @throws SQLException
     */
    public JSONObject getPhoto(int userId, int photoId) throws IDException, SQLException;
    /**
     * Get profile
     * @param userId
     * @param profileUserId
     * @return JSONObject profileData
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getProfile(int userId, int profileUserId) throws IDException, SQLException;
    /**
     * Edit profile
     * @param userId
     * @param profileData
     * @throws IDException e
     * @throws SQLException
     */
    public void editProfile(int userId, JSONObject profileData) throws IDException, SQLException;
    /**
     * Send message
     * @param userId
     * @param toUserId
     * @throws IDException e
     * @throws SQLException
     */
    public void sendMessage(int userId, int toUserId) throws IDException, SQLException;
    /**
     * Send invitation
     * @param userId
     * @param toUserId
     * @throws IDException e
     * @throws SQLException
     */
    public void sendInvitation(int userId, int toUserId) throws IDException, SQLException;
    /**
     * Accept invitation
     * @param messageId
     * @throws IDException e
     * @throws SQLException
     */
    public void acceptInvitation(int messageId) throws IDException, SQLException;
    /**
     * Set friendship visibility
     * @param userId
     * @param friendUserId
     * @param visibility
     * @throws IDException e
     * @throws SQLException
     */
    public void setFriendshipVisibility(int userId, int friendUserId, String visibility) throws IDException, SQLException;
    /**
     * Like photo
     * @param userId
     * @param photoId
     * @throws IDException e
     * @throws SQLException
     */
    public void likePhoto(int userId, int photoId) throws IDException, SQLException;
    /**
     * Create collection
     * @param userId
     * @param metadata
     * @param photoIds
     * @return int collectionId
     * @throws IDException e
     * @throws SQLException
     */
    public int createCollection(int userId, JSONObject metadata, JSONArray photoIds) throws IDException, SQLException;
    /**
     * Edit collection
     * @param userId
     * @param collectionId
     * @param metadata
     * @param changes
     * @throws IDException e
     * @throws SQLException
     */
    public void editCollection(int userId, int collectionId, JSONObject metadata, JSONObject changes) throws IDException, SQLException;
    /**
     * Get collection
     * @param userId
     * @param collectionId
     * @return JSONObject collectionData {}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getCollection(int userId, int collectionId) throws IDException, SQLException;

    /**
     * Get friend's collections
     * @param userId
     * @param friendUserId
     * @param columnCount
     * @param offset
     * @return JSONArray(time desc) of {collectionId, title, timeCreated}
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray getCollections(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException;

    /**
     * Search photo by using keyword
     * @param userId
     * @param keyWord
     * @param scope
     * @param days
     * @return JSONArray of photoData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchPhoto(int userId, String keyWord, String scope, int days) throws IDException, SQLException;

    /**
     * Search event by using keyword in title and description
     * @param userId
     * @param keyWord
     * @return JSONArray of eventData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchEvent(int userId, String keyWord) throws IDException, SQLException;

    /**
     * Search event by using location information
     * @param userId
     * @param location {lon, lat, country, city, street, zip} lon and lat have higher priority
     * @return JSONArray of eventData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchEventByLoc(int userId, JSONObject location) throws IDException, SQLException;

    /**
     * Search collection by using keyword
     * @param userId
     * @param keyWord
     * @return JSONArray of collectionData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchCollection(int userId, String keyWord) throws IDException, SQLException;
}
