package com.luzianu.io;

import com.luzianu.Collection;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.luzianu.io.Input.*;

public class CollectionDb {
    private int version;
    private List<Collection> collections;

    public CollectionDb(int version) {
        this.version = version;
        this.collections = new ArrayList<>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getSize() {
        return collections.size();
    }

    public void addCollection(Collection collection) {
        collections.add(collection);
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public static class Reader {
        public static CollectionDb read(File file) throws IOException {
            CollectionDb db;

            long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
            byte[] buffer = null;
            if (file.length() < presumableFreeMemory * .9) {
                try {
                    buffer = new byte[(int) file.length()];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(buffer);
                    }
                } catch (Exception ignored) {
                    System.err.println("collection.db file is too big to be loaded into memory");
                }
            }

            try (InputStream is = buffer != null ? new ByteArrayInputStream(buffer) : new FileInputStream(file)) {
                int version = readInt(is);
                //System.out.println("version: " + version);

                db = new CollectionDb(version);

                int numberOfCollections = readInt(is);
                //System.out.println("number of collections: " + numberOfCollections);

                for (int i = 0; i < numberOfCollections; i++) {
                    String name = readString(is);
                    //System.out.println("    name: " + name);

                    Collection collection = new Collection(name);

                    int n = readInt(is);
                    //System.out.println("    n: " + n);

                    for (int j = 0; j < n; j++) {
                        String md5 = readString(is);
                        //System.out.println("        md5: " + md5);
                        collection.addBeatmap(md5);
                    }

                    db.addCollection(collection);
                }
            }

            System.out.println(file.getName() + ": Version " + db.getVersion() + " with " + db.getSize() + " collections read.");

            return db;
        }
    }

    public static class Writer {
        public static void write(CollectionDb db, File file) throws IOException {
            try (OutputStream os = new FileOutputStream(file)) {
                writeInt(os, db.getVersion());
                writeInt(os, db.getSize());

                for (Collection collection : db.getCollections()) {
                    writeString(os, collection.getName());
                    writeInt(os, collection.getSize());

                    for (String md5 : collection.getMd5s()) {
                        writeString(os, md5);
                    }
                }
            }

            System.out.println(file.getName() + ": Version " + db.getVersion() + " with " + db.getSize() + " collections written.");

        }
    }
}
