package tpb;
import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.*;

public class Cleaner {
	private LinkedList<String> remove;
	private LinkedList<String> space;
	public Cleaner()
	{
		remove = new LinkedList<String>();
		space = new LinkedList<String>();
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
		
	}
	
	
	public String clean(String dirty)
	{
		Pattern p;
		Matcher m;
		String search;
		
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
			dirty=m.replaceAll("+");
			System.out.println("Now string is " + dirty);
		}

		dirty.replace(" ", "+");
		System.out.println();
		return dirty;
	}
	
	public static void main(String[] args)
	{
		Cleaner c = new Cleaner();
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost/test","nick", "what");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String cleaned;
		String statement = "SELECT * FROM torrentinfo";
		try{
			
		
			PreparedStatement prep = conn.prepareStatement(statement);
			ResultSet rs = prep.executeQuery();
			while(rs.next())
				{
					cleaned=c.clean(rs.getString("title"));
					//put data in xml file
				}
			}
		catch(Exception e)
		{ 
			e.printStackTrace();
		}
		
		}
	}


