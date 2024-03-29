package model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class Collection {
    private final int id;
    private int hostId = -1;
    private String title = null;
    private String description = null;
    private String visibility = null;
    private Date timeCreated = null;
    private HashSet<Integer> photos = null;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    public Collection(int id, int hostId, String title, String visibility, String description, Date timeCreated,
                      HashSet<Integer> photos) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.visibility = visibility;
        this.description = description;
        this.timeCreated = timeCreated;
        this.photos = photos;
    }

    public Collection(int id, int hostId, String title, String visibility, String description, Date timeCreated) {
        this.id = id;
        this.hostId = hostId;
        this.title = title;
        this.visibility = visibility;
        this.description = description;
        this.timeCreated = timeCreated;
    }

    public Collection(int id, HashSet<Integer> photos) {
        this.id = id;
        this.photos = photos;
    }

    public Collection(JSONObject collectionData) {
        this.id = collectionData.optInt("collection_id");
        try {
            this.hostId = collectionData.optInt("host_id");
            this.title = isNull(collectionData, "title") ? null : collectionData.getString("title");
            this.description = isNull(collectionData, "description") ? null : collectionData.getString(
                    "description");
            this.visibility = collectionData.getString("visibility");
        } catch (JSONException e) {
            e.printStackTrace();
            this.hostId = -1;
            this.title = null;
            this.visibility = null;
            this.description = null;
            this.timeCreated = null;
        }

        try {
            this.photos = new HashSet<Integer>();
            JSONArray photosArray = collectionData.getJSONArray("photos");
            for (int i = 0; i < photosArray.length(); ++i) {
                this.photos.add(photosArray.getInt(i));
            }
        } catch (JSONException e) {
            this.photos = null;
        }
    }

    public void editCollectionMetadata(JSONObject metadata) {
        if (title != null) {
            this.title = isNull(metadata, "title") ? null : metadata.getString("title");
            this.description = isNull(metadata, "description") ? null : metadata.getString("description");
            this.visibility = metadata.getString("visibility");
        }
    }

    public void editCollectionPhoto(JSONObject changes) {
        if (this.photos != null) {
            if (changes.has("add")) {
                try {
                    JSONArray toAdd = changes.getJSONArray("add");
                    for (int i = 0; i < toAdd.length(); ++i) {
                        this.photos.add(toAdd.getInt(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (changes.has("del")) {
                try {
                    JSONArray toDelete = changes.getJSONArray("del");
                    for (int i = 0; i < toDelete.length(); ++i) {
                        this.photos.remove(toDelete.getInt(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("collection_id", id);
        object.put("host_id", hostId);
        object.put("title", title);
        object.put("description", description);
        object.put("visibility", visibility);
        object.put("time_created", dateFormat.format(timeCreated));
        JSONArray photosArr = new JSONArray();
        for (Integer photoId:photos) {
            photosArr.put(photoId);
        }
        object.put("photos", photosArr);
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

    public String getVisibility() {
        return visibility;
    }

    public Date getTimeCreated() {
        return timeCreated;
    }

    public HashSet<Integer> getPhotos() {
        return photos;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
