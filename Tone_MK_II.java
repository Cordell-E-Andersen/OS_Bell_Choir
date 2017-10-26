import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author Cordell
 *
 */
public class Tone_MK_II {

    // Mary had a little lamb
	/**
	 * will become what the file reads in
	 */
    private static List<BellNote> song;/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        										//MAIN
    public static void main(String[] args) throws Exception {
        System.out.println("ENTERED TONEDOM");
    	final AudioFormat af =
            new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
        Tone_MK_II t = new Tone_MK_II(af);
        song = compileSong(args[0]);//new ArrayList<BellNote>();
        
        /*for(int i=0;i<args.length;i++) {
        	System.out.println(args[i]);
        }*/
        t.playSong(song);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 
     * @param fileName
     * @return
     */
    public static List<BellNote> compileSong(String fileName){
    	List<BellNote> theSong = new ArrayList<BellNote>();
    	
    	File file = new File(fileName);
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file); 
                 BufferedReader br = new BufferedReader(fileReader)) { //try with resources - auto calls close and throws exceptions that may occur to the catch
                String line = null;
                while ((line = br.readLine()) != null) {
                    BellNote bn = parseNote(line);
                    if (bn != null) {
                    	theSong.add(bn);
                    } else {
                        System.err.println("Error: Invalid input '" + line + "'");
                    }
                }
            } catch (IOException ignored) {}
        } else {
            System.err.println("File '" + fileName + "' not found");
        }
    	
    	return theSong;
    }
    
    
    //tictactoeV2
    /**
     * 
     * @param line
     * @return
     */
    private static BellNote parseNote(String line) {
        String[] fields = line.split("\\s+");
        if (fields.length == 2) {
           return new BellNote(newNote(fields[0].trim()),newNoteLength(fields[1].trim()));
        }
        return null;
    }
    
    /**
     * 
     * @param potentialNote
     * @return
     */
    private static Note newNote(String potentialNote) {
    	//add validation here
    	Note newNote = Note.valueOf(potentialNote);
    	return newNote;
    }
    
    /**
     * 
     * @param potentialNoteLength
     * @return
     */
    private static NoteLength newNoteLength(String potentialNoteLength) {
    	//add validation
    	
    	int length = Integer.parseInt(potentialNoteLength);
    	NoteLength newNoteLength = NoteLength.NONEXISTANT;
    	//add more validation
    	/*
    	 * could not be parseable int
    	 * could not be 1,2,4,8
    	 * 
    	 */
    	switch(length) {
    	case 1: newNoteLength = NoteLength.WHOLE;
    		break;
    	case 2: newNoteLength = NoteLength.HALF;
    		break;
    	case 4: newNoteLength = NoteLength.QUARTER;
    		break;
    	case 8: newNoteLength = NoteLength.EIGTH;
    		break;
    	default: System.err.println("Invalid note length "+length+".");
    		break;
    	}
    	return newNoteLength;
    }
    
    private final AudioFormat af;

    /**
     * 
     * @param af
     */
    Tone_MK_II(AudioFormat af) {
        this.af = af;
    }

    /**
     * 
     * @param song
     * @throws LineUnavailableException
     */
    void playSong(List<BellNote> song) throws LineUnavailableException {
        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open();
            line.start();
            //this is where we're going to need to sort out our threads
            for (BellNote bn: song) {
                playNote(line, bn);
            }
            line.drain();
        }
    }

    /**
     * 
     * @param line
     * @param bn
     */
    private void playNote(SourceDataLine line, BellNote bn) {
        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        line.write(bn.note.sample(), 0, length);
        line.write(Note.REST.sample(), 0, 50);
    }
}