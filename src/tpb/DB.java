package tpb;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
import java.net.*;

//torrentinfo: id, date, title, size, cat, rating, desc
//torrents:    id, file

public class DB {
	Connection conn = null;
	Log logger = null;
	
	public DB(Log logger) {
		this.logger = logger;
	}
	public DB() { }

	public void open(String filename) {
		log("Opening database: "+filename);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/test","nick", "what");
			
		} catch (Exception e) {
			e.printStackTrace();
			log("Failed to open database.");
		}
		
	}
	
	public void init() throws SQLException {
		Statement stat = conn.createStatement();
		//Create tables
	//	stat.execute("CREATE TABLE IF NOT EXISTS torrentinfo(id INTEGER PRIMARY KEY, date INTEGER, title TEXT, size INTEGER, cat INTEGER, rating INTEGER, desc TEXT)");
	//	stat.execute("CREATE TABLE IF NOT EXISTS torrents(id INTEGER PRIMARY KEY, file BLOB)");
	//	stat.execute("CREATE TABLE IF NOT EXISTS cat(id INTEGER PRIMARY KEY, title TEXT)");
		//Populate categories
		try {
			FileInputStream fstream = new FileInputStream("cat.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line=br.readLine()) != null) {
				int id = Integer.parseInt(line.substring(0, line.indexOf(" ")));
				String title = line.substring(line.indexOf(" ")+1);
				insertCat(id, title);
			}
			in.close();
		} catch (IOException e) {
			log("Could not init database, could not read cat.txt");
		}
	}

	public int getStart() throws SQLException
	{
		PreparedStatement prep = conn.prepareStatement("SELECT Max(id) from torrentinfo");
		ResultSet rs = prep.executeQuery();
		rs.next();
		return(rs.getInt(1));
	}
	public void insertCat(int id, String title) throws SQLException {
		String statement = "REPLACE INTO cat VALUES (?,?)";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, id);
		prep.setString(2, title);
		prep.executeUpdate();
	}

	public void insertInfo(TorrentInfo info) throws SQLException {
		String statement = "REPLACE INTO torrentinfo VALUES (?,?,?,?,?,?,?,?)";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, info.id);
		prep.setInt(2, info.date);
		prep.setString(3, info.title);
		prep.setLong(4, info.size);
		prep.setInt(5, info.cat);
		prep.setInt(6, info.rating);
		prep.setInt(7, info.seeders);
		prep.setInt(8, info.leechers);
		prep.executeUpdate();
	}

	public void insertTorrent(Torrent torrent) throws SQLException {
		String statement = "REPLACE INTO torrents VALUES (?,?)";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, torrent.id);
		prep.setBytes(2, torrent.file);
		prep.executeUpdate();
		if (logger != null) {
			logger.inc();
		}
	}
	
	private Boolean exists(int id, String table) throws SQLException {
		String statement = "SELECT COUNT(*) FROM "+ table + " WHERE id=?";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();
		rs.next();
		int num = rs.getInt("COUNT(*)");
		return num==1;
	}

	public Boolean exists(int id) throws SQLException {
		return (exists(id,"torrentinfo") || exists(id,"torrents"));
	}

	public Boolean broken(int id) throws SQLException {
		return (exists(id,"torrentinfo") ^ exists(id,"torrents"));
	}

	public void dump() throws SQLException {
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT id, date, title, size, cat, rating, desc FROM torrentinfo");
		while (rs.next()) {
			System.out.println("------------------");
			System.out.println("Id: "+rs.getInt("id"));
			System.out.println("Date: "+rs.getInt("date"));
			System.out.println("Title: "+rs.getString("title"));
			System.out.println("Size: "+rs.getLong("size"));
			System.out.println("Cat: "+rs.getInt("cat"));
			System.out.println("Rating: "+rs.getInt("rating"));
			System.out.println("Desc: "+rs.getInt("desc"));
		}
		rs.close();
	}
	
	public TorrentInfo getInfo(int id) throws SQLException {
		String statement = "SELECT date, title, size, cat, rating, desc FROM torrentinfo WHERE id=?";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();
		
		TorrentInfo info = new TorrentInfo(id);
		info.date   = rs.getInt("date");
		info.title  = rs.getString("title");
		info.size   = rs.getLong("size");
		info.cat    = rs.getInt("cat");
		info.rating = rs.getInt("rating");
		info.desc   = rs.getString("desc");
		
		rs.close();
		
		return info;
	}

	public Torrent getTorrent(int id) throws SQLException {
		String statement = "SELECT file FROM torrents WHERE id=?";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setInt(1, id);
		ResultSet rs = prep.executeQuery();
		
		Torrent torrent = new Torrent(id);
		torrent.code = 200;
		torrent.file = rs.getBytes("file");
		
		rs.close();
		
		return torrent;
	}
	
	public int first() throws SQLException {
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT MIN(id) FROM torrentinfo");
		rs.next();
		return rs.getInt("MIN(id)");
	}
	
	public int last() throws SQLException {
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT MAX(id) FROM torrentinfo");
		rs.next();
		return rs.getInt("MAX(id)");
	}
	
	public String info() throws SQLException {
		String info = "Database info: ";
		Statement stat = conn.createStatement();
		
		//torrentinfo
		ResultSet rs = stat.executeQuery("SELECT COUNT(*) FROM torrentinfo");
		rs.next();
		info += rs.getInt("COUNT(*)")+" entries in torrentinfo, ";
		
		//torrents
		rs = stat.executeQuery("SELECT COUNT(*) FROM torrents");
		rs.next();
		info += rs.getInt("COUNT(*)")+" entries in torrents.";
		
		return info;
	}
	
	/**
	 * http://www.sqlite.org/pragma.html#pragma_synchronous
	 */
	public void sync(String arg) throws SQLException {
		String statement = "PRAGMA synchronous = ?";
		PreparedStatement prep = conn.prepareStatement(statement);
		prep.setString(1, arg);
		prep.execute();
		log("Setting database synchronous mode to '"+arg+"'.");
	}
	
	public void close() throws SQLException {
		conn.close();
	}

	public void begin() throws SQLException {
		Statement stat = conn.createStatement();
		stat.execute("BEGIN TRANSACTION");
	}
	public void commit() throws SQLException {
		Statement stat = conn.createStatement();
		stat.execute("COMMIT");
	}
	
	public void log(String text) {
		if (logger != null) {
			logger.log(text);
		}
		else {
			System.out.println(text);
		}
	}

	
	public static void main(String[] args) throws SQLException {
		DB db=new DB(null);
		db.open("../tpb.db");
		db.init();
		System.out.println("first(): "+db.first());
		System.out.println("last(): "+db.last());
		//db.dump();
		db.close();
		System.out.println("Done!");
	}

}
