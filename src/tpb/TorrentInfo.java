package tpb;

import java.io.*;

public class TorrentInfo {
	protected String error = null;
	
	protected Integer id;
	protected Integer date;
	protected String title;
	protected Long size;
	protected Integer cat;
	protected Integer rating;
	protected String desc;
	
	public void write(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(info());
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not write "+filename+".");
		}
	}
	
	public String info() {
		return "Id: "+id+"\n"+
	           "Error: "+error+"\n"+
	           "Date: "+date+"\n"+
	           "Title: "+title+"\n"+
	           "Size: "+size+"\n"+
	           "Cat: "+cat+"\n"+
	           "Rating: "+rating+"\n"+
	           "Desc: "+desc;
	}
	
	public Boolean error() {
		return (error != null);
	}
	
	public String toString() {
		return (error()? "Error ("+error+")": title);
	}
	
	public TorrentInfo(int id) {
		this.id = id;
	}
}
