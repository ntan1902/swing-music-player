import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

public class SwingAudioPlayer implements ActionListener {
    private final JFrame frame = new JFrame();
    private final JPanel panelOptions = new JPanel();
    private final JButton buttonStop = new JButton("Stop");
    private final JButton buttonPlay = new JButton("Play");
    private final JButton buttonPause = new JButton("Pause");
    private final JButton buttonNext = new JButton("Next");
    private final JButton buttonPrior = new JButton("Prior");
    private final JButton buttonOpen = new JButton("Open");
    private final JPanel panelPlayTimer = new JPanel();
    private final JSlider slider = new JSlider();
    private final JLabel labelTimeCounter = new JLabel("00:00:00");
    private final JLabel labelDuration = new JLabel("00:00:00");
    private final JLabel labelFileName = new JLabel("");

    private final JTable table = new JTable() {
        @Override
        public boolean editCellAt(int row, int column, java.util.EventObject e) {
            return false;
        }
    };
    JScrollPane scrollPane = new JScrollPane();
    // Icons used for buttons
    private final ImageIcon iconPrior = new ImageIcon(getClass().getResource("/images/Prior.png"));
    private final ImageIcon iconNext = new ImageIcon(getClass().getResource("/images/Next.png"));
    private final ImageIcon iconPlay = new ImageIcon(getClass().getResource("/images/Play.gif"));
    private final ImageIcon iconStop = new ImageIcon(getClass().getResource("/images/Stop.gif"));
    private final ImageIcon iconPause = new ImageIcon(getClass().getResource("/images/Pause.png"));

    private boolean isPlaying = false;
    private boolean isPause = false;
    private int rowSelected;
    private String audioFilePath;
    private String directoryPath;
    private File[] audioFiles;
    private PlayingTimer timer;
    private AudioPlayer player = new AudioPlayer();
    private Thread playbackThread;

    public SwingAudioPlayer() {
        initialize();
    }

    private void initialize() {
        frame.setSize(800, 376);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        panelOptions.setBounds(0, 0, 784, 62);
        panelOptions.setBackground(Color.DARK_GRAY);
        frame.getContentPane().add(panelOptions);
        panelOptions.setLayout(null);

        buttonStop.setBounds(384, 21, 90, 30);
        buttonStop.setFont(new Font("Sans", Font.BOLD, 12));
        buttonStop.setIcon(iconStop);
        panelOptions.add(buttonStop);

        buttonPlay.setBounds(484, 21, 90, 30);
        buttonPlay.setFont(new Font("Sans", Font.BOLD, 12));
        buttonPlay.setIcon(iconPlay);
        panelOptions.add(buttonPlay);

        buttonPause.setBounds(584, 21, 90, 30);
        buttonPause.setFont(new Font("Sans", Font.BOLD, 12));
        buttonPause.setIcon(iconPause);
        panelOptions.add(buttonPause);

        buttonNext.setBounds(684, 21, 90, 30);
        buttonNext.setFont(new Font("Sans", Font.BOLD, 12));
        buttonNext.setIcon(iconNext);
        panelOptions.add(buttonNext);

        buttonPrior.setBounds(284, 21, 90, 30);
        buttonPrior.setFont(new Font("Sans", Font.BOLD, 12));
        buttonPrior.setIcon(iconPrior);
        panelOptions.add(buttonPrior);

        buttonOpen.setBounds(10, 21, 102, 30);
        buttonOpen.setFont(new Font("Sans", Font.BOLD, 12));
        buttonOpen.setIcon(new MetalIconFactory.TreeFolderIcon());
        panelOptions.add(buttonOpen);

        panelPlayTimer.setBounds(0, 63, 784, 73);
        panelPlayTimer.setBackground(Color.GRAY);
        frame.getContentPane().add(panelPlayTimer);
        panelPlayTimer.setLayout(null);

        slider.setBounds(174, 36, 448, 26);
        slider.setEnabled(false);
        slider.setValue(0);
        panelPlayTimer.add(slider);

        labelTimeCounter.setHorizontalAlignment(SwingConstants.CENTER);
        labelTimeCounter.setBounds(109, 36, 55, 26);
        panelPlayTimer.add(labelTimeCounter);

        labelDuration.setHorizontalAlignment(SwingConstants.CENTER);
        labelDuration.setBounds(632, 36, 55, 26);
        panelPlayTimer.add(labelDuration);

        labelFileName.setHorizontalAlignment(SwingConstants.CENTER);
        labelFileName.setBounds(176, 11, 446, 19);
        panelPlayTimer.add(labelFileName);

        scrollPane.setBounds(0, 136, 784, 376);
        scrollPane.setViewportView(table);
        frame.getContentPane().add(scrollPane);

        buttonOpen.addActionListener(this);

        buttonPrior.addActionListener(this);
        buttonPrior.setEnabled(false);

        buttonPlay.addActionListener(this);
        buttonPlay.setEnabled(false);

        buttonNext.addActionListener(this);
        buttonNext.setEnabled(false);

        buttonPause.addActionListener(this);
        buttonPause.setEnabled(false);

        buttonStop.addActionListener(this);
        buttonStop.setEnabled(false);

        frame.setVisible(true);
    }

