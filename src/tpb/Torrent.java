package tpb;

import java.io.FileOutputStream;
import java.io.IOException;

public class Torrent {
	protected int id;
	protected byte[] file;
	protected int code = -1;
	
	public void write(String filename) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			if (error()) {
				out.write(("error: "+code).getBytes());
			}
			else {
				out.write(file);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not write "+filename+".");
		}
	}
	
	public Boolean error() {
		return code != 200;
	}
	
	public String toString() {
		return Integer.toString(code);
	}
	
	public Torrent(int id) {
		this.id = id;
	}

}
