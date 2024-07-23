import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class Viz {

    //TODO: Figure out why the labels/fret boxes become ever so slightly longer when the label changes/repaints

    //CONFIG VARIABLES ****************************
    private static boolean noAutoClear = false;
    private static String lastSaveLocation;
    private static File selectedFile;
    private static Point locationOnScreen = null;
    private static JFrame frameGlobal;
    // ********************************************

    private static final Map<Integer, String> notes = new HashMap<>(),
            buttons = new HashMap<>(),
            stringNames = new HashMap<>();
    private static final Map<String, Integer> notesToNums = new HashMap<>();
    private static final Map<String, Object> componentMap = new HashMap<>();
    private static final Map<Integer, Integer> stringNotes = new HashMap<>(),
            stringOctaves = new HashMap<>();
    private static final List<String> result = new ArrayList<>(),
            inputs = new ArrayList<>(), rTemp = new ArrayList<>(), iTemp = new ArrayList<>();
    private static boolean firstUpdate = true;
    private static final List<Integer> chordSizes = new ArrayList<>();
    private static int chordSize = 0;

    /**
     * Adds all necessary components to the frame object's content pane.
     * @param frame The main JFrame's ContentPane
     */
    public static void frameComponents(Container frame) {
        frame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel label;
        JFormattedTextField fTField;
        
        label = new JLabel("String");
        label.setName("StringCol");
        componentMap.put(label.getName(), label);
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 10;
        c.insets = new Insets(5, 5, 5, 5);
        frame.add(label, c);
        
        label = new JLabel("Fret");
        label.setName("FretCol");
        componentMap.put(label.getName(), label);
        c.gridx = 1;
        frame.add(label, c);
        
        label = new JLabel("Current Notes:");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setName("CurrentNotes");
        componentMap.put(label.getName(), label);
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 2;
        frame.add(label, c);

        JTextField tField = new JTextField(35);
        tField.setName("CurrentNotes");
        tField.setEnabled(false);
        tField.setDisabledTextColor(new Color(100, 100, 255));
        tField.setFont(new Font("Dialog", Font.BOLD, 12));
        componentMap.put(tField.getName(), tField);
        c.gridx = 3;
        frame.add(tField, c);

        JButton button = getjButton();
        button.setName("Submit");
        componentMap.put(button.getName(), button);
        c.gridx = 2;
        c.gridy = 1;
        c.gridheight = 3;
        button.setText("<html><p>Add Notes</p><p>to History</p></html>");
        frame.add(button, c);

        JButton button2 = new JButton("Clear Fret Entries");
        button2.addActionListener(e -> {
            for(int i=1;i<=8;i++) {
                JFormattedTextField obj = (JFormattedTextField) componentMap.get("Fret"+i);
                obj.setValue(null);
            }
        });
        button2.setName("ClearFrets");
        componentMap.put(button2.getName(), button2);
        c.gridx = 2;
        c.gridy = 2;
        c.gridheight = 5;
        frame.add(button2, c);

        label = new JLabel("History:");
        label.setName("History");
        componentMap.put(label.getName(), label);
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 3;
        c.gridy = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.WEST;
        frame.add(label, c);

        JTextArea history = new JTextArea(10, 35);
        history.setEditable(false);
        history.setLineWrap(false);
        history.setVisible(true);
        history.setName("History");
        history.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        componentMap.put(history.getName(), history);
        history.setText("Note History will appear here...");
        JScrollPane scroll = new JScrollPane(history);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 3;
        c.gridy = 2;
        c.gridheight = 8;
        frame.add(scroll, c);

        Map<Integer, String> map = new HashMap<>();
        for(int i=1;i<=8;i++) {
            map.put(i, stringNames.get(i));
        }
        NumberFormatter nFormatter = getNumberFormatter();

        for(int i=1;i<=8;i++) {
            label = new JLabel(map.get(i));
            label.setName("String" + i);
            componentMap.put(label.getName(), label);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = i;
            c.gridheight = 1;
            c.insets = new Insets(5,5,5,10);
            frame.add(label, c);

            fTField = new JFormattedTextField(nFormatter);
            fTField.setValue(null);
            fTField.setColumns(2);
            fTField.setName("Fret" + i);
            componentMap.put(fTField.getName(), fTField);
            fTField.addActionListener(e -> {
                StringBuilder newText = new StringBuilder();
                JTextField JTF = (JTextField) componentMap.get("CurrentNotes");
                rTemp.clear();
                iTemp.clear();
                chordSize = 0;
                for(int a=1;a<=8;a++) {
                    JFormattedTextField ftf = (JFormattedTextField) componentMap.get("Fret"+a);
                    String note = calcNote(ftf.getText(),a);
                    if(!note.isEmpty()) {
                        if(a!=8) {
                            note += " ";
                        }
                        chordSize++;
                    }
                    newText.append(note);
                }
                JTF.setText(newText.toString());
            });
            fTField.addFocusListener(new TextFocusListener(fTField));
            c.gridx = 1;
            c.gridy = i;
            c.insets = new Insets(5,5,5,5);
            frame.add(fTField, c);
        }
    }

    /**
     * Auto-generated function. Returns the submission button + its action listener.
     * @return A JButton object with an ActionListener registered.
     */
    private static JButton getjButton() {
        JButton button = new JButton();
        button.addActionListener(e -> {
            JTextField cn = (JTextField) componentMap.get("CurrentNotes");
            String line = cn.getText();
            result.addAll(rTemp);
            inputs.addAll(iTemp);
            chordSizes.add(chordSize);
            if(!noAutoClear) {
                cn.setText("");
                for(int i=1;i<=8;i++) {
                    JFormattedTextField ftf = (JFormattedTextField) componentMap.get("Fret"+i);
                    ftf.setValue(null);
                }
                iTemp.clear();
                rTemp.clear();
                chordSize = 0;
            }
            JTextArea history = (JTextArea) componentMap.get("History");
            if (firstUpdate) {
                history.setText(line + "\n");
                firstUpdate = false;
            } else {
                history.append(line + "\n");
            }
        });
        return button;
    }

    /**
     * Calculates and returns a note string.
     * This function is null-safe when computing empty strings and NaN values.
     * @param fieldText A text string containing an integer.
     * @param sNum The string number (1-8)
     * @return A String of the format [1-2 Letters][Integer]
     */
    private static String calcNote(String fieldText, int sNum) {
        try {
            int fret = Integer.parseInt(fieldText);
            int baseNote = stringNotes.get(sNum);
            int octave = (baseNote + (12*stringOctaves.get(sNum)) + fret) /12;
            int finalNote = (baseNote + fret) % 12;
            rTemp.add(notes.get(finalNote) + octave);
            iTemp.add(buttons.get(finalNote) + ((octave>=3) ? "+" + (octave-3) : (octave-3)));
            return notes.get(finalNote) + octave;
        } catch(NumberFormatException e) {
            return "";
        }
    }

    /**
     * Initializes, enables, and creates all the Menu objects for the frame's MenuBar
     * @param frame The JFrame window object
     */
    public static void setMenu(JFrame frame) {
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem cls = file.add(new JMenuItem("Clear History"));
        cls.addActionListener(e -> {
            JTextArea history = (JTextArea) componentMap.get("History");
            history.setText("Note History will appear here...");
            inputs.clear();
            result.clear();
            firstUpdate = true;
        });
        file.addSeparator();
        JMenuItem save = file.add(new JMenuItem("Save"));
        save.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save");
        save.getActionMap().put("Save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null && selectedFile.exists()) {
                    write(selectedFile);
                } else {
                    save();
                }
            }
        });
        save.addActionListener(e -> {
            if (selectedFile != null && selectedFile.exists()) {
                write(selectedFile);
            } else {
                save();
            }
        });
        JMenuItem saveAs = file.add(new JMenuItem("Save As..."));
        saveAs.addActionListener(e -> save());
        file.addSeparator();
        JMenuItem exit = file.add(new JMenuItem("Exit Without Saving"));
        exit.addActionListener(e -> System.exit(0));
        menu.add(file);

        JMenu opt = new JMenu("Options");
        JMenuItem tune = opt.add(new JMenuItem("Change Tunings..."));
        tune.addActionListener(e -> tuningDialog(frame));
        JCheckBoxMenuItem noReset = new JCheckBoxMenuItem("Disable Automatic Fret Clear");
        noReset.setState(noAutoClear);
        noReset.addItemListener(e -> {
            noAutoClear = !noAutoClear;
            noReset.setSelected(noAutoClear);
            writeConfig();
        });
        opt.add(noReset);
        JMenuItem def = opt.add(new JMenuItem("Restore Default Settings"));
        def.addActionListener(e -> {
           if(noAutoClear) {
               noReset.doClick();
           }
           resetMaps(false);
           for(int i=1;i<=8;i++) {
               JLabel obj = (JLabel) componentMap.get("String" + i);
               obj.setText(stringNames.get(i));
               obj.repaint();
               frame.pack();
           }
           writeConfig();
        });
        menu.add(opt);
        frame.setJMenuBar(menu);
    }

    /**
     * Opens a save as dialog box and has the user attempt to select a file to save to.
     */
    public static void save() {
        if(result.isEmpty()) {
            JOptionPane.showMessageDialog(frameGlobal, "No note history to save!",
                    "Notice", JOptionPane.WARNING_MESSAGE);
        }
        else {
            JFileChooser fC = getjFileChooser();
            if (fC.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fC.getSelectedFile();
                String n = selectedFile.getName();
                if(!n.matches(".+\\.[tT][xX][tT]$")) {
                    selectedFile = new File(selectedFile.getName() + ".txt");
                }
                lastSaveLocation = selectedFile.getAbsolutePath();
                writeConfig();
                write(selectedFile);
            }
        }
    }

    private static JFileChooser getjFileChooser() {
        JFileChooser fC = new JFileChooser() {
            @Override
            public void approveSelection(){
                File f = getSelectedFile();
                if(f.exists() && getDialogType() == SAVE_DIALOG){
                    int result = JOptionPane.showConfirmDialog(this,
                            "This will overwrite an existing file, continue?",
                            "Overwrite",JOptionPane.YES_NO_CANCEL_OPTION);
                    switch(result){
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        default:
                            return;
                    }
                }
                super.approveSelection();
            }
        };

        fC.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".txt") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "*.txt, *.TXT";
            }
        });
        fC.setSelectedFile(new File(lastSaveLocation));
        return fC;
    }

    /**
     * Writes information from the "History" box to a file.
     * @param file The file to write to
     */
    public static void write(File file) {
        try(FileWriter fw = new FileWriter(file)) {
            fw.write("Notes:\n");
            int i = 0;
            int count = 1;
            for (String s : result) {
                if(count ==1) {
                    fw.write("| ");
                }
                fw.write(s + " ");
                if(count == chordSizes.get(i)) {
                    i++;
                    count = 0;
                    fw.write("|, ");
                }
                count++;
            }
            i=0;
            count=1;
            fw.write("\nFTI Buttons:\n");
            for (String input : inputs) {
                if(count ==1) {
                    fw.write("| ");
                }
                fw.write(input + " ");
                if(count == chordSizes.get(i)) {
                    i++;
                    count = 0;
                    fw.write("|, ");
                }
                count++;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frameGlobal,
                    "Save failed. Please try again.","Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Dialog box for adjusting the native string tunings.
     * @param frame The JFrame parent window.
     */
    public static void tuningDialog(JFrame frame) {
        GridBagConstraints c = new GridBagConstraints();
        JDialog dialog = new JDialog(frame, "Change Tunings...");
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container dcp = dialog.getContentPane();
        dcp.setLayout(new GridBagLayout());
        JLabel label = new JLabel("<html><p>Note: This software will never output</p>" +
                "<p>flat notes in the conversions.</p></html>");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,5,5,5);
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        dcp.add(label, c);
        label = new JLabel("<html><p>Current</p><p>String</p></html>");
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        dcp.add(label, c);
        label = new JLabel("<html><p>New</p><p>String</p></html>");
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        c.gridx = 1;
        dcp.add(label, c);
        for(int i=1;i<=8;i++) {
            label = new JLabel(stringNames.get(i));
            label.setHorizontalTextPosition(SwingConstants.CENTER);
            c.gridx = 0;
            c.gridy = i+1;
            dcp.add(label, c);
            JTextField jtf = new JTextField();
            jtf.setColumns(3);
            jtf.addFocusListener(new TextFocusListener(jtf));
            jtf.setName("dString" + i);
            componentMap.put(jtf.getName(), jtf);
            c.gridx = 1;
            dcp.add(jtf, c);
        }
        JButton button = new JButton("Cancel");
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.addActionListener(e -> dialog.dispose());
        c.gridx = 0;
        c.gridy = 10;
        dcp.add(button, c);
        button = new JButton("Save Changes");
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.addActionListener(e -> {
            boolean fail = false;
            Map<String, String> newStringMap = new HashMap<>();
            for(int i=1;i<=8;i++) {
                JTextField obj = (JTextField) componentMap.get("dString"+i);
                String s = obj.getText();
                if(s.matches("^[a-gA-G][b#]?\\d+$")) {
                    String noteName = obj.getText().substring(0, 1).toUpperCase();
                    String rest = obj.getText().substring(1);
                    s = noteName + rest;
                    //An index for finding the location of the numerical portion of the note.
                    int numIndex = 1;
                    if(s.contains("#") || s.substring(1).contains("b")) {
                        numIndex++;
                    }
                    newStringMap.put("t" + i, s); //Includes word string as-is
                    newStringMap.put("o" + i, s.substring(numIndex)); //Includes only the numbers at the end
                    String current = s.substring(0, numIndex);
                    boolean replace = false;
                    if(notesToNums.containsKey(current)) {
                        replace = true;
                        newStringMap.put("n"+i, notesToNums.get(current).toString());
                    }
                    if(!replace) {
                        fail = true;
                    }
                }
                else if(s.isEmpty()) {
                    newStringMap.put("t"+i, stringNames.get(i));
                    newStringMap.put("o"+i, stringOctaves.get(i).toString());
                    newStringMap.put("n"+i, stringNotes.get(i).toString());
                }
                else {
                    fail = true;
                }
            }
            if(!fail) {
                for(int i=1;i<=8;i++) {
                    stringNames.replace(i, newStringMap.get("t" + i));
                    try {
                        stringOctaves.replace(i, Integer.parseInt(newStringMap.get("o" + i)));
                        stringNotes.replace(i, Integer.parseInt(newStringMap.get("n" + i)));
                    } catch(NumberFormatException e2) {
                        System.out.println("Number handling error: Non-parsable Int: " +
                                newStringMap.get("o" + i) + " or " + newStringMap.get("n" + i));
                        throw e2;
                    }
                    JLabel obj = (JLabel) componentMap.get("String"+i);
                    obj.setText(stringNames.get(i));
                    obj.repaint();
                    frame.pack();
                }
                writeConfig();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        "<html><p>Please use valid notes in the text fields! Eg. A1, Bb2, " +
                        "G#7</p><p>Please ensure you use a lowercase 'b' for flat notes.</p></html>",
                        "Error!", JOptionPane.ERROR_MESSAGE);
            }
        });
        c.gridx = 1;
        dcp.add(button, c);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    /**
     * Formats numerical TextFields for 2-digit integers only.
     * @return A NumberFormatter object
     */
    private static NumberFormatter getNumberFormatter() {
        NumberFormat nFormat = NumberFormat.getNumberInstance();
        nFormat.setParseIntegerOnly(true);
        nFormat.setMaximumIntegerDigits(2);
        nFormat.setMaximumFractionDigits(0);
        return new NumberFormatter(nFormat);
    }

    /**
     * Creates, initializes, and shows the GUI.
     */
    public static void runGUI() {
        JFrame frame = new JFrame("Auto-Transposer GUI Edition");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        frameGlobal = frame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        readConfig();
        setMenu(frame);
        frameComponents(frame.getContentPane());
        frame.setResizable(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {}
            @Override
            public void componentMoved(ComponentEvent e) {
                locationOnScreen = frame.getLocationOnScreen();
                writeConfig();
            }
            @Override
            public void componentShown(ComponentEvent e) {}
            @Override
            public void componentHidden(ComponentEvent e) {}
        });
        if(locationOnScreen == null) {
            locationOnScreen = frame.getLocationOnScreen();
        }
        else {
            frame.setLocation(locationOnScreen);
        }
    }

    /**
     * Initializes or resets the object maps for the different note lines to manage their calculations.
     * @param init Whether to initialize new maps or replace existing ones. Run 'init' mode only one time.
     */
    public static void resetMaps(boolean init) {
        //Initialize value maps
        if(init) {
            notes.put(0, "A");
            notes.put(1, "A#");
            notes.put(2, "B");
            notes.put(3, "C");
            notes.put(4, "C#");
            notes.put(5, "D");
            notes.put(6, "D#");
            notes.put(7, "E");
            notes.put(8, "F");
            notes.put(9, "F#");
            notes.put(10, "G");
            notes.put(11, "G#");
            notesToNums.put("A", 0);
            notesToNums.put("A#", 1);
            notesToNums.put("Bb", 1);
            notesToNums.put("B", 2);
            notesToNums.put("B#", 3);
            notesToNums.put("Cb", 2);
            notesToNums.put("C", 3);
            notesToNums.put("C#", 4);
            notesToNums.put("Db", 4);
            notesToNums.put("D", 5);
            notesToNums.put("D#", 6);
            notesToNums.put("Eb", 6);
            notesToNums.put("E", 7);
            notesToNums.put("E#", 8);
            notesToNums.put("Fb", 7);
            notesToNums.put("F", 8);
            notesToNums.put("F#", 9);
            notesToNums.put("Gb", 9);
            notesToNums.put("G", 10);
            notesToNums.put("G#", 11);
            notesToNums.put("Ab", 11);
            buttons.put(0, "n");
            buttons.put(1, "j");
            buttons.put(2, "m");
            buttons.put(3, "z");
            buttons.put(4, "s");
            buttons.put(5, "x");
            buttons.put(6, "d");
            buttons.put(7, "c");
            buttons.put(8, "v");
            buttons.put(9, "g");
            buttons.put(10, "b");
            buttons.put(11, "h");
            stringNames.put(1, "E4");
            stringNames.put(2, "B3");
            stringNames.put(3, "G3");
            stringNames.put(4, "D3");
            stringNames.put(5, "A2");
            stringNames.put(6, "E2");
            stringNames.put(7, "B2");
            stringNames.put(8, "F#1");
            stringNotes.put(1, 7);
            stringNotes.put(2, 2);
            stringNotes.put(3, 10);
            stringNotes.put(4, 5);
            stringNotes.put(5, 0);
            stringNotes.put(6, 7);
            stringNotes.put(7, 2);
            stringNotes.put(8, 9);
            stringOctaves.put(1, 4);
            stringOctaves.put(2, 3);
            stringOctaves.put(3, 3);
            stringOctaves.put(4, 3);
            stringOctaves.put(5, 2);
            stringOctaves.put(6, 2);
            stringOctaves.put(7, 2);
            stringOctaves.put(8, 1);
        }
        //reset value maps to defaults
        else {
            stringNames.replace(1, "E4"); stringNames.replace(2, "B3"); stringNames.replace(3, "G3");
            stringNames.replace(4, "D3"); stringNames.replace(5, "A2"); stringNames.replace(6, "E2");
            stringNames.replace(7, "B2"); stringNames.replace(8, "F#1");
            stringNotes.replace(1, 7); stringNotes.replace(2, 2); stringNotes.replace(3, 10);
            stringNotes.replace(4, 5); stringNotes.replace(5, 0); stringNotes.replace(6, 7);
            stringNotes.replace(7, 2); stringNotes.replace(8, 9);
            stringOctaves.replace(1, 4); stringOctaves.replace(2, 3); stringOctaves.replace(3, 3);
            stringOctaves.replace(4, 3); stringOctaves.replace(5, 2); stringOctaves.replace(6, 2);
            stringOctaves.replace(7, 2); stringOctaves.replace(8, 1);
        }
    }

    /**
     * Reads the application config file if it exists.
     */
    public static void readConfig() {
        File f = new File("config.properties");
        if(f.exists()) {
            Properties props = new Properties();
            Point p = new Point();
            try(FileReader fr = new FileReader(f)) {
                props.load(fr);
                noAutoClear = Boolean.parseBoolean(props.getProperty("noAutoClear")); //Double check that this property is assigned on creation!
                lastSaveLocation = props.getProperty("lastSaveLocation");
                p.setLocation(Double.parseDouble(props.getProperty("locationOnScreenX")), Double.parseDouble(props.getProperty("locationOnScreenY")));
                locationOnScreen = p.getLocation();
                for(int i=1;i<=8;i++) {
                    stringNames.replace(i, props.getProperty("StringName"+i));
                    stringOctaves.replace(i, Integer.parseInt(props.getProperty("StringOctave"+i)));
                    stringNotes.replace(i, Integer.parseInt(props.getProperty("StringNote"+i)));
                }
            } catch(Exception e) {
                JFrame errorFrame = new JFrame();
                errorFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                errorFrame.setVisible(true);
                JOptionPane.showMessageDialog(errorFrame, "Failed to find a valid config file. Creating a new one...", "Warning", JOptionPane.WARNING_MESSAGE);
                errorFrame.dispose();
            }
        }
    }

    /**
     * Writes the application config file. To be run whenever a config-tagged variable is set.
     */
    public static void writeConfig() {
        File f = new File("config.properties");
        Properties props = new Properties();
        try(FileWriter fw = new FileWriter(f, false)) {
            props.setProperty("noAutoClear", Boolean.toString(noAutoClear));
            if(lastSaveLocation == null) {
                lastSaveLocation = "";
            }
            props.setProperty("lastSaveLocation", lastSaveLocation);
            props.setProperty("locationOnScreenX", Double.toString(locationOnScreen.getX()));
            props.setProperty("locationOnScreenY", Double.toString(locationOnScreen.getY()));
            for(int i=1;i<=8;i++) {
                props.setProperty("StringName"+i, stringNames.get(i));
                props.setProperty("StringOctave"+i, stringOctaves.get(i).toString());
                props.setProperty("StringNote"+i, stringNotes.get(i).toString());
            }
            props.store(fw, "For use with AT-viz auto-transposer.");
        } catch(Exception e) {
            JOptionPane.showMessageDialog(frameGlobal, "An exception has occurred. Please contact the developer for information.", "Error!", JOptionPane.ERROR_MESSAGE);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    /**
     * Initialization vector + Runnable
     * @param args Not used.
     */
    public static void main(String[] args) {
        resetMaps(true);
        //Thread-safe event dispatcher
        SwingUtilities.invokeLater(Viz::runGUI);
    }
}