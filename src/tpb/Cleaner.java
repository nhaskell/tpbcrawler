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
		
		space.add("\\s\\-\\s");
		space.add("\\-");
		space.add("\\_");
		space.add("[a-z]\\.[a-zA-z]");
		
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


