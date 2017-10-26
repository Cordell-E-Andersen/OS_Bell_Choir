/**
 * 
 * @author Cordell
 *
 */
public class Stage {
	private boolean available = true;

	/**
	 * allows the thread to acquire the mutex and play its note
	 */
	public synchronized void acquireStage() {
		while (!available) {
			try {
				wait();
			} catch (Exception e) {

			}
		}
		available = false;

	}

	/**
	 * releases the threads hold on the mutex and allows the next thread to play its note
	 */
	public synchronized void releaseStage() {
		try {
			available = true;
			notify();
		} catch (Exception e) {

		}

	}

}
