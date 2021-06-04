package com.luzianu;

import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {
    public static final String GIT_HUB_LATEST_URL = "https://github.com/LuzianU/OsuCollectionGenerator/releases/latest";
    public static final String GIT_HUB_DOWNLOAD_BASE_URL = "https://github.com/LuzianU/OsuCollectionGenerator/releases/download/";
    public static final String JAR_NAME = "OsuCollectionGenerator.jar";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize flat LaF");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException ex) {
                System.err.println("Failed to initialize system LaF");
            }
        }

        if (args.length != 0) { // only if this program was run by the main one
            try {
                HttpURLConnection con = (HttpURLConnection) (new URL(GIT_HUB_LATEST_URL).openConnection());
                con.setInstanceFollowRedirects(false);
                con.connect();
                String location = con.getHeaderField("Location");
                if (!location.contains(GIT_HUB_LATEST_URL)) { // redirect happened
                    String latestVersion = location.replace(GIT_HUB_LATEST_URL.replace("latest", "tag/"), "");

                    // download new version
                    String dlUrl = GIT_HUB_DOWNLOAD_BASE_URL + latestVersion + "/" + JAR_NAME;
                    try (BufferedInputStream in = new BufferedInputStream(new URL(dlUrl).openStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(JAR_NAME)) {
                        byte dataBuffer[] = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }

                        JOptionPane.showMessageDialog(null, "Please open CollectionManager.jar again.",
                                                      "Installed " + latestVersion, JOptionPane.PLAIN_MESSAGE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
