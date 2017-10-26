import javax.sound.sampled.SourceDataLine;

/**
 * 
 * @author Cordell
 *
 */
public class Choir_Member implements Runnable{

	public Stage stage;
	private Thread choirThread;
	private volatile boolean songContinues;
	
	/**
	 * 
	 * @param stage
	 */
	public Choir_Member(Stage stage){
		this.stage = stage;
		
	}
	
	/**
	 * 
	 */
	public void run() {
		
		
		
	}
	
//    /**
//     * 
//     * @param line
//     * @param bn
//     */
//    private void playNote(SourceDataLine line, BellNote bn) {
//        final int ms = Math.min(bn.length.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
//        final int length = Note.SAMPLE_RATE * ms / 1000;
//        line.write(bn.note.sample(), 0, length);
//        line.write(Note.REST.sample(), 0, 50);
//    }
	
}
