package db;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import com.IDException;
import model.Collection;
import model.Photo;
import model.Event;
import model.Profile;
import org.json.*;

public class MySQLDBConnection implements DBConnection{
    private Connection conn = null;
    public MySQLDBConnection() {
        this(DBUtil.URL);
    }
    public MySQLDBConnection(String url) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String getMD5(String input) {
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            String hashText = bigInt.toString(16);
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    @Override
    public void close() {
        if (conn != null) {
            try{
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int signUp(String username, String email, String password, String firstName, String lastName)
            throws IDException, SQLException {
        if (username == null || email == null || password == null || firstName == null || lastName == null) {
            throw new IDException("Not enough information");
        }
        if (password.length() < 6) {
            throw new IDException("Invalid password");
        }
        if (username.indexOf('@') != -1) {
            throw new IDException("Invalid username");
        }
        if (email.indexOf('@') == -1) {
            throw new IDException("Invalid email address");
        }
        password = getMD5(password);

        try {
            String query = "INSERT INTO User (username, password, email, first_name, last_name) VALUES (?,?,?,?,?)";
            int userId = 0;
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setString(4, firstName);
            statement.setString(5, lastName);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getInt("ID");
            }
            return userId;
        } catch (SQLException e) {
            e.printStackTrace();
            String sqlExceptionStr = e.toString();
            if (sqlExceptionStr.contains("username")) {
                throw new SQLException("Username \'" + username + "\' existed");
            } else if (sqlExceptionStr.contains("email")) {
                throw new SQLException("Email \'" + email + "\' existed");
            }
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int verifySignIn(String usernameEmail, String password) throws IDException, SQLException {
        if (usernameEmail == null || password == null) {
            throw new IDException("Username/Email/Password can not be empty");
        }
        password = getMD5(password);
        String query = null;
        if (usernameEmail.indexOf('@') == -1) {
            query = "SELECT password, ID FROM User WHERE username = ?";
        } else {
            query = "SELECT password, ID FROM User WHERE email = ?";
        }
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, usernameEmail);
            ResultSet rs = statement.executeQuery();
            int userID = 0;
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                userID = rs.getInt("ID");
                if (!password.equals(storedPassword)) {
                    throw new IDException("Wrong password");
                }
                return userID;
            } else {
                throw new IDException("Invalid Username/Email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getPhotos(int userId, int friendUserId, int columnCount, int offset)
            throws IDException, SQLException {

        try {
            String query = "CALL show_photos(?,?,?,?)";
            JSONArray photos = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, columnCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Photo photo = new Photo(rs.getInt("ID"),
                        rs.getInt("upload_by_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"),
                        rs.getTimestamp("time_captured"),
                        rs.getTimestamp("time_uploaded"),
                        rs.getString("category_name")
                        );
                JSONObject photoJSON = photo.toJSONObject();
                photos.put(photoJSON);
            }
            return photos;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getEvents(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL show_events(?,?,?,?)";
            JSONArray events = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, columnCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip")
                );
                JSONObject eventJSON = event.toJSONObject();
                events.put(eventJSON);
            }
            return events;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int uploadPhoto(int userId, JSONObject metadata) throws IDException, SQLException {
        String query = "INSERT INTO Photo " +
                "(upload_by_userID, category_name, title, description, longitude, latitude, " +
                "country, city, street, zip, time_captured, visibility)" +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        DateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);
        Timestamp timeCaptured = new Timestamp((new Date()).getTime());
        int photoId = 0;
        try {
            timeCaptured = new Timestamp(format.parse(metadata.getString("time_captured")).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, metadata.getString("category"));
            statement.setString(3, metadata.getString("title"));
            statement.setString(4, metadata.getString("description"));
            statement.setFloat(5, metadata.getFloat("lon"));
            statement.setFloat(6, metadata.getFloat("lat"));
            statement.setString(7, metadata.getString("country"));
            statement.setString(8, metadata.getString("city"));
            statement.setString(9, metadata.getString("street"));
            statement.setString(10, metadata.getString("zip"));
            statement.setTimestamp(11, timeCaptured);
            statement.setString(12, metadata.getString("visibility"));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                photoId = rs.getInt("ID");
            }
            return photoId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int createCollection(int userId, JSONObject metadata) throws IDException, SQLException {
        String query = "INSERT INTO Collection (host_userID, title, description, visibility) VALUES (?,?,?,?)";
        int collectionId = 0;
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, metadata.getString("title"));
            statement.setString(3, metadata.getString("description"));
            statement.setString(4, metadata.getString("visibility"));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                collectionId = rs.getInt("ID");
            }
            return collectionId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int createEvent(int userId, JSONObject metadata) throws IDException, SQLException {
        String query = "INSERT INTO Event " +
                "(host_userID, title, description, longitude, latitude, limitation, " +
                "country, city, street, zip, time_happened, visibility)" +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        DateFormat format = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);
        Timestamp timeHappened = new Timestamp((new Date()).getTime());
        int eventId = 0;
        try {
            timeHappened = new Timestamp(format.parse(metadata.getString("time_happened")).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, metadata.getString("title"));
            statement.setString(3, metadata.getString("description"));
            statement.setFloat(4, metadata.getFloat("lon"));
            statement.setFloat(5, metadata.getFloat("lat"));
            statement.setInt(6, metadata.getInt("limitation"));
            statement.setString(7, metadata.getString("country"));
            statement.setString(8, metadata.getString("city"));
            statement.setString(9, metadata.getString("street"));
            statement.setString(10, metadata.getString("zip"));
            statement.setTimestamp(11, timeHappened);
            statement.setString(12, metadata.getString("visibility"));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                eventId = rs.getInt("ID");
            }
            return eventId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int sendMessage(int userId, JSONObject message) throws IDException, SQLException {
        String query = "INSERT INTO Message (from_userID, to_userID, type, content)" +
                "VALUES (?,?,?,?)";
        int messageId = 0;
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setInt(2, message.getInt("to_user_id"));
            statement.setString(3, "normal");
            statement.setString(4, message.getString("content"));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                messageId = rs.getInt("ID");
            }
            return messageId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int sendInvitation(int userId, int toUserId, String content) throws IDException, SQLException {
        String query = "INSERT INTO Message (from_userID, to_userID, type, content)" +
                "VALUES (?,?,?,?)";
        int messageId = 0;
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setInt(2, toUserId);
            statement.setString(3, "invitation");
            statement.setString(4, content);
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                messageId = rs.getInt("ID");
            }
            return messageId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void likePhoto(int userId, int photoId) throws IDException, SQLException {
        String query = "INSERT INTO User_like_photo (userID, photoID) VALUES (?,?);";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, photoId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void acceptInvitation(int messageId) throws IDException, SQLException {
        String query = "CALL accept_invitation(?)";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, messageId);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void registerEvent(int userId, int eventId) throws IDException, SQLException {
        String query = "INSERT INTO User_participate_event (eventID, userID) VALUES (?,?)";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, eventId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editEvent(int userId, int eventId, JSONObject metadata) throws IDException, SQLException {
        String query = "UPDATE Event SET title = ?, description = ?, limitation = ?, visibility = ?, time_happened = ?," +
                "longitude = ?, latitude = ?, country = ?, city = ?, street = ?, zip = ? WHERE ID = ?";
        Event event = new Event(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, event.getTitle());
            statement.setString(2, event.getDescription());
            statement.setInt(3, event.getLimitation());
            statement.setString(4, event.getVisibility());
            statement.setTimestamp(5, new Timestamp(event.getTimeHappened().getTime()));
            statement.setFloat(6, event.getLon());
            statement.setFloat(7, event.getLat());
            statement.setString(8, event.getCountry());
            statement.setString(9, event.getCity());
            statement.setString(10, event.getStreet());
            statement.setString(11, event.getZip());
            statement.setInt(12, eventId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editPhoto(int userId, int photoId, JSONObject metadata) throws IDException, SQLException {
        String query = "UPDATE Photo SET category_name = ?, title = ?, description = ?, longitude = ?, country = ?," +
                "city = ?, street = ?, zip = ?, time_captured = ?, visibility = ? WHERE ID = ?";
        Photo photo = new Photo(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, photo.getCategory());
            statement.setString(2, photo.getTitle());
            statement.setString(3, photo.getDescription());
            statement.setFloat(4, photo.getLon());
            statement.setFloat(5, photo.getLat());
            statement.setString(6, photo.getCountry());
            statement.setString(7, photo.getStreet());
            statement.setString(8, photo.getZip());
            statement.setTimestamp(9, new Timestamp(photo.getTimeCaptured().getTime()));
            statement.setString(10, photo.getVisibility());
            statement.setInt(11, photoId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editCollectionMetadata(int userId, int collectionId, JSONObject metadata) throws IDException, SQLException {
        String query = "UPDATE Collection SET title = ?, description = ?, visibility = ? WHERE ID = ?";
        Collection collection = new Collection(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, collection.getTitle());
            statement.setString(2, collection.getDescription());
            statement.setString(3, collection.getVisibility());
            statement.setInt(4, collection.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editProfile(int userId, JSONObject profileData) throws IDException, SQLException {
        String query = "UPDATE User SET first_name = ?, last_name = ?, DOB = ?, country = ?," +
                "introduction = ?, gender = ? WHERE ID = ?";
        Profile profile = new Profile(profileData);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            statement.setDate(3, new java.sql.Date(profile.getDOB().getTime()));
            statement.setString(4, profile.getCountry());
            statement.setString(5, profile.getIntroduction());
            statement.setString(6, profile.getGender());
            statement.setInt(7, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void setFriendshipVisibility(int userId, int friendUserId, String visibility) throws IDException, SQLException {
        String forUser = null;
        if (userId > friendUserId) {
            forUser = "visibility_user2";
        } else {
            forUser = "visibility_user1";
        }
        String query = "UPDATE Friendship SET " + forUser + " = ? WHERE user1_ID = ? AND user2_ID = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, visibility);
            if (userId > friendUserId) {
                statement.setInt(2, friendUserId);
                statement.setInt(3, userId);
            } else {
                statement.setInt(2, userId);
                statement.setInt(3, friendUserId);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editCollectionPhotos(int userId, int collectionId, JSONObject changes) throws SQLException {
        String queryAdd = "INSERT INTO Collection_photo (collectionID, photoID) VALUES (?,?)";
        String queryDel = "DELETE FROM Collection_photo WHERE collectionID = ?, photoID = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(queryAdd);
            JSONArray addArr = changes.getJSONArray("add");
            for (int i = 0; i < addArr.length(); ++i) {
                statement.setInt(1, collectionId);
                statement.setInt(2, addArr.getInt(i));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
        try {
            PreparedStatement statement = conn.prepareStatement(queryDel);
            JSONArray delArr = changes.getJSONArray("del");
            for (int i = 0; i < delArr.length(); ++i) {
                statement.setInt(1, collectionId);
                statement.setInt(2, delArr.getInt(i));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void changePassword(int userId, String oldPassword, String newPassword) throws SQLException {
        String query = "UPDATE User SET password = ? WHERE ID = ? AND password = ?";
        try {
            oldPassword = getMD5(oldPassword);
            newPassword = getMD5(newPassword);
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, newPassword);
            statement.setInt(2, userId);
            statement.setString(3, oldPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void changeUsername(int userId, String newUsername) throws SQLException {
        String query = "UPDATE User SET username = ? WHERE ID = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, newUsername);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void changeEmail(int userId, String newEmail) throws SQLException {
        String query = "UPDATE User SET email = ? WHERE ID = ?";
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, newEmail);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getFriendList(int userId, int toSeeUserId) throws IDException, SQLException {
        String query = "CALL show_friends(?,?)";
        JSONArray friendList = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, toSeeUserId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject person = new JSONObject();
                person.put("id", rs.getInt("ID"));
                person.put("first_name", rs.getString("first_name"));
                person.put("last_name", rs.getString("last_name"));
                friendList.put(person);
            }
            return friendList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getMessageList(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException {
        String query = "SELECT ID, from_userID, time_stamp, is_read, content FROM Message" +
                "WHERE (from_userID = ? AND to_userID = ?) OR (to_userID = ? AND from_userID = ?)" +
                "ORDER BY time_stamp DESC" +
                "LIMIT ?, ?";
        try {
            DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);
            JSONArray messageList = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, userId);
            statement.setInt(4, friendUserId);
            statement.setInt(5, offset);
            statement.setInt(6, columnCount);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject message = new JSONObject();
                message.put("id", rs.getInt("ID"));
                message.put("to_from", rs.getInt("from_userID") == userId ? "from" : "to");
                message.put("time_stamp", dateFormat.format(rs.getTimestamp("time_stamp")));
                message.put("is_read", rs.getBoolean("is_read"));
                message.put("content", rs.getString("content"));
                messageList.put(message);
            }
            return messageList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getInvitationList(int userId) throws IDException, SQLException {
        String query = "SELECT ID, to_userID, time_stamp FROM Message" +
                "WHERE (from_userID = ? OR to_userID = ?) AND is_read = FALSE";
        try {
            DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);
            JSONArray invitationList = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject invitation = new JSONObject();
                invitation.put("id", rs.getInt("ID"));
                invitation.put("to", rs.getInt("to_userID"));
                invitation.put("time_stamp", dateFormat.format(rs.getTimestamp("time_stamp")));
                invitation.put("content", rs.getString("content"));
                invitationList.put(invitation);
            }
            return invitationList;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONObject getPhoto(int userId, int photoId) throws IDException, SQLException {
        String query = "CALL show_photo(?,?)";
        JSONObject photoJSON = new JSONObject();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, photoId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Photo photo = new Photo(rs.getInt("ID"),
                        rs.getInt("upload_by_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"),
                        rs.getTimestamp("time_captured"),
                        rs.getTimestamp("time_uploaded"),
                        rs.getString("category_name")
                );
                photoJSON = photo.toJSONObject();
            }
            return photoJSON;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getFOF(int userId) throws IDException, SQLException {
        String query = "CALL show_FOF(?)";
        JSONArray FOFArr = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject FOF = new JSONObject();
                FOF.put("id", rs.getInt("ID"));
                FOF.put("first_name", rs.getString("first_name"));
                FOF.put("last_name", rs.getString("last_name"));
                FOFArr.put(FOF);
            }
            return FOFArr;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONObject getEvent(int userId, int eventId) throws IDException, SQLException {
        String query = "CALL show_event(?,?)";
        JSONObject eventJSON = new JSONObject();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, eventId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"));
                eventJSON = event.toJSONObject();
            }
            return eventJSON;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONObject getCollection(int userId, int collectionId) throws IDException, SQLException {
        String query = "CALL show_collection(?,?)";
        String queryPhotos = "SELECT photoID FROM Collection_photo WHERE collectionID = ?";
        JSONObject collectionJSON = new JSONObject();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, collectionId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                PreparedStatement statementPhotos = conn.prepareStatement(queryPhotos);
                ResultSet rsPhotos = statementPhotos.executeQuery();
                HashSet<Integer> photos = new HashSet<Integer>();
                while (rsPhotos.next()) {
                    photos.add(rsPhotos.getInt("photoID"));
                }
                Collection collection = new Collection(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("visibility"),
                        rs.getString("description"),
                        rs.getTimestamp("time_created"),
                        photos);
                collectionJSON = collection.toJSONObject();
            }
            return collectionJSON;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray getCollections(int userId, int friendUserId, int columnCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL show_collections(?,?,?,?)";
            JSONArray collections = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, columnCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Collection collection = new Collection(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("visibility"),
                        rs.getString("description"),
                        rs.getTimestamp("time_created")
                );
                JSONObject collectionJSON = collection.toJSONObject();
                collections.put(collectionJSON);
            }
            return collections;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONObject getProfile(int userId, int toSeeUserId) throws IDException, SQLException {
        String query = "CALL show_profile(?,?)";
        JSONObject profileJSON = new JSONObject();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, toSeeUserId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Profile profile = new Profile(
                        rs.getInt("ID "),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getDate("DOB"),
                        rs.getTimestamp("lastAccessTime"),
                        rs.getString("country"),
                        rs.getString("introduction"),
                        rs.getString("gender")
                );
                profileJSON = profile.toJSONObject();
            }
            return profileJSON;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray searchPhoto(int userId, String keyWord, String scope, int days, int columnCount, int offset)
            throws IDException, SQLException {
        String query = "CALL search_photos(?,?,?,?,?,?)";
        JSONArray photos = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setString(3, scope);
            statement.setInt(4, days);
            statement.setInt(5, columnCount);
            statement.setInt(6, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Photo photo = new Photo(rs.getInt("ID"),
                        rs.getInt("upload_by_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"),
                        rs.getTimestamp("time_captured"),
                        rs.getTimestamp("time_uploaded"),
                        rs.getString("category_name")
                );
                JSONObject photoJSON = photo.toJSONObject();
                photos.put(photoJSON);
            }
            return photos;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray searchEvent(int userId, String keyWord, int columnCount, int offset) throws IDException, SQLException {
        String query = "CALL search_events(?,?,?,?)";
        JSONArray events = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setInt(3, columnCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip")
                );
                JSONObject eventJSON = event.toJSONObject();
                events.put(eventJSON);
            }
            return events;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray searchCollection(int userId, String keyWord, int columnCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL search_collections(?,?,?,?)";
            JSONArray collections = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setInt(3, columnCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Collection collection = new Collection(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("visibility"),
                        rs.getString("description"),
                        rs.getTimestamp("time_created")
                );
                JSONObject collectionJSON = collection.toJSONObject();
                collections.put(collectionJSON);
            }
            return collections;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public JSONArray searchEventByLoc(int userId, JSONObject location, int columnCount, int offset) throws IDException, SQLException {
        String query = "CALL search_local_events(?,?,?,?,?)";
        JSONArray events = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, location.getString("search_city"));
            statement.setString(3, location.getString("search_street"));
            statement.setInt(4, columnCount);
            statement.setInt(5, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip")
                );
                JSONObject eventJSON = event.toJSONObject();
                events.put(eventJSON);
            }
            return events;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }
}
