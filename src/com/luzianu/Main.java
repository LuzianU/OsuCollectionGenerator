package com.luzianu;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.luzianu.beatmap.BeatmapInfo;
import com.luzianu.io.CollectionDb;
import com.luzianu.io.OsuDb;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.luzianu.UserVariable.*;

public class Main {

    private static OsuDb osuDb;

    public static String osuRootDir;
    public static String osuSongDir;

    public static final String CURRENT_VERSION = "v1.3";
    public static final String GIT_HUB_LATEST_URL = "https://github.com/LuzianU/OsuCollectionGenerator/releases/latest";

    public static HashMap<String, UserVariable> userVariables = new HashMap<>();
    public static ArrayList<String> skipBeatmapIds = new ArrayList<>();

    /**
     * If you run this code in your IDE make sure to add "-noUpdate" to the program arguments
     * to avoid having to execute updater.jar
     */
    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);

        doUpdater(args);

        Properties properties = new Properties();

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

        if (!loadAndGenerateProperties(properties))
            return; // exit program
        storeProperties(properties);

        UserInterface ui = new UserInterface();

        new Thread(() -> {
            try {
                osuDb = OsuDb.Reader.read(Paths.get(osuRootDir, "osu!.db").toFile(), ui);
            } catch (IOException e) {
                System.err.println("There was an error reading the osu!.db file");
            }
        }).start();
    }

    public static void doUpdater(String[] args) {
        if (args.length == 0 || !args[0].equals("-noUpdate")) {
            boolean runUpdater = false;
            String latestVersion = null;
            try {
                HttpURLConnection con = (HttpURLConnection) (new URL(GIT_HUB_LATEST_URL).openConnection());
                con.setInstanceFollowRedirects(false);
                con.connect();
                String location = con.getHeaderField("Location");
                if (location != null && !location.contains(GIT_HUB_LATEST_URL)) // redirect happened
                    latestVersion = location.replace(GIT_HUB_LATEST_URL.replace("latest", "tag/"), "");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (latestVersion != null && !latestVersion.equals(CURRENT_VERSION))
                runUpdater = true;

            if (runUpdater) {
                try {
                    final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                    final File currentJar;
                    currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

                    if (!currentJar.getName().endsWith(".jar"))
                        return;

                    // Build command: java -jar application.jar -.../Updater.jar
                    final ArrayList<String> command = new ArrayList<>();
                    command.add(javaBin);
                    command.add("-jar");
                    command.add(currentJar.getPath().replace(currentJar.getName(), "") + "Updater.jar");
                    command.add(currentJar.getPath());

                    final ProcessBuilder builder = new ProcessBuilder(command);
                    builder.start();
                    System.exit(0);
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
            }
        } else
            System.out.println("-noUpdate");
    }

    public static boolean generateFromOsuDb(UserInterface ui, File outputFile, boolean skipBeatmaps, CollectionDb defaultCollectionDb) throws FileNotFoundException {
        if (new File("skipBeatmaps.txt").exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(new File("skipBeatmaps.txt")))) {
                for (String line; (line = br.readLine()) != null; ) {
                    skipBeatmapIds.add(line.trim());
                }
            } catch (IOException ignored) {
            }
        }

        //Printer printer = new Printer("output.txt");
        final int[] counter = { 0 };
        final AtomicInteger[] percentage = { new AtomicInteger() };
        HashMap<String, ArrayList<BeatmapInfo>> collectionMap = new HashMap<>();

        osuDb.beatmaps.parallelStream().forEach(beatmap -> {
            if (percentage[0].get() < (int) (100.0 * counter[0] / osuDb.beatmaps.size())) {
                percentage[0].set((int) (100.0 * counter[0] / osuDb.beatmaps.size()));
                ui.setGenerateProgressbarValue(percentage[0].get());
            }

            counter[0]++;

            // skip non osu!std maps
            if (beatmap.mode == 0) {
                File osuFile = Paths.get(osuSongDir, beatmap.folderName, beatmap.nameOfOsuFile).toFile();

                if (osuFile.exists()) {
                    // skip ones in skipBeatmapIds
                    if (!skipBeatmapIds.contains(beatmap.difficultyId + "")) {
                        BeatmapInfo info = Analyzer.analyze(osuFile);

                        if (info != null && info.isAccepted) {
                            info.artist = beatmap.artist;
                            info.title = beatmap.title;
                            info.creator = beatmap.creator;
                            info.difficulty = beatmap.difficulty;
                            info.md5 = beatmap.md5;

                            String collectionName = "BPM Change";
                            if (!info.isBpmChange) {
                                int roundedBpm = (info.mostCommonBpm / 10) * 10;
                                collectionName = "BPM " + roundedBpm + "-" + (roundedBpm + 9);
                            }

                            if (info.isComplex) {
                                collectionName = "BPM Complex";
                            }

                            ArrayList<BeatmapInfo> bpmInfos = collectionMap.getOrDefault(collectionName, new ArrayList<>());
                            bpmInfos.add(info);
                            collectionMap.put(collectionName, bpmInfos);

                            if (info.isBpmChange) {
                                collectionName = "BPM Change";
                                bpmInfos = collectionMap.getOrDefault(collectionName, new ArrayList<>());
                                bpmInfos.add(info);
                                collectionMap.put(collectionName, bpmInfos);
                            }

                            if (!skipBeatmapIds.contains(beatmap.difficultyId + ""))
                                skipBeatmapIds.add(beatmap.difficultyId + "");

                            //printer.add(beatmap, info);
                        }
                    }
                }
            }

        });

        //printer.print();

        CollectionDb collectionDb = defaultCollectionDb;
        if (collectionDb == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = new Date();
            int version = Integer.parseInt(dateFormat.format(date));
            collectionDb = new CollectionDb(version);
        }

        for (String key : collectionMap.keySet()) {
            Collection collection = null;

            boolean createNew = true;
            for (Collection c : collectionDb.getCollections()) {
                if (c.getName().equals(key)) {
                    createNew = false;
                    collection = c;
                    break;
                }
            }
            if (createNew)
                collection = new Collection(key);

            for (BeatmapInfo beatmapInfo : collectionMap.get(key))
                collection.addBeatmap(beatmapInfo.md5);

            if (createNew)
                collectionDb.addCollection(collection);
        }

        try (PrintStream out = new PrintStream(new FileOutputStream("skipBeatmaps.txt"))) {
            for (String id : skipBeatmapIds)
                out.println(id);
        }

        try {
            CollectionDb.Writer.write(collectionDb, outputFile);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private static boolean loadAndGenerateProperties(Properties properties) throws IOException {
        if (!new File("config.properties").exists())
            new File("config.properties").createNewFile();

        int orderInUi = 0;
        userVariables.put(MIN_STREAM_BPM, new UserVariable(
                MIN_STREAM_BPM,
                120,
                "<html>[Integer] Min. 1/4 bpm of a \"stream\" to be recognized as one." +
                "<br>A \"stream\" is at least two succeeding objects within the specified bpm range." +
                "<br>You might want to set this to something a bit higher like 150+ if you are primarily interested" +
                "<br>in 230+ bpm stream maps.</html>",
                orderInUi++,
                false));

        userVariables.put(MAX_STREAM_BPM, new UserVariable(
                MAX_STREAM_BPM,
                0,
                "<html>[Integer] Max. 1/4 bpm of a \"stream\" to be recognized as one." +
                "<br>A \"stream\" is at least two succeeding objects within the specified bpm range</html>",
                orderInUi++,
                false));

        userVariables.put(MIN_STREAM_PERCENTAGE, new UserVariable(
                MIN_STREAM_PERCENTAGE,
                40,
                "<html>[Integer] Only classify a map as a stream map if it's percentage of \"stream\" objects " +
                "<br>is greater than this variable.</html>",
                orderInUi++,
                false));

        userVariables.put(MAX_STREAM_PERCENTAGE, new UserVariable(
                MAX_STREAM_PERCENTAGE,
                100,
                "<html>[Integer] Only classify a map as a stream map if it's percentage of \"stream\" objects " +
                "<br>is less than this variable.</html>",
                orderInUi++,
                false));

        userVariables.put(BPM_CHANGE_THRESHOLD, new UserVariable(
                BPM_CHANGE_THRESHOLD,
                20,
                "<html>[Integer] Sum of the length of all non 1/3, 1/4, 1/6, 1/8, 1/12, 1/16 streams " +
                "<br>divided by the sum of the length of all streams times 100%. " +
                "<br>If that percentage is higher that this variable, a stream map is considered as having bpm changes. " +
                "<br>If it is, the streamPercentage AND streamLength is within the specified threshold " +
                "the map gets added to \"BPM Change\" </html>",
                orderInUi++,
                false));

        userVariables.put(BPM_COMPLEX_THRESHOLD, new UserVariable(
                BPM_COMPLEX_THRESHOLD,
                20,
                "<html>[Integer] Sum of the length of all 1/3, 1/6, 1/8, 1/12, 1/16 streams " +
                "<br>divided by the sum of the length of all streams times 100%. " +
                "<br>If that percentage is higher that this variable, a map is considered as being complex. " +
                "<br>If it is and the streamPercentage is within the specified threshold " +
                "the map gets added to \"BPM Complex\". " +
                "<br>Note: The streamLength threshold will not affect this decision.</html>",
                orderInUi++,
                false));

        userVariables.put(MIN_STREAM_LENGTH_AT_LEAST, new UserVariable(
                MIN_STREAM_LENGTH_AT_LEAST,
                9,
                "<html>[Integer] Only classify a possible stream map as one if it has AT LEAST one stream longer than" +
                "x objects. " +
                "<br>This variable will only be applied to non-complex ones.</html>",
                orderInUi++,
                false));

        userVariables.put(MAX_STREAM_LENGTH_ALL, new UserVariable(
                MAX_STREAM_LENGTH_ALL,
                0,
                "<html>[Integer] Only classify a possible stream map as one if ALL streams are shorter than" +
                "x objects. " +
                "<br>This variable will only be applied to non-complex ones.</html>",
                orderInUi++,
                false));

        userVariables.put(DELTA_VARIATION, new UserVariable(
                DELTA_VARIATION,
                5,
                "<html>[Integer] A \"delta\" is the time period between two objects in ms. This variable is necessary due to casting " +
                "<br>errors of deltas in streams. A 180 bpm stream has deltas of 83.33.. ms. " +
                "<br>objects are stored as integers however that way some deltas will be 83 and some 84.</html>",
                orderInUi++,
                false));

        userVariables.put(PRINT_STREAMS, new UserVariable(
                PRINT_STREAMS,
                0,
                "<html>[0 or 1]Prints information about every \"stream\" of the dropped beatmap.</html>",
                orderInUi++,
                false));

        userVariables.put(PRINT_STREAM_THRESHOLD, new UserVariable(
                PRINT_STREAM_THRESHOLD,
                0,
                "<html>[Integer] Only print the respective stream information if it's longer than or equal to x objects." +
                "<br>Use this to avoid seeing so many doubles, triples, etc.</html>",
                orderInUi++,
                false));

        userVariables.put(WEIGHT_3, new UserVariable(
                WEIGHT_3,
                1,
                "[Double] Weight for a 1/3 stream when calculating if the map has a complex rhythm.",
                orderInUi++,
                true));

        userVariables.put(WEIGHT_6, new UserVariable(
                WEIGHT_6,
                1,
                "[Double] Weight for a 1/6 stream when calculating if the map has a complex rhythm.",
                orderInUi++,
                true));

        userVariables.put(WEIGHT_8, new UserVariable(
                WEIGHT_8,
                2,
                "[Double] Weight for a 1/8 stream when calculating if the map has a complex rhythm.",
                orderInUi++,
                true));

        userVariables.put(WEIGHT_12, new UserVariable(
                WEIGHT_12,
                4,
                "[Double] Weight for a 1/12 stream when calculating if the map has a complex rhythm.",
                orderInUi++,
                true));

        userVariables.put(WEIGHT_16, new UserVariable(
                WEIGHT_16,
                4,
                "[Double] Weight for a 1/16 stream when calculating if the map has a complex rhythm.",
                orderInUi++,
                true));

        userVariables.put(BROADEN_SEARCH, new UserVariable(
                BROADEN_SEARCH,
                0,
                "<html>[0 or 1] Broaden search by also looking for 1/3 streams below the threshold." +
                "<br>If you are mainly interested in complex patterns, set this variable to 1.",
                orderInUi++,
                false));

        try (FileInputStream is = new FileInputStream("config.properties")) {
            properties.load(is);

            if (!properties.containsKey("osuRootDir")) {
                File selectedFile;
                do {
                    selectedFile = openOsuRootDirChooser();
                    if (demandExit)
                        return false;
                } while (selectedFile == null);
                properties.setProperty("osuRootDir", selectedFile.getAbsolutePath());
            }
            osuRootDir = properties.getProperty("osuRootDir");
            System.out.println("osuRootDir: " + osuRootDir);

            // get osu songs dir
            File cfgFile = Paths.get(osuRootDir, "osu!." + System.getProperty("user.name") + ".cfg").toFile();
            try (BufferedReader br = new BufferedReader(new FileReader(cfgFile))) {
                for (String line; (line = br.readLine()) != null; ) {
                    if (line.startsWith("BeatmapDirectory")) {
                        line = line.substring(line.indexOf("=") + 1).trim();
                        if (Paths.get(line).isAbsolute())
                            osuSongDir = line;
                        else
                            osuSongDir = Paths.get(osuRootDir, line).toString();
                        System.out.println("osuSongDir: " + osuSongDir);
                        break;
                    }
                }
            }
        }

        return true;
    }

    private static boolean demandExit = false;

    private static void storeProperties(Properties properties) throws IOException {
        properties.store(new FileOutputStream("config.properties"), null);
    }

    private static File openOsuRootDirChooser() {
        // try to determine osu root dir by running osu! process. Windows only
        if (System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH).contains("win")) {
            String tasksCmd = System.getenv("windir") + "/system32/wbem/WMIC.exe process where \"name='osu!.exe'\" get ExecutablePath";

            try {
                Process p = Runtime.getRuntime().exec(tasksCmd);
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

                ArrayList<String> procs = new ArrayList<>();
                String line;
                while ((line = input.readLine()) != null)
                    procs.add(line);

                input.close();

                Optional<String> optionalString = procs.stream().
                        filter(row -> row.contains("\\osu!\\osu!.exe")).findFirst();

                if (optionalString.isPresent())
                    return new File(optionalString.get().trim().replace("\\osu!.exe", ""));
            } catch (IOException ignored) {
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.home") + "\\AppData\\Local\\osu!"));
        chooser.setDialogTitle("Choose your osu! root folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (chooser.getSelectedFile().isDirectory()) {

                if (Arrays.stream(chooser.getSelectedFile().listFiles()).anyMatch(x -> x.getName().equals("osu!.db")))
                    return chooser.getSelectedFile();
                else
                    return null;
            }
        } else demandExit = true;

        return null;
    }

}
