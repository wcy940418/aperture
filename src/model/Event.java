package model;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private final int id;
    private final int hostId;
    private String title;
    private String description;
    private Integer limitation;
    private String visibility;
    private Date timeCreated;
    private Date timeHappened;
    private Float lon;
    private Float lat;
    private String country;
    private String state;
    private String city;
    private String street;
    private String zip;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    public Event(int id, int hostId, String title, String description, Integer limitation, String visibility,
                 Date timeCreated, Date timeHappened, Float lon, Float lat, String country, String state, String city,
                 String street, String zip) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.description = description;
        this.limitation = limitation;
        this.visibility = visibility;
        this.timeCreated = timeCreated;
        this.timeHappened = timeHappened;
        this.lon = lon;
        this.lat = lat;
        this.country = country;
        this.state = state;
        this.city = city;
        this.street = street;
        this.zip = zip;
    }
    public Event(JSONObject object) {
        try {
            this.timeHappened = isNull(object, "time_happened") ? null : dateFormat.parse(object.getString(
                    "time_happened"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.id = object.optInt("event_id");
        this.hostId = object.optInt("host_id");
        this.title = object.getString("title");
        this.description = isNull(object, "description") ? null : object.getString("description");
        this.limitation = isNull(object, "limitation") ? null : object.getInt("limitation");
        this.visibility = object.getString("visibility");
        this.lon = isNull(object, "lon") ? null : object.getFloat("lon");
        this.lat = isNull(object, "lat") ? null : object.getFloat("lat");
        this.country = isNull(object, "country") ? null : object.getString("country");
        this.state = isNull(object, "state") ? null : object.getString("state");
        this.city = isNull(object, "city") ? null : object.getString("city");
        this.street = isNull(object, "street") ? null : object.getString("street");
        this.zip = isNull(object, "zip") ? null : object.getString("zip");
    }
    public void editEvent(JSONObject object) {
        try {
            this.timeHappened = isNull(object, "time_happened") ? null : dateFormat.parse(object.getString(
                    "time_happened"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.title = object.getString("title");
        this.description = isNull(object, "description") ? null : object.getString("description");
        this.limitation = isNull(object, "limitation") ? null : object.getInt("limitation");
        this.visibility = object.getString("visibility");
        this.lon = isNull(object, "lon") ? null : object.getFloat("lon");
        this.lat = isNull(object, "lat") ? null : object.getFloat("lat");
        this.country = isNull(object, "country") ? null : object.getString("country");
        this.state = isNull(object, "state") ? null : object.getString("state");
        this.city = isNull(object, "city") ? null : object.getString("city");
        this.street = isNull(object, "street") ? null : object.getString("street");
        this.zip = isNull(object, "zip") ? null : object.getString("zip");
    }


    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("event_id", id);
            object.put("host_id", hostId);
            object.put("title", title);
            object.put("description", description);
            object.put("visibility", visibility);
            object.put("lon", lon);
            object.put("lat", lat);
            object.put("country", country);
            object.put("state", state);
            object.put("city", city);
            object.put("street", street);
            object.put("zip", zip);
            object.put("time_created", dateFormat.format(timeCreated));
            object.put("time_happened", dateFormat.format(timeHappened));
            object.put("limitation", limitation);
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

    public int getHostId() {
        return hostId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getLimitation() {
        return limitation;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public Date getTimeHappened() {
        return timeHappened;
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

    public String getVisibility() {
        return visibility;
    }

    public String getState() {
        return state;
    }
}
