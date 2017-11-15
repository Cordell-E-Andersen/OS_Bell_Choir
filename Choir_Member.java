import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author Cordell based off of the Player class created shown to us class (and
 *         compiled by Lauren, Andrew, and my efforts), the class creates a
 *         choir member who will receive one note and be given a list of note
 *         lengths that they will play during the song
 *
 */
public class Choir_Member implements Runnable {

	private AudioFormat af;
	private Thread choirThread;
	private Note choirBell;
	private List<NoteLength> durationSequence;
	private String id;
	private volatile boolean songContinues;
	private boolean myTurn;
	private int currentDuration;
	private final SourceDataLine sourceDataLine;

	/**
	 * 
	 * creates choir member
	 * 
	 * @param SourceDataLine,
	 *            Note
	 */
	public Choir_Member(SourceDataLine sourceDataLine, Note choirBell) {
		this.sourceDataLine = sourceDataLine;
		this.choirBell = choirBell;
		currentDuration = 0;
		id = choirBell.name() + "_Bell_Toller";
		choirThread = new Thread(this, id);
		durationSequence = new ArrayList<NoteLength>();
		choirThread.start();
	}

	/**
	 * adds a duration to the list of durations the player will play
	 * 
	 * @param tollLength
	 */
	public void addNoteLength(NoteLength tollLength) {
		durationSequence.add(tollLength);
	}

	/**
	 * stops the choir member playing when the song ends
	 * 
	 */
	public void stopToller() {
		songContinues = false;
		synchronized (this) {
			notify();
		}
	}

	/**
	 * Ensures that the choir member threads terminate in an orderly fashion. Waits
	 * for the choir member thread to terminate.
	 */
	public void waitToStop() {
		try {
			choirThread.join();
		} catch (InterruptedException e) {
			System.err.println(choirThread.getName() + " is still trying to play.");
		}
	}

	/**
	 * give the choir member the ability to play their note without overlapping
	 * others
	 * 
	 * taken from player
	 */
	public void giveStage() {
		synchronized (this) {
			if (myTurn) {
				throw new IllegalStateException(
						"Attempt to give stage to a bell toller who hasn't completed the current turn.");
			}
			myTurn = true;
			notify();
			while (myTurn) {
				try {
					wait();
				} catch (InterruptedException ignored) {
				}
			}
		}
	}

	/**
	 * determines the choir member's actions for the duration of the song
	 * 
	 * taken from player
	 */
	public void run() {
		songContinues = true;
		synchronized (this) {
			do {
				while (!myTurn && songContinues) {
					try {
						wait();
					} catch (InterruptedException ignored) {
					}
				}
				if (songContinues) {
					try {
						playNote();
					} catch (LineUnavailableException lue) {
						System.out.println("Next line for " + id + " not found.");
					}
					currentDuration++;

					myTurn = false;
					notify();
				}
			} while (songContinues);
		}
	}

	/**
	 * returns the note the choir member plays
	 * 
	 * @return Note
	 */
	public Note getChoirBell() {
		return choirBell;
	}

	/**
	 * returns the choir member's id
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * gets the sequence of note durations
	 * 
	 * @return List<NoteLength>
	 */
	public List<NoteLength> getDurationSequence() {
		return durationSequence;
	}

	/**
	 * plays the note for the next specified duration
	 * 
	 * @throws LineUnavailableException
	 */
	public void playNote() throws LineUnavailableException {
		// method condensed from two original note playing methods in Tone
		try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
			final int ms = Math.min(durationSequence.get(currentDuration).timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
			final int length = Note.SAMPLE_RATE * ms / 1000;
			sourceDataLine.write(choirBell.sample(), 0, length);
			sourceDataLine.write(Note.REST.sample(), 0, 50);
		}
	}

}
