package tpb;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.*;

public class Cleaner {
	private LinkedList<String> remove;
	private LinkedList<String> space;
	private LinkedList<String> encodings;
	private LinkedList<String> audioBits;
	private LinkedList<String> videoEncodings;
	private LinkedList<String> videoRes;
	private LinkedList<String> language;
	private static final String DATABASE_URL="jdbc:mysql://localhost/test";
	
	public Cleaner()
	{
		remove = new LinkedList<String>();
		space = new LinkedList<String>();
		encodings = new LinkedList<String>();
		audioBits = new LinkedList<String>();
		videoEncodings = new LinkedList<String>();
		videoRes = new LinkedList<String>();
		language = new LinkedList<String>();
		
		remove.add("\\[.*\\]");
		remove.add("\\(.*\\)");
		remove.add("\\{.*\\}");
		remove.add("\\.(m|M)(p|P)(g|G)");
		remove.add("\\.(a|A)(v|V)(i|I)");
		remove.add("(x|X).?(V|v)(i|I)(D|d)");
		remove.add("(d|D)(i|I)(v|V)(x|X)");
		remove.add("(DVD)(R|r)(i|I)(p|P)");
		remove.add("(H|h)(d|D)(T|t)(v|V)");
		remove.add("(m|M)(p|P)3");
		remove.add("(m|M)(o|O)(v|V)");
		remove.add("(B|b)(r|R)(R|r)(i|I)(p|P)");
		remove.add("2(H|h)(D|d)");
		remove.add("MVGroup");
		remove.add("FQM");
		remove.add("aAF");
		remove.add("VTV");
		remove.add("FUtv");
		remove.add("IMMERSE");
		remove.add("720(p|P)");
		remove.add("1080(p|P)");
		
		
		space.add("\\s\\-\\s");
		space.add("\\-");
		space.add("\\_");
		space.add("\\.");
		space.add("\\++");
		
		encodings.add("(m|M)(p|P)3@@mp3");
		encodings.add("(a|A)(a|A)(c|C)@@aac");
		encodings.add("(f|F)(l|L)(a|A)(c|C)@@FLAC");
		
		audioBits.add("128(k|K)@@128kbps");
		audioBits.add("192(k|K)@@192kbps");
		audioBits.add("256(k|K)@@256kbps");
		audioBits.add("320(k|K)@@320kbps");
		audioBits.add("(v|V)(b|B)(r|R)@@VBR");
		audioBits.add("EAC@@EAC");
		
		videoEncodings.add("(m|M)(p|P)(e|E)?(g|G)@@mpg");
		videoEncodings.add("(a|A)(v|V)(i|I)@@avi");
		videoEncodings.add("xvid@@avi-xvid");
		videoEncodings.add("divx@@avi-divx");
		videoEncodings.add("(m|M)(o|O)(v|V)@@mov");
		videoEncodings.add("(m|M)(k|K)(v|V)@@mkv");
		
		videoRes.add("720(p|P)?@@720p");
		videoRes.add("1080(p|P)?@@1080p");
		videoRes.add("HDTV@@720p");
		videoRes.add("SD@@SD");
		
		language.add("spanish");
		language.add("german");
		language.add("italian");
		
	}
	
	
	public String clean(String dirty,BufferedWriter out) throws IOException
	{
		Pattern p;
		Matcher m;
		String search;
		out.write(dirty + "\n");
		
		Iterator<String> i = remove.iterator();
		while(i.hasNext())
		{
			
			search = i.next();
			System.out.println("Searching for string " +search);
			p=Pattern.compile(search);
			m = p.matcher(dirty);
			dirty=m.replaceAll("");
			
			System.out.println("Now string is " + dirty);
		}

		i = space.iterator();
		while(i.hasNext())
		{
			search = i.next();
			System.out.println("Searching for string " +search);
			p=Pattern.compile(search);
			m = p.matcher(dirty);
			dirty=m.replaceAll(" ");
			System.out.println("Now string is " + dirty);
		}

		System.out.println();
		dirty = URLEncoder.encode(dirty, "UTF-8");
		p=Pattern.compile("\\++");
		m = p.matcher(dirty);
		dirty = m.replaceAll("+");
		out.write(dirty+"\n\n");
		return dirty;
	}
	
	public String getParameters(String title, int cat, int id) throws SQLException
	{
		Connection conn = null;
		String parameters = null;
		String type = null;
		
			type=getType(cat);
			parameters="?type=" + type;
			parameters+="&id=" + id;
			if(type=="music")
			{
				String encoding = getData(title,encodings);
				parameters+="&encoding=" + encoding;
				if(encoding!="flac")
				{
					String bitrate = getData(title,audioBits);
					parameters+="&bitrate=" + bitrate;
				}
			}
			if(type=="tvshow"||type=="movie")
			{
				String encoding = getData(title,videoEncodings);
				String resolution = getData(title,videoRes);
				parameters+="&encoding=" + encoding + "&resolution=" + resolution;
			}
			if(type == "tvshow")
			{
				String seasonEp = getSeasonEpisode("title");
				if(seasonEp != null)
				{
					parameters+="&season=" + seasonEp.split("@@")[0] + "&episode=" + seasonEp.split("@@")[1];
				}
				
			}
			return parameters;
		
		
		
	}
	
	private String getSeasonEpisode(String title)
	{
		Pattern p;
		Matcher m;
		String result = null;
		p=Pattern.compile("(s|S)[0-9][0-9](e|E)[0-9][0-9]");
		m = p.matcher(title);
		
		if(m.find())
		{
			return title.substring(m.start()+1,m.start()+3) + "@@" + title.substring(m.start()+4,m.start()+6);
		}
		
		return null;
	}
	
	private String getType(Integer cat)
	{
		String type = null;
		
		if(cat<=199)	{ type="music"; }
		if(cat==201||cat==202||cat==207) { type="movie"; }
		if(cat==205||cat==208) { type="tvshow"; }
		if(cat==203){ type="musicvid"; }
		if(type==null) { type="misc"; }
		
		return type;
	}
	
	private String getData(String title, LinkedList<String> list)
	{
		
		Pattern p;
		Matcher m;
		String search;
		Iterator<String> i = list.iterator();
		while(i.hasNext())
		{
			
			search = i.next();
			p=Pattern.compile(search.split("@@")[0], Pattern.CASE_INSENSITIVE);
			m = p.matcher(title);
			if(m.find()) { return search.split("@@")[1];}
			
			
			
		}
		
		return "unknown";
	}
	
	private Connection connect()
	{
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/test","nick", "what");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	public static void main(String[] args)
	{
		Cleaner c = new Cleaner();
		Connection conn = null;
		FileWriter fstream = null;
		try {
			fstream = new FileWriter("../../out.txt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    BufferedWriter out = new BufferedWriter(fstream);
	    conn = c.connect();

		
		String statement = "SELECT title,cat,id FROM torrentinfo WHERE id>5700000 AND cat=205";
		try{
			
		
			PreparedStatement prep = conn.prepareStatement(statement);
			ResultSet rs = prep.executeQuery();
			while(rs.next())
				{
					System.out.print(rs.getString("title"));
					String parameters = c.getParameters(rs.getString("title"),rs.getInt("cat"),rs.getInt("id"));
					//cleaned=c.clean(rs.getString("title"),out);
					
					//out.write(parameters + "\n" + c.getSeasonEpisode(rs.getString("title")));
					System.out.println(parameters + "  " + c.getSeasonEpisode(rs.getString("title")));
					
				}
			System.out.println("Done!");
			}
		catch(Exception e)
		{ 
			e.printStackTrace();
		}
		
		}
	}


