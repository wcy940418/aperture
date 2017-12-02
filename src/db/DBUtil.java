package db;

public class DBUtil {
    private static final String HOSTNAME = "localhost";
    private static final String PORT = "3306";
    private static final String DB = "aperture";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    public static final String URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DB + "?user=" + USERNAME
            + "&password=" + PASSWORD + "&autoreconnect=true";
}
