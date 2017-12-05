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
    private int limitation;
    private String visibility;
    private Date timeCreated;
    private Date timeHappened;
    private float lon;
    private float lat;
    private String country;
    private String city;
    private String street;
    private String zip;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    public Event(int id, int hostId, String title, String description, int limitation, String visibility,
                 Date timeCreated, Date timeHappened, float lon, float lat, String country, String city,
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
        this.city = city;
        this.street = street;
        this.zip = zip;
    }
    public Event(JSONObject object) {
        try {
            this.timeCreated = dateFormat.parse(object.getString("time_created"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.timeHappened = dateFormat.parse(object.getString("time_happened"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.id = object.getInt("event_id");
        this.hostId = object.getInt("host_id");
        this.title = object.getString("title");
        this.description = object.getString("description");
        this.limitation = object.getInt("limitation");
        this.visibility = object.getString("visibility");
        this.lon = object.getFloat("lon");
        this.lat = object.getFloat("lat");
        this.country = object.getString("country");
        this.city = object.getString("city");
        this.street = object.getString("street");
        this.zip = object.getString("zip");
    }
    public void editEvent(JSONObject object) {
        try {
            this.timeHappened = dateFormat.parse(object.getString("time_happened"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.title = object.getString("title");
        this.description = object.getString("description");
        this.limitation = object.getInt("limitation");
        this.visibility = object.getString("visibility");
        this.lon = object.getFloat("lon");
        this.lat = object.getFloat("lat");
        this.country = object.getString("country");
        this.city = object.getString("city");
        this.street = object.getString("street");
        this.zip = object.getString("zip");
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

    public int getLimitation() {
        return limitation;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public Date getTimeHappened() {
        return timeHappened;
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

    public String getVisibility() {
        return visibility;
    }
}
