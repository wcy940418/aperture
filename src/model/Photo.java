package model;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Photo {
    private int id;
    private int uploaderId;
    private String title;
    private String description;
    private float lon;
    private float lat;
    private String visibility;
    private String country;
    private String city;
    private String street;
    private String zip;
    private Date timeCaptured;
    private Date timeUploaded;
    private String category;
    private DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD HH:MM:SS", Locale.ENGLISH);
    public Photo(int id, int uploaderId, String title, String description, String visibility, float lon, float lat,
                 String country, String city, String street, String zip, Date timeCaptured, Date timeUploaded,
                 String category) {
        this.id = id;
        this.uploaderId = uploaderId;
        this.title = title;
        this.description = description;
        this.visibility = visibility;
        this.lon = lon;
        this.lat = lat;
        this.country = country;
        this.city = city;
        this.street = street;
        this.zip = zip;
        this.timeCaptured = timeCaptured;
        this.timeUploaded = timeUploaded;
        this.category = category;
    }
    public Photo(JSONObject object) {
        try {
            this.timeCaptured = dateFormat.parse(object.getString("time_captured"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.timeUploaded = dateFormat.parse(object.getString("time_uploaded"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.id = object.getInt("id");
        this.uploaderId = object.getInt("uploader_id");
        this.title = object.getString("title");
        this.description = object.getString("description");
        this.visibility = object.getString("visibility");
        this.lon = object.getFloat("lon");
        this.lat = object.getFloat("lat");
        this.country = object.getString("country");
        this.city = object.getString("city");
        this.street = object.getString("street");
        this.zip = object.getString("zip");
        this.category = object.getString("category");
    }
    public void editPhotoInfo(JSONObject object) {
        try {
            this.timeCaptured = dateFormat.parse(object.getString("time_captured"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.visibility = object.getString("visibility");
        this.title = object.getString("title");
        this.description = object.getString("description");
        this.lon = object.getFloat("lon");
        this.lat = object.getFloat("lat");
        this.country = object.getString("country");
        this.city = object.getString("city");
        this.street = object.getString("street");
        this.zip = object.getString("zip");
        this.category = object.getString("category");
    }

    public int getId() {
        return id;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public float getLon() {
        return lon;
    }

    public float getLat() {
        return lat;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }

    public Date getTimeCaptured() {
        return timeCaptured;
    }

    public Date getTimeUploaded() {
        return timeUploaded;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getCategory() {
        return category;
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("id", id);
            object.put("uploader_id", uploaderId);
            object.put("title", title);
            object.put("description", description);
            object.put("lon", lon);
            object.put("lat", lat);
            object.put("country", country);
            object.put("city", city);
            object.put("street", street);
            object.put("visibility", visibility);
            object.put("zip", zip);
            object.put("time_captured", dateFormat.format(timeCaptured));
            object.put("time_uploaded", dateFormat.format(timeUploaded));
            object.put("category", category);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;

    }
}
