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

        try {
            String query = "INSERT INTO User (username, password, email, first_name, last_name) VALUES (?,MD5(?),?,?,?)";
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
                userId = rs.getInt(1);
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
    public JSONArray getPhotos(int userId, int friendUserId, int rowCount, int offset)
            throws IDException, SQLException {

        try {
            String query = "CALL show_photos(?,?,?,?)";
            JSONArray photos = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, rowCount);
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
                        rs.getString("category_name"),
                        rs.getInt("likes")
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
    public JSONArray getEvents(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL show_events(?,?,?,?)";
            JSONArray events = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, rowCount);
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
                "country, city, street, zip, time_captured, visibility) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        Photo photo = new Photo(metadata);
        int photoId = 0;
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, photo.getCategory());
            statement.setString(3, photo.getTitle());
            statement.setString(4, photo.getDescription());
            if (photo.getLon() == null) {
                statement.setNull(5, Types.FLOAT);
            } else {
                statement.setFloat(5, photo.getLon());
            }
            if (photo.getLat() == null) {
                statement.setNull(6, Types.FLOAT);
            } else {
                statement.setFloat(6, photo.getLat());
            }
            statement.setString(7, photo.getCountry());
            statement.setString(8, photo.getCity());
            statement.setString(9, photo.getStreet());
            statement.setString(10, photo.getZip());
            if (photo.getTimeCaptured() == null) {
                statement.setNull(11, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(11, new Timestamp(photo.getTimeCaptured().getTime()));
            }
            statement.setString(12, photo.getVisibility());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                photoId = rs.getInt(1);
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
        Collection collection = new Collection(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, collection.getTitle());
            statement.setString(3, collection.getDescription());
            statement.setString(4, collection.getVisibility());
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                collectionId = rs.getInt(1);
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
                "country, city, street, zip, time_happened, visibility) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        int eventId = 0;
        Event event = new Event(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, userId);
            statement.setString(2, event.getTitle());
            statement.setString(3, event.getDescription());
            if (event.getLon() == null) {
                statement.setNull(4, Types.FLOAT);
            } else {
                statement.setFloat(4, metadata.getFloat("lon"));
            }
            if (event.getLat() == null) {
                statement.setNull(5, Types.FLOAT);
            } else {
                statement.setFloat(5, metadata.getFloat("lat"));
            }
            if (event.getLimitation() == null) {
                statement.setNull(6, Types.INTEGER);
            } else {
                statement.setInt(6, metadata.getInt("limitation"));
            }
            statement.setString(7, metadata.getString("country"));
            statement.setString(8, metadata.getString("city"));
            statement.setString(9, metadata.getString("street"));
            statement.setString(10, metadata.getString("zip"));
            if (event.getTimeHappened() == null) {
                statement.setNull(11, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(11, new Timestamp(event.getTimeHappened().getTime()));
            }
            statement.setString(12, metadata.getString("visibility"));
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                eventId = rs.getInt(1);
            }
            return eventId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int sendMessage(int userId, JSONObject message) throws IDException, SQLException {
        String query = "INSERT INTO Message (from_userID, to_userID, type, content) " +
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
                messageId = rs.getInt(1);
            }
            return messageId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public int sendInvitation(int userId, int toUserId, String content) throws IDException, SQLException {
        String query = "INSERT INTO Message (from_userID, to_userID, type, content) " +
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
                messageId = rs.getInt(1);
            }
            return messageId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void likePhoto(int userId, int photoId) throws IDException, SQLException {
        String query = "INSERT INTO User_like_photo (userID, photoID) VALUES (?,?)";
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
            statement.setInt(1, eventId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
    }

    @Override
    public void editEvent(int userId, int eventId, JSONObject metadata) throws IDException, SQLException {
        String query = "UPDATE Event SET title = ?, description = ?, limitation = ?, visibility = ?, " +
                "time_happened = ?, longitude = ?, latitude = ?, country = ?, city = ?, street = ?, zip = ? " +
                "WHERE ID = ?";
        Event event = new Event(getEvent(userId, eventId));
        event.editEvent(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, event.getTitle());
            statement.setString(2, event.getDescription());
            if (event.getLimitation() == null) {
                statement.setNull(3, Types.INTEGER);
            } else {
                statement.setInt(3, event.getLimitation());
            }
            statement.setString(4, event.getVisibility());
            if (event.getTimeHappened() == null) {
                statement.setNull(5, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, new Timestamp(event.getTimeHappened().getTime()));
            }
            if (event.getLon() == null) {
                statement.setNull(6, Types.FLOAT);
            } else {
                statement.setFloat(6, event.getLon());
            }
            if (event.getLat() == null) {
                statement.setNull(7, Types.FLOAT);
            } else {
                statement.setFloat(7, event.getLat());
            }
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
        String query = "UPDATE Photo SET category_name = ?, title = ?, description = ?, longitude = ?, latitude = ?, " +
                "country = ?, city = ?, street = ?, zip = ?, time_captured = ?, visibility = ? WHERE ID = ?";
        Photo photo = new Photo(getPhoto(userId, photoId));
        photo.editPhotoInfo(metadata);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, photo.getCategory());
            statement.setString(2, photo.getTitle());
            statement.setString(3, photo.getDescription());
            if (photo.getLon() == null) {
                statement.setNull(4, Types.FLOAT);
            } else {
                statement.setFloat(4, photo.getLon());
            }
            if (photo.getLat() == null) {
                statement.setNull(5, Types.FLOAT);
            } else {
                statement.setFloat(5, photo.getLat());
            }
            statement.setString(6, photo.getCountry());
            statement.setString(7, photo.getCity());
            statement.setString(8, photo.getStreet());
            statement.setString(9, photo.getZip());
            if (photo.getTimeCaptured() == null) {
                statement.setNull(10, Types.TIMESTAMP);
            } else {
                statement.setTimestamp(10, new Timestamp(photo.getTimeCaptured().getTime()));
            }
            statement.setString(11, photo.getVisibility());
            statement.setInt(12, photoId);
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
        String query = "UPDATE User SET first_name = ?, last_name = ?, DOB = ?, country = ?, " +
                "introduction = ?, gender = ? WHERE ID = ?";
        Profile profile = new Profile(getProfile(userId, userId));
        profile.editProfile(profileData);
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            if (profile.getDOB() == null) {
                statement.setNull(3, Types.DATE);
            } else {
                statement.setDate(3, new java.sql.Date(profile.getDOB().getTime()));
            }
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
        String queryAdd = "INSERT IGNORE INTO Collection_photo (collectionID, photoID) VALUES (?,?)";
        String queryDel = "DELETE FROM Collection_photo WHERE collectionID = ? AND photoID = ?";
        String queryAuth = "SELECT is_own_collection(?,?)";
        boolean authorized = true;
        try {
            PreparedStatement statement = conn.prepareStatement(queryAuth);
            statement.setInt(1, userId);
            statement.setInt(2, collectionId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                authorized = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Internal error");
        }
        if (!authorized) {
            throw new SQLException("Unauthorized modification");
        }
        if (changes.has("add")) {
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
        }
        if (changes.has("del")) {
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
                person.put("user_id", rs.getInt("ID"));
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
    public JSONArray getMessageList(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException {
        String query = "SELECT ID, from_userID, time_stamp, is_read, content FROM Message " +
                "WHERE ((from_userID = ? AND to_userID = ?) OR (to_userID = ? AND from_userID = ?)) " +
                "AND type = 'normal' " +
                "ORDER BY time_stamp DESC " +
                "LIMIT ?, ?";
        try {
            System.out.println(userId);
            System.out.println(friendUserId);
            System.out.println(rowCount);
            System.out.println(offset);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            JSONArray messageList = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, userId);
            statement.setInt(4, friendUserId);
            statement.setInt(5, offset);
            statement.setInt(6, rowCount);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject message = new JSONObject();
                message.put("message_id", rs.getInt("ID"));
                message.put("to_from", rs.getInt("from_userID") == userId ? "to" : "from");
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
        String query = "SELECT ID, from_userID, time_stamp, content FROM Message " +
                "WHERE to_userID = ? AND is_read = FALSE AND type = 'invitation'";
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            JSONArray invitationList = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                JSONObject invitation = new JSONObject();
                invitation.put("message_id", rs.getInt("ID"));
                invitation.put("from_user_id", rs.getInt("from_userID"));
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
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"),
                        rs.getTimestamp("time_captured"),
                        rs.getTimestamp("time_uploaded"),
                        rs.getString("category_name"),
                        rs.getInt("likes")
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
                FOF.put("user_id", rs.getInt("ID"));
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
                        rs.getObject("limitation") == null ? null : rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
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
                statementPhotos.setInt(1, rs.getInt("ID"));
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
    public JSONArray getCollections(int userId, int friendUserId, int rowCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL show_collections(?,?,?,?)";
            String queryPhotos = "SELECT photoID FROM Collection_photo WHERE collectionID = ?";
            JSONArray collections = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, friendUserId);
            statement.setInt(3, rowCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                PreparedStatement statementPhotos = conn.prepareStatement(queryPhotos);
                statementPhotos.setInt(1, rs.getInt("ID"));
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
                        photos
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
        String query = "CALL show_profile(?)";
        JSONObject profileJSON = new JSONObject();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, toSeeUserId);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Profile profile = new Profile(
                        rs.getInt("ID"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getDate("DOB"),
                        rs.getTimestamp("time_last_access"),
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
    public JSONArray searchPhoto(int userId, String keyWord, String scope, int days, int rowCount, int offset)
            throws IDException, SQLException {
        String query = "CALL search_photos(?,?,?,?,?,?)";
        JSONArray photos = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setString(3, scope);
            statement.setInt(4, days);
            statement.setInt(5, rowCount);
            statement.setInt(6, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Photo photo = new Photo(rs.getInt("ID"),
                        rs.getInt("upload_by_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("visibility"),
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getString("street"),
                        rs.getString("zip"),
                        rs.getTimestamp("time_captured"),
                        rs.getTimestamp("time_uploaded"),
                        rs.getString("category_name"),
                        null
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
    public JSONArray searchEvent(int userId, String keyWord, int rowCount, int offset) throws IDException, SQLException {
        String query = "CALL search_events(?,?,?,?)";
        JSONArray events = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setInt(3, rowCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getObject("limitation") == null ? null : rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
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
    public JSONArray searchCollection(int userId, String keyWord, int rowCount, int offset) throws IDException, SQLException {
        try {
            String query = "CALL search_collections(?,?,?,?)";
            String queryPhotos = "SELECT photoID FROM Collection_photo WHERE collectionID = ?";
            JSONArray collections = new JSONArray();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, keyWord);
            statement.setInt(3, rowCount);
            statement.setInt(4, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                PreparedStatement statementPhotos = conn.prepareStatement(queryPhotos);
                statementPhotos.setInt(1, rs.getInt("ID"));
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
                        photos
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
    public JSONArray searchEventByAddress(int userId, JSONObject address, int rowCount, int offset) throws IDException, SQLException {
        String query = "CALL search_events_by_address(?,?,?,?,?)";
        JSONArray events = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setString(2, address.getString("city"));
            statement.setString(3, address.getString("street"));
            statement.setInt(4, rowCount);
            statement.setInt(5, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getObject("limitation") == null ? null : rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
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
    public JSONArray searchEventByLoc(int userId, JSONObject location, int rowCount, int offset) throws IDException, SQLException {
        String query = "CALL search_events_by_loc(?,?,?,?,?,?)";
        JSONArray events = new JSONArray();
        try {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setFloat(2, location.getJSONObject("center").getFloat("lon"));
            statement.setFloat(3, location.getJSONObject("center").getFloat("lat"));
            statement.setInt(4, location.getInt("max_distance"));
            statement.setInt(4, rowCount);
            statement.setInt(5, offset);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Event event = new Event(rs.getInt("ID"),
                        rs.getInt("host_userID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getObject("limitation") == null ? null : rs.getInt("limitation"),
                        rs.getString("visibility"),
                        rs.getTimestamp("time_created"),
                        rs.getTimestamp("time_happened"),
                        rs.getObject("longitude") == null ? null : rs.getFloat("longitude"),
                        rs.getObject("latitude") == null ? null : rs.getFloat("latitude"),
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
