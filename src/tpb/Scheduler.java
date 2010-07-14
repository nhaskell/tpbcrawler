package tpb;

public class Scheduler {
	int id;
	int step;
	int num;
	int processed = 0;
	Boolean exit = false;

	public Scheduler(int start, int step, int num) {
		id = start;
		this.step = step;
		this.num = num;
	}

	public synchronized int next() {
		if (exit || processed == num) {
			exit();
			return -1;
		}
		
		//Output update
		if (processed%1000 == 0) {
			System.out.println(getLeft()+" ids remaining, "+Log.newTorrents+" new torrents.");
		}
		
		//Return
		int next = id;
		id = id+step;
		processed++;
		return next;
	}
	
	public int getProcessed() {
		return processed;
	}
	
	public int getLeft() {
		return num-processed;
	}

	public void exit() {
		exit = true;
	}
	
}
