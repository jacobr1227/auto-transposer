package com.guitarTranspose;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Guitar {
    public static void main(String[] args) {

        Map<Integer, String> notes = new HashMap<>();
        notes.put(0, "A"); notes.put(1, "A#"); notes.put(2, "B");
        notes.put(3, "C"); notes.put(4, "C#"); notes.put(5, "D");
        notes.put(6, "D#"); notes.put(7, "E"); notes.put(8, "F");
        notes.put(9, "F#"); notes.put(10, "G"); notes.put(11, "G#");
        Map<Integer, String> buttons = new HashMap<>();
        buttons.put(0, "n"); buttons.put(1, "j"); buttons.put(2, "m");
        buttons.put(3, "z"); buttons.put(4, "s"); buttons.put(5, "x");
        buttons.put(6, "d"); buttons.put(7, "c"); buttons.put(8, "v");
        buttons.put(9, "g"); buttons.put(10, "b"); buttons.put(11, "h");
        List<String> result = new ArrayList<>();
        List<String> inputs = new ArrayList<>();
        String filename = "noteSequence_" + System.currentTimeMillis();
        //System.out.println("Welcome to the transposer! Would you like to use non-standard tunings (y/n)?\n(Provided is an 8-string guitar in common tuning, F#, B, E, A, D, G, B, E)");
        //TODO: this
        //TODO: Modularize the below section into the "guitar" category, provided alternative transposers for other instruments.
        while(true) {
            try {
                Scanner sysin = new Scanner(System.in);
                System.out.println("Press 9 to save/quit.\nNumber of notes written: " + result.size());
                System.out.println("Choose a string:\n1. e\n2. b\n3. G\n4. D\n5. A\n6. E\n7. B\n8. F#");
                int string;
                string = Integer.parseInt(sysin.nextLine());
                int baseNote;
                int octave;
                switch (string) {
                    case 1:
                        baseNote = 7;
                        octave = 4;
                        break;
                    case 2:
                        baseNote = 2;
                        octave = 3;
                        break;
                    case 3:
                        baseNote = 10;
                        octave = 3;
                        break;
                    case 4:
                        baseNote = 5;
                        octave = 3;
                        break;
                    case 5:
                        baseNote = 0;
                        octave = 2;
                        break;
                    case 6:
                        baseNote = 7;
                        octave = 2;
                        break;
                    case 7:
                        baseNote = 2;
                        octave = 2;
                        break;
                    case 8:
                        baseNote = 9;
                        octave = 1;
                        break;
                    case 9:
                        sysin.close();
                        if (!result.isEmpty()) {
                            writeFile(filename, result, inputs);
                            System.out.println("Note sequence output to file successfully.");
                            System.out.println("File saved as " + filename + ".");
                        }
                        System.exit(0);
                    default:
                        sysin.close();
                        System.out.println("Invalid character.");
                        continue;
                }
                System.out.println("Type the fret number: ");
                int fret;
                fret = sysin.nextInt();
                octave = (baseNote + (12 * octave) + fret) / 12;
                int finalNote = (baseNote + fret) % 12;
                result.add(notes.get(finalNote) + octave);
                inputs.add(buttons.get(finalNote) + ((octave>=3) ? "+" + (octave-3) : (octave-3)));
            }
            catch(Exception e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static void writeFile(String filename, List<String> result, List<String> inputs) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write("Notes:\n");
        for (String s : result) {
            fw.write(s + " ");
        }
        fw.write("\nFTI Buttons:\n");
        for (String input : inputs) {
            fw.write(input + " ");
        }
        fw.close();
    }
}