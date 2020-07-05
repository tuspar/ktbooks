package main.services.backend;

import org.dizitart.no2.Nitrite;

public class Database {
    private static final String path = "data/data.db";
    private static Database dbc;
    private static Nitrite db;

    private Database() {
        System.out.println("Creating New Nitrite Instance");
        db = Nitrite.builder()
                .compressed()
                .filePath(path)
                .openOrCreate();
    }

    public static Nitrite getInstance() {
        if (dbc == null) {
            dbc = new Database();
        }
        db.commit();
        return db;
    }

    public static String getPath() {
        return path;
    }

    public static void exit() {
        db.commit();
        db.close();
    }
}

