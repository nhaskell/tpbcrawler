package tpb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
	BufferedWriter out;
	Boolean terminal;
	Boolean open;
	static int newTorrents = 0;
	
	public void open() {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = format.format(date);
		String filename = "log-"+dateString.replace(':', '.')+".txt";
		try {
			out = new BufferedWriter(new FileWriter("logs/"+filename));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not open log file.");
		}
		open = true;
		log("New log opened at "+dateString+".");
	}
	
	public Boolean isOpen() {
		return open;
	}
	
	private void write(String text, Boolean terminal) {
		if (isOpen()) {
			try {
				out.write(text);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to write to file. Text: "+text);
			}
		}
		if (terminal || this.terminal) {
			System.out.print(text);
		}
	}

	public void log(String text, Boolean terminal) {
		write(text+"\n", terminal);
	}

	public void log(String text) {
		log(text, terminal);
	}

	public void log() {
		log("");
	}
	
	public void close() {
		Date date = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = format.format(date);
		log("Closing log at "+dateString+".");
		try {
			out.close();
		} catch (IOException e) {
			log("Could not close log file.");
			e.printStackTrace();
		}
		open = false;
	}
	
	public void setTerminal(Boolean terminal) {
		this.terminal = terminal;
	}
	
	public Log(Boolean terminal) {
		this.terminal = terminal;
	}
	
	public Log() {
		this(false);
	}

	public synchronized void inc() {
		newTorrents++;
	}
	
	
}
