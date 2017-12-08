package db;

import db.DBConnection;
import db.MySQLDBConnection;

import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PublicResourceCache {
    private static class SingletonHolder {
        private static final PublicResourceCache INSTANCE = new PublicResourceCache();
    }
    public static final PublicResourceCache getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private static HashSet<Integer> publicPhoto, publicEvent, publicCollection;
    private static boolean publicResLoaded = false;
    private static final DBConnection conn = new MySQLDBConnection();
    private static ReadWriteLock photoRwLock = new ReentrantReadWriteLock();
    private static ReadWriteLock eventRwLock = new ReentrantReadWriteLock();
    private static ReadWriteLock collectionRwLock = new ReentrantReadWriteLock();
    private PublicResourceCache() {
        if (!publicResLoaded) {
            reloadPublicResource();
        }
    }
    public static void reloadPublicResource() {
        try {
            photoRwLock.writeLock().lock();
            eventRwLock.writeLock().lock();
            collectionRwLock.writeLock().lock();
            publicPhoto = conn.publicPhotoLoader();
            publicEvent = conn.publicEventLoader();
            publicCollection = conn.publicCollectionLoader();
            publicResLoaded = true;
            System.out.println("Public resource cache loaded");
        } catch (Exception e) {
            publicPhoto = new HashSet<Integer>();
            publicEvent = new HashSet<Integer>();
            publicCollection = new HashSet<Integer>();
            publicResLoaded = false;
            e.printStackTrace();
        } finally {
            photoRwLock.writeLock().unlock();
            eventRwLock.writeLock().unlock();
            collectionRwLock.writeLock().unlock();
        }
    }
    public void addPublicPhoto(int photoId) {
        photoRwLock.writeLock().lock();
        publicPhoto.add(photoId);
        photoRwLock.writeLock().unlock();
    }
    public void addPublicCollection(int collectionId) {
        collectionRwLock.writeLock().lock();
        publicCollection.add(collectionId);
        collectionRwLock.writeLock().unlock();
    }
    public void addPublicEvent(int eventId) {
        eventRwLock.writeLock().lock();
        publicEvent.add(eventId);
        eventRwLock.writeLock().unlock();
    }
    public void delPublicPhoto(int photoId) {
        photoRwLock.writeLock().lock();
        publicPhoto.remove(photoId);
        photoRwLock.writeLock().unlock();
    }
    public void delPublicCollection(int collectionId) {
        collectionRwLock.writeLock().lock();
        publicCollection.remove(collectionId);
        collectionRwLock.writeLock().unlock();
    }
    public void delPublicEvent(int eventId) {
        eventRwLock.writeLock().lock();
        publicEvent.remove(eventId);
        eventRwLock.writeLock().unlock();
    }
    public boolean isPublicPhoto(int photoId) {
        boolean hasPhoto;
        photoRwLock.readLock().lock();
        hasPhoto = publicPhoto.contains(photoId);
        photoRwLock.readLock().unlock();
        return hasPhoto;
    }
    public boolean isPublicCollection(int collectionId) {
        boolean hasCollection;
        collectionRwLock.readLock().lock();
        hasCollection = publicCollection.contains(collectionId);
        collectionRwLock.readLock().unlock();
        return hasCollection;
    }
    public boolean isPublicEvent(int eventId) {
        boolean hasEvent;
        eventRwLock.readLock().lock();
        hasEvent = publicEvent.contains(eventId);
        eventRwLock.readLock().unlock();
        return hasEvent;
    }

}
