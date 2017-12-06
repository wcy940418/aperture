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
    private Float lon;
    private Float lat;
    private String visibility;
    private String country;
    private String city;
    private String street;
    private String zip;
    private Date timeCaptured;
    private Date timeUploaded;
    private String category;
    private Integer likes;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public Photo(int id, int uploaderId, String title, String description, String visibility, Float lon, Float lat,
                 String country, String city, String street, String zip, Date timeCaptured, Date timeUploaded,
                 String category, Integer likes) {
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
        this.likes = likes;
    }

    public Photo(JSONObject object) {
        try {
            this.timeCaptured = isNull(object, "time_captured") ? null : dateFormat.parse(object.getString(
                    "time_captured"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.id = object.optInt("photo_id");
        this.uploaderId = object.optInt("uploader_id");
        this.title = isNull(object, "title") ? null : object.getString("title");
        this.description = isNull(object, "description") ? null : object.getString("description");
        this.visibility = object.getString("visibility");
        this.lon = isNull(object, "lon") ? null : object.getFloat("lon");
        this.lat = isNull(object, "lat") ? null : object.getFloat("lat");
        this.country = isNull(object, "country") ? null : object.getString("country");
        this.city = isNull(object, "city") ? null : object.getString("city");
        this.street = isNull(object, "street") ? null : object.getString("street");
        this.zip = isNull(object, "zip") ? null : object.getString("zip");
        this.category = object.getString("category");
        this.likes = object.optInt("likes");
    }

    public void editPhotoInfo(JSONObject object) {
        try {
            this.timeCaptured = isNull(object, "time_captured") ? null : dateFormat.parse(object.getString(
                    "time_captured"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.title = isNull(object, "title") ? null : object.getString("title");
        this.description = isNull(object, "description") ? null : object.getString("description");
        this.visibility = object.getString("visibility");
        this.lon = isNull(object, "lon") ? null : object.getFloat("lon");
        this.lat = isNull(object, "lat") ? null : object.getFloat("lat");
        this.country = isNull(object, "country") ? null : object.getString("country");
        this.city = isNull(object, "city") ? null : object.getString("city");
        this.street = isNull(object, "street") ? null : object.getString("street");
        this.zip = isNull(object, "zip") ? null : object.getString("zip");
        this.category = object.getString("category");
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("photo_id", id);
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
            object.put("time_captured", timeCaptured == null ? null : dateFormat.format(timeCaptured));
            object.put("time_uploaded", dateFormat.format(timeUploaded));
            object.put("category", category);
            object.put("likes", likes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    private boolean isNull(JSONObject object, String key) {
        return object.isNull(key);
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

    public Float getLon() {
        return lon;
    }

    public Float getLat() {
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

    public Integer getLikes() {
        return likes;
    }
}
