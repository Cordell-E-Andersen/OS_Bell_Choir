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
 * @author Cordell based off the Tone class, this class reads a song in from a
 *         text file, validates the notes in the file, creates choir members for
 *         each note, gives them a sequence of durations, and plays the song
 *         provided all the imput is valid
 *
 */
public class Controller {

	private static volatile boolean validSong;
	private static List<Choir_Member> choir;
	private static List<Integer> sequenceOfPlay;

	public static void main(String[] args) throws Exception {
		System.out.println("Please Note: any invalid input will disqualify the whole song.\n"
				+ "The only notes recognized by this program are A4-A6; inclusive of sharps, not flats.");
		validSong = true;
		final AudioFormat af = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);
		choir = new ArrayList<Choir_Member>();
		sequenceOfPlay = new ArrayList<Integer>();

		// validate song from the file
		final SourceDataLine mainSourceDataLine = compileSong(af, args[0]);

		mainSourceDataLine.open();
		mainSourceDataLine.start();
		if (validSong) {
			//start playing
			for (int i = 0; i < sequenceOfPlay.size(); i++) {
				choir.get(sequenceOfPlay.get(i)).giveStage();
			}

			//tell players to stop
			for (int i = 0; i < choir.size(); i++) {
				choir.get(i).stopToller();
			}

			//wait for players to stop
			for (int i = 0; i < choir.size(); i++) {
				choir.get(i).waitToStop();
			}

			//data checking
			/*
			for (int i = 0; i < choir.size(); i++) {
				System.out.println("We recruited Choir Member " + choir.get(i).getId());
				System.out.println("Choir Member " + choir.get(i).getId() + " plays "
						+ choir.get(i).getDurationSequence().size() + " times.");
			}
			System.out.println("Sequence of Play:");
			for (int i = 0; i < sequenceOfPlay.size(); i++) {
				System.out.println(choir.get(sequenceOfPlay.get(i)).getId());
			}*/
		}
		mainSourceDataLine.drain();
	}

	/**
	 * takes the audio format and file name to create a series of Choir_Members with
	 * the notes listed in the file. Syntax for file reading taken from the loadMoves method in TicTacToeV2
	 * 
	 * @param AudioFormat, String
	 *            
	 * @return SourceDataLine
	 */
	public static SourceDataLine compileSong(AudioFormat audioFormat, String fileName) {
		File file = new File(fileName);
		try {
			final SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
			// checks to see if the file exists
			if (file.exists()) {
				try (FileReader fileReader = new FileReader(file); BufferedReader br = new BufferedReader(fileReader)) {
					String line = null;
					// while there is more to be read from the document
					while ((line = br.readLine()) != null) {
						BellNote bn = parseNote(line);
						// if a valid BellNote
						if (bn != null && bn.note != null && bn.length != null) {
							// boolean for if the bell is already assigned
							boolean present = false;
							// if for the first choir member
							if (choir.size() == 0) {
								Choir_Member newBell = new Choir_Member(sourceDataLine, bn.note);
								choir.add(newBell);
								choir.get(0).addNoteLength(bn.length);
								sequenceOfPlay.add(0);
							} else {
								// int for if the bell exists
								int position = -1;
								// check to see if bell note is already assigned
								for (int i = 0; i < choir.size(); i++) {
									// if to check to see if the note is already assigned
									if (bn.note.equals(choir.get(i).getChoirBell())) {
										present = true;
										position = i;
									}
								}
								// if for how to proceed after determining presence or not
								if (present) {
									choir.get(position).addNoteLength(bn.length);
									sequenceOfPlay.add(position);
								} else {
									// create new choir member
									Choir_Member newBell = new Choir_Member(sourceDataLine, bn.note);
									choir.add(newBell);
									choir.get(choir.size() - 1).addNoteLength(bn.length);
									sequenceOfPlay.add(choir.size() - 1);
								}
							}
						} else {
							validSong = false;
							System.err.println("Error: Invalid input '" + line + "'");
						}
					}
				} catch (IOException ignored) {
				}
			} else {
				validSong = false;
				System.err.println("File '" + fileName + "' not found");
			}
			return sourceDataLine;
		} catch (LineUnavailableException lue) {
			return null;
		}

	}

	/**
	 * creates a bell note from the given string, provided the string passes
	 * validation
	 * 
	 * @param String
	 *            
	 * @return BellNote
	 */
	private static BellNote parseNote(String line) {
		String[] fields = line.trim().split("\\s+");
		if (fields.length == 2) {
			return new BellNote(newNote(fields[0]), newNoteLength(fields[1]));
		} else if (fields.length < 2) {
			System.err.println("Error: Too few line arguments");
		} else {
			System.err.println("Error: Too many line arguments");
		}
		return null;
	}

	/**
	 * validates potential note for the bell note
	 * 
	 * @param String
	 *            
	 * @return Note
	 */
	private static Note newNote(String potentialNote) {
		Note newNote = null;
		try {
			newNote = Note.valueOf(potentialNote);
		} catch (IllegalArgumentException iae) {
			System.err.println("Error: Invalid Note '" + potentialNote + "'");
		}
		return newNote;
	}

	/**
	 * validates potential note length for the bell note
	 * 
	 * @param String
	 *            
	 * @return NoteLength
	 */
	private static NoteLength newNoteLength(String potentialNoteLength) {
		int length = -1;
		NoteLength newNoteLength = null;
		
		//checks to see if the potential note length is an int
		try {
			length = Integer.parseInt(potentialNoteLength);
		} catch (NumberFormatException nfe) {
			System.err.println("Error: Cannot parse '" + potentialNoteLength + "' to an int");
		}

		//checks for if the potential note length is a valid NoteLength
		switch (length) {
		case 1:
			newNoteLength = NoteLength.WHOLE;
			break;
		case 2:
			newNoteLength = NoteLength.HALF;
			break;
		case 4:
			newNoteLength = NoteLength.QUARTER;
			break;
		case 8:
			newNoteLength = NoteLength.EIGTH;
			break;
		default:
			System.err.println("Error: Invalid note length '" + potentialNoteLength + "'");
			break;
		}
		return newNoteLength;
	}

}