    /**
     * Handle click events on the buttons.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source instanceof JButton) {
            JButton button = (JButton) source;
            if (button == buttonOpen) {
                openFile();
            }
            if (button == buttonPlay) {
                int index = table.getSelectedRow();
                if (index >= 0) {
                    if (isPause) {
                        if (rowSelected != index) {
                            audioFilePath = audioFiles[index].getAbsolutePath();
                            playBack();
                        } else {
                            resumePlaying();
                        }
                    } else {
                        if (isPlaying || isPause) {
                            stopPlaying();
                            while (player.getAudioClip().isRunning()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        audioFilePath = audioFiles[index].getAbsolutePath();
                        playBack();
                    }
                    rowSelected = index;
                }

            }
            if (button == buttonPause) {
                if (!isPause) {
                    pausePlaying();
                }
            }
            if (button == buttonStop) {
                stopPlaying();

            }
            if (button == buttonNext) {
                int index = table.getSelectedRow();
                if (index == audioFiles.length - 1) {
                    index = 0;
                } else {
                    index++;
                }
                table.setRowSelectionInterval(index, index);
                buttonPlay.doClick();

            }
            if (button == buttonPrior) {
                int index = table.getSelectedRow();
                if (index == 0) {
                    index = audioFiles.length - 1;
                } else {
                    index--;
                }
                table.setRowSelectionInterval(index, index);
                buttonPlay.doClick();
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = null;
        fileChooser = new JFileChooser(".");

        fileChooser.setDialogTitle("Open folder");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // disable the "All files" option.
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(null);

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            System.out.println("getCurrentDirectory(): " + fileChooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + fileChooser.getSelectedFile());

            directoryPath = fileChooser.getSelectedFile().getAbsolutePath();
            File f = new File(directoryPath);
            // This filter will only include files ending with .wav
            FilenameFilter filter = (f1, name) -> name.endsWith(".wav");

            // This is how to apply the filter
            audioFiles = f.listFiles(filter);
            createTable();
            buttonPlay.setEnabled(true);
            buttonPrior.setEnabled(true);
            buttonNext.setEnabled(true);

        } else {
            System.out.println("No Selection ");
        }
    }

    public void createTable() {
        Vector<String> header = new Vector<>();
        header.add("Title");
        header.add("Date modified");
        header.add("Type");
        header.add("Size (KB)");
        header.add("Time");

        Vector<Vector<?>> data = new Vector<>();

        for (File audioFile : audioFiles) {
            Vector<Object> row = new Vector<>();
            row.add(audioFile.getName().replace(".wav", ""));
            row.add(new Date(audioFile.lastModified()));
            row.add("WAV File");
            row.add(audioFile.length() / 1_000);

            try {
                player.load(audioFile.getAbsolutePath());
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
            row.add(player.getClipLengthString());

            data.add(row);
        }

        table.setModel(new DefaultTableModel(data, header));
        table.changeSelection(0, 0, false, false);

    }

    private void playBack() {
        timer = new PlayingTimer(labelTimeCounter, slider);
        timer.start();
        isPlaying = true;
        isPause = false;
        playbackThread = new Thread(() -> {
            try {
                buttonPause.setEnabled(true);
                buttonStop.setEnabled(true);

                player.load(audioFilePath);
                timer.setAudioClip(player.getAudioClip());
                labelFileName.setText("Playing File: " + audioFiles[rowSelected].getName());
                slider.setMaximum((int) player.getClipSecondLength());

                labelDuration.setText(player.getClipLengthString());
                player.play();

                resetControls();

            } catch (UnsupportedAudioFileException ex) {
                JOptionPane.showMessageDialog(frame, "The audio format is unsupported!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                resetControls();
                ex.printStackTrace();
            } catch (LineUnavailableException ex) {
                JOptionPane.showMessageDialog(frame, "Could not play the audio file because line is unavailable!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                resetControls();
                ex.printStackTrace();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "I/O error while playing the audio file!", "Error",
                        JOptionPane.ERROR_MESSAGE);
                resetControls();
                ex.printStackTrace();
            }

        });

        playbackThread.start();
    }

    private void resumePlaying() {
        isPause = false;
        buttonPause.setEnabled(true);
        player.resume();
        timer.resumeTimer();
        playbackThread.interrupt();
    }

    private void pausePlaying() {
        isPause = true;
        buttonPause.setEnabled(false);
        player.pause();
        timer.pauseTimer();
        playbackThread.interrupt();
    }

    private void stopPlaying() {
        isPause = false;
        buttonPause.setEnabled(false);
        buttonStop.setEnabled(false);

        timer.reset();
        timer.interrupt();
        player.stop();
        playbackThread.interrupt();
    }

    private void resetControls() {
        timer.reset();
        timer.interrupt();
        isPlaying = false;
    }

}
