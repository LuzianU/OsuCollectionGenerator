package com.luzianu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class Updater {
    public static final String gitHubLatestUrl = "https://github.com/LuzianU/OsuCollectionGenerator/releases/latest";
    public static final String gitHubDownloadBaseUrl = "https://github.com/LuzianU/OsuCollectionGenerator/releases/download/";
    public static final String jarName = "OsuCollectionGenerator.jar";

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length != 0) { // only if this program was run by the main one
            try {
                HttpURLConnection con = (HttpURLConnection) (new URL(gitHubLatestUrl).openConnection());
                con.setInstanceFollowRedirects(false);
                con.connect();
                String location = con.getHeaderField("Location");
                if (!location.contains(gitHubLatestUrl)) { // redirect happened
                    String latestVersion = location.replace(gitHubLatestUrl.replace("latest", "tag/"), "");

                    // download new version
                    String dlUrl = gitHubDownloadBaseUrl + latestVersion + "/" + jarName;
                    try (BufferedInputStream in = new BufferedInputStream(new URL(dlUrl).openStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(jarName)) {
                        byte dataBuffer[] = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // now launch the main program with -noUpdate argument

        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final File currentJar = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());

        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        if (args.length == 0)
            command.add(currentJar.getPath().replace(currentJar.getName(), "") + "OsuCollectionGenerator.jar");
        else
            command.add(currentJar.getPath().replace(currentJar.getName(), "") + args[0]);

        if (args.length != 0) // only if this program was run by the main one
            command.add("-noUpdate");

        System.out.println(command);

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
    }
}
