package model;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Profile {
    private final int id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Date DOB;
    private Date lastAccessTime;
    private String country;
    private String introduction;
    private String gender;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    private DateFormat DOBFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public Profile(int id, String username, String email, String firstName, String lastName,
                   Date DOB, Date lastAccessTime, String country, String introduction, String gender) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.DOB = DOB;
        this.lastAccessTime = lastAccessTime;
        this.country = country;
        this.introduction = introduction;
        this.gender = gender;
    }

    public Profile(JSONObject object) {
        this.id = object.getInt("user_id");
        this.username = object.getString("username");
        this.email = object.getString("email");
        this.firstName = object.getString("first_name");
        this.lastName = object.getString("last_name");
        try {
            this.DOB = DOBFormat.parse(object.getString("DOB"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.lastAccessTime = dateFormat.parse(object.getString("time_last_access"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.country = object.getString("country");
        this.introduction = object.getString("introduction");
        this.gender = object.getString("gender");
    }

    public void editProfile(JSONObject object) {
        this.firstName = object.getString("first_name");
        this.lastName = object.getString("last_name");
        try {
            this.DOB = DOBFormat.parse(object.getString("DOB"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.country = object.getString("country");
        this.introduction = object.getString("introduction");
        this.gender = object.getString("gender");
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("user_id", id);
            object.put("username", username);
            object.put("email", email);
            object.put("first_name", firstName);
            object.put("last_name", lastName);
            object.put("DOB", DOB);
            object.put("time_last_access", dateFormat.format(lastAccessTime));
            object.put("country", country);
            object.put("introduction", introduction);
            object.put("gender", gender);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getDOB() {
        return DOB;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public String getCountry() {
        return country;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getGender() {
        return gender;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
