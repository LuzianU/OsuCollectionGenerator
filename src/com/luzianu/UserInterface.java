package com.luzianu;

import com.luzianu.beatmap.BeatmapInfo;
import com.luzianu.beatmap.Stream;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserInterface {
    private JButton buttonGenerate;
    private JProgressBar readProgressBar;
    private JProgressBar generateProgressBar;
    private JFrame frame;
    private JPanel rightPanel;
    private List<JTextField> textFields;
    private JTextArea textArea;
    private JScrollPane scrollPane;

    public UserInterface() {
        UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        UIManager.put("Button.font", new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        UIManager.put("TextField.font", new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        UIManager.put("ToolTip.font", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("ProgressBar.font", new Font(Font.SANS_SERIF, Font.ITALIC, 14));

        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setInitialDelay(0);

        frame = new JFrame("Collection Generator");
        int[] sizes = new int[]{ 16, 32, 64, 128 };
        List<BufferedImage> icons = new ArrayList<>();

        // yes, the icons are generated at runtime
        for (Integer size : sizes) {
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) (72.0 * size / Toolkit.getDefaultToolkit().getScreenResolution())));
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String text = "\\";
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int x = (size - metrics.stringWidth(text)) / 2;
            int y = ((size - metrics.getHeight()) / 2) + metrics.getAscent();
            g.setColor(Color.WHITE);
            g.fillOval(0, 0, size, size);
            g.setColor(new Color(255, 102, 170));
            g.fill(new Ellipse2D.Double(size * .05, size * .05, size * .9, size * .9));
            g.setColor(Color.WHITE);
            g.drawString(text, x, y);
            icons.add(img);
            g.dispose();
        }
        frame.setIconImages(icons);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setRows(12);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setVisible(false);
        buttonGenerate = new JButton("Generate");

        rightPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        DragDropPanel dragDropPanel = new DragDropPanel();
        rightPanel.add(dragDropPanel, BorderLayout.CENTER);

        buttonGenerate.setVisible(false);
        buttonGenerate.addActionListener(e -> {
            if (saveVariables()) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose where to save the generated collection. Do NOT overwrite any of your osu .db files!");
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isDirectory() || f.getName().endsWith(".db");
                    }

                    @Override
                    public String getDescription() {
                        return "Collection database (*.db)";
                    }
                });

                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {

                    if (fc.getSelectedFile().equals(Paths.get(Main.osuRootDir, "osu!.db").toFile()) ||
                        fc.getSelectedFile().equals(Paths.get(Main.osuRootDir, "collection.db").toFile()) ||
                        fc.getSelectedFile().equals(Paths.get(Main.osuRootDir, "presence.db").toFile()) ||
                        fc.getSelectedFile().equals(Paths.get(Main.osuRootDir, "scores.db").toFile())) {
                        JOptionPane.showMessageDialog(frame, "Do NOT overwrite any of your osu .db files!",
                                                      ":|", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        Thread thread = new Thread(() -> {
                            swapToGenerateProgressBar();
                            File file = fc.getSelectedFile();
                            if (!file.getName().endsWith(".db"))
                                file = new File(file.getAbsolutePath() + ".db");
                            System.out.println(file.getAbsolutePath());
                            try {
                                if (Main.generateFromOsuDb(this, file)) {
                                    JOptionPane.showMessageDialog(frame, "Collections successfully generated!\n" +
                                                                         "You can open the database file with CollectionManager\n" +
                                                                         "to add them to your osu collections.", ":)",
                                                                  JOptionPane.PLAIN_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(frame, "Error :(");
                                }
                            } catch (FileNotFoundException fileNotFoundException) {
                                JOptionPane.showMessageDialog(frame, "Error :(");
                            }
                            swapToButtonGenerate();
                        });
                        thread.start();
                    }
                }

            } else {
                JOptionPane.showMessageDialog(frame, "One or more of the text field values are incorrect.",
                                              ":(", JOptionPane.ERROR_MESSAGE);
            }

        });

        readProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        readProgressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
        readProgressBar.setMaximum(100);
        readProgressBar.setValue(0);
        readProgressBar.setFocusable(false);
        readProgressBar.setStringPainted(true);
        readProgressBar.setString("reading osu!.db - 0%");

        generateProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        generateProgressBar.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
        generateProgressBar.setMaximum(100);
        generateProgressBar.setValue(0);
        generateProgressBar.setFocusable(false);
        generateProgressBar.setStringPainted(true);
        generateProgressBar.setString("generating collections - 0%");
        generateProgressBar.setVisible(false);

        rightPanel.add(readProgressBar, BorderLayout.SOUTH);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints cbd = new GridBagConstraints();
        cbd.fill = GridBagConstraints.BOTH;
        cbd.weightx = .5;
        cbd.weighty = .5;

        frame.add(rootPanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        rootPanel.add(topPanel, BorderLayout.NORTH);
        rootPanel.add(scrollPane, BorderLayout.CENTER);

        cbd.gridx = 0;
        topPanel.add(leftPanel, BorderLayout.LINE_START);
        cbd.gridx++;
        topPanel.add(rightPanel, BorderLayout.CENTER);
        cbd.gridy = 1;

        NumberFormat format = NumberFormat.getInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(0);
        formatter.setMaximum(Integer.MAX_VALUE);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);

        textFields = new ArrayList<>();
        new ArrayList<>(Main.userVariables.values()).stream().sorted(Comparator.comparingInt(k -> k.orderInUi)).
                forEach(userVariable -> {
                    JTextField textField = new JTextField();
                    textField.setColumns(4);
                    textField.setHorizontalAlignment(SwingConstants.CENTER);
                    if (userVariable.isDouble)
                        textField.setText(String.valueOf(userVariable.value));
                    else
                        textField.setText(String.valueOf((int) userVariable.value));
                    JPanel panel = new JPanel(new BorderLayout());
                    JLabel label = new JLabel("❔ " + userVariable.name + ": ");
                    panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                    label.setToolTipText(userVariable.description);
                    panel.add(label, BorderLayout.LINE_START);
                    panel.add(textField, BorderLayout.LINE_END);
                    leftPanel.add(panel);
                    textFields.add(textField);
                });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setMinimumSize(frame.getSize());
        //frame.setResizable(false);

        //com.luzian.beatmap.Info beatmapInfo = new Info();
        //beatmapInfo.title = "Title";
        //beatmapInfo.artist = "Artist";
        //beatmapInfo.creator = "Creator";
        //beatmapInfo.difficulty = "Difficulty";
        //beatmapInfoPanel.show(beatmapInfo);
    }

    public void swapToButtonGenerate() {
        SwingUtilities.invokeLater(() -> {
            readProgressBar.setVisible(false);
            buttonGenerate.setVisible(true);
            generateProgressBar.setVisible(false);
            rightPanel.add(buttonGenerate, BorderLayout.SOUTH);
            frame.pack();
        });
    }

    public void swapToGenerateProgressBar() {
        SwingUtilities.invokeLater(() -> {
            readProgressBar.setVisible(false);
            buttonGenerate.setVisible(false);
            generateProgressBar.setVisible(true);
            rightPanel.add(generateProgressBar, BorderLayout.SOUTH);
            frame.pack();
        });
    }

    public void setReadProgressbarValue(int percentage) {
        SwingUtilities.invokeLater(() -> {
            readProgressBar.setValue(percentage);
            readProgressBar.setString("reading osu!.db - " + percentage + "%");
        });
    }

    public void setGenerateProgressbarValue(int percentage) {
        SwingUtilities.invokeLater(() -> {
            generateProgressBar.setValue(percentage);
            generateProgressBar.setString("generating collections " + percentage + "%");
        });
    }

    class DragDropPanel extends JPanel {
        public DragDropPanel() {
            setLayout(new BorderLayout());
            JLabel label = new JLabel("<html>Drop a .osu file here " +
                                      "<br>to analyze it with the" +
                                      "<br>current settings.");
            label.setVerticalAlignment(SwingConstants.BOTTOM);
            label.setFont(label.getFont().deriveFont(Font.ITALIC));
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            label.setVerticalTextPosition(SwingConstants.CENTER);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            setDropTarget(new DropTarget() {
                public synchronized void drop(DropTargetDropEvent evt) {
                    try {
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        java.util.List<File> droppedFiles = (List<File>) evt.getTransferable().
                                getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : droppedFiles) {
                            if (file.isFile() && file.getName().endsWith(".osu")) {
                                if (saveVariables()) {
                                    if (!scrollPane.isVisible()) {
                                        scrollPane.setVisible(true);
                                        frame.pack();
                                    }
                                    if (!textArea.getText().isEmpty())
                                        textArea.append("\n\n");
                                    textArea.append(file.getName());

                                    BeatmapInfo beatmapInfo = Analyzer.analyze(file);
                                    int printStreams = (int) Main.userVariables.get(UserVariable.PRINT_STREAMS).value;
                                    int printStreamThreshold = (int) Main.userVariables.get(UserVariable.PRINT_STREAM_THRESHOLD).value;

                                    if (printStreams == 1) {
                                        for (Stream stream : beatmapInfo.streams) {
                                            if (stream.getTotalHitObjects() >= printStreamThreshold)
                                                textArea.append("\n\t" + stream);
                                        }
                                    }

                                    textArea.append("\n\t" + String.format("%.2f", beatmapInfo.streamPercentage) + "% stream percentage");
                                    textArea.append("\n\t" + String.format("%.2f", beatmapInfo.bpmChangePercentage) + "% bpm change percentage");
                                    textArea.append("\n\t" + String.format("%.2f", beatmapInfo.complexPercentage) + "% complex percentage");

                                    textArea.append("\n\tstreamMap: " + beatmapInfo.bpmMap);
                                    textArea.append("\n\tchangeMap: " + beatmapInfo.changeMap);
                                    textArea.append("\n\tcomplexMap: " + beatmapInfo.complexMap);

                                    textArea.append("\n\tmostCommonBpm: " + beatmapInfo.mostCommonBpm);

                                    textArea.append("\n\tisBpmChange: " + beatmapInfo.isBpmChange);
                                    textArea.append("\n\tisBpmComplex: " + beatmapInfo.isComplex);

                                    textArea.append("\n\t--> isAccepted: " + beatmapInfo.isAccepted);
                                } else {
                                    JOptionPane.showMessageDialog(frame, "One or more of the text field values are incorrect.",
                                                                  ":(", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            add(label, BorderLayout.CENTER);
            setBorder(BorderFactory.createDashedBorder(Color.gray, 10, 10));
        }
    }

    public boolean saveVariables() {
        AtomicBoolean error = new AtomicBoolean(true);
        new ArrayList<>(Main.userVariables.values()).stream().sorted(Comparator.comparingInt(k -> k.orderInUi)).
                forEach(userVariable -> {
                    try {
                        if (userVariable.isDouble) {
                            userVariable.value = Double.parseDouble(textFields.get(userVariable.orderInUi).getText());
                        } else {
                            userVariable.value = Integer.parseInt(textFields.get(userVariable.orderInUi).getText());
                        }
                    } catch (Exception e) {
                        if (error.get())
                            error.set(false);
                    }
                });

        return error.get();
    }
}
