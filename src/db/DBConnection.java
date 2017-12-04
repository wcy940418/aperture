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
    public int signUp(String username, String email, String password, String firstName, String lastName) throws IDException, SQLException;

    /**
     * Change password
     * @param userId
     * @param oldPassword
     * @param newPassword
     * @throws SQLException
     */
    public void changePassword(int userId, String oldPassword, String newPassword) throws SQLException;

    /**
     * Change username
     * @param userId
     * @param newUsername
     * @throws SQLException
     */
    public void changeUsername(int userId, String newUsername) throws SQLException;

    /**
     * Change email
     * @param userId
     * @param newEmail
     * @throws SQLException
     */
    public void changeEmail(int userId, String newEmail) throws SQLException;

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
     * @param toSeeUserId
     * @return JSONArray of {userId, first_name, last_name}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getFriendList(int userId, int toSeeUserId) throws IDException, SQLException;
    /**
     * Get message list with a specific user
     * @param userId
     * @param friendUserId
     * @param rowCount
     * @param offset
     * @return JSONArray of {messageId, to/from, timeStamp, isRead, content}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getMessageList(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException;
    /**
     * Get invitation list
     * @param userId
     * @return JSONArray(time desc) of {messageId, friendUserId, timeStamp}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getInvitationList(int userId) throws IDException, SQLException;
    /**
     * Get FOFs
     * @param userId
     * @return JSONArray of {userId, first_name, last_name}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getFOF(int userId) throws IDException, SQLException;
    /**
     * Get photos
     * @param userId
     * @param friendUserId
     * @param rowCount
     * @param offset
     * @return JSONArray of {photoId, uploaderName, category, title, description, lon, lat, country, city, street, zip,
     *                      time_cap, time_upload}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getPhotos(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException;
    /**
     * Get events
     * @param userId
     * @param friendUserId
     * @param rowCount
     * @param offset
     * @return JSONArray(time desc) of {eventId, hostName, title, description, lon, lat, timeCreated, timeHappend, country, city, street, zip}
     * @throws IDException e
     * @throws SQLException
     */
    public JSONArray getEvents(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException;
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
     * @throws SQLException
     */
    public int uploadPhoto(int userId, JSONObject metadata) throws IDException, SQLException;
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
     * @param toSeeUserId
     * @return JSONObject profileData
     * @throws IDException e
     * @throws SQLException
     */
    public JSONObject getProfile(int userId, int toSeeUserId) throws IDException, SQLException;
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
     * @param message
     * @throws IDException e
     * @throws SQLException
     * @return messageId
     */
    public int sendMessage(int userId, JSONObject message) throws IDException, SQLException;
    /**
     * Send invitation
     * @param userId
     * @param toUserId
     * @param content
     * @throws IDException e
     * @throws SQLException
     * @return invitationId(messageId)
     */
    public int sendInvitation(int userId, int toUserId, String content) throws IDException, SQLException;
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
     * @return int collectionId
     * @throws IDException e
     * @throws SQLException
     */
    public int createCollection(int userId, JSONObject metadata) throws IDException, SQLException;
    /**
     * Edit collection metadata
     * @param userId
     * @param collectionId
     * @param metadata
     * @throws IDException e
     * @throws SQLException
     */
    public void editCollectionMetadata(int userId, int collectionId, JSONObject metadata) throws IDException, SQLException;

    /**
     * Edit collection photos
     * @param userId
     * @param collectionId
     * @param changes
     * @throws SQLException
     */
    public void editCollectionPhotos(int userId, int collectionId, JSONObject changes) throws SQLException;
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
     * @param rowCount
     * @param offset
     * @return JSONArray(time desc) of {collectionId, title, timeCreated}
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray getCollections(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException;

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
    public JSONArray searchPhoto(int userId, String keyWord, String scope, int days, int rowCount, int offset) throws IDException, SQLException;

    /**
     * Search event by using keyword in title and description
     * @param userId
     * @param keyWord
     * @param rowCount
     * @param offset
     * @return JSONArray of eventData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchEvent(int userId, String keyWord, int rowCount, int offset) throws IDException, SQLException;

    /**
     * Search event by using location information
     * @param userId
     * @param location {lon, lat, country, city, street, zip} lon and lat have higher priority
     * @param rowCount
     * @param offset
     * @return JSONArray of eventData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchEventByLoc(int userId, JSONObject location, int rowCount, int offset) throws IDException, SQLException;

    /**
     * Search collection by using keyword
     * @param userId
     * @param keyWord
     * @param rowCount
     * @param offset
     * @return JSONArray of collectionData
     * @throws IDException
     * @throws SQLException
     */
    public JSONArray searchCollection(int userId, String keyWord, int rowCount, int offset) throws IDException, SQLException;

    /**
     * User register event
     * @param userId
     * @param eventId
     * @throws IDException
     * @throws SQLException
     */
    public void registerEvent(int userId, int eventId) throws IDException, SQLException;
}
