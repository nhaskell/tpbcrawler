package tpb;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.text.*;
import java.util.*;

import net.htmlparser.jericho.*;

/**
 * The most difficult part is to parse the torrentinfo. Some torrents have 1
 * column and some have 2. This code should be checked if it can run faster.
 */
public class Net {
	Log logger = null;
	Cleaner clean = null;

	public Net(Log logger) {
		this.logger = logger;
	}

	public Net() {
	}

	public TorrentInfo fetchInfo(int id) throws InterruptedException {
		// Init
		TorrentInfo info = new TorrentInfo(id);
		String url = "http://thepiratebay.org/torrent/" + id;
		Integer timeout = 0;
		Boolean notlogged = false;
		// Read url
		for (;;) {
			Source source = null;
			try {
				source = new Source(new URL(url));
			} catch (FileNotFoundException e) {

					info.error = "404";
					notlogged=true;
					break;

			}
				catch(Exception e)
				{
				e.printStackTrace();
				log("Problem fetching info for id " + id + ", retrying.", true);
				Thread.sleep(2000);
				continue;
			}
			// Check for error
			Element e = source.getElementById("err");
			if (e != null) {
				info.error = e.getFirstElement("h2").getTextExtractor()
						.toString();
				return info;
			}

			// Set variables
			Element details = source.getElementById("details");
			int start;

			// Get title
			e = source.getElementById("title");
			if (e == null) {
				log("Could not get title, retrying.", true);
				Thread.sleep(2000);
				info.error="invalid title";
				continue;
			}
			info.title = e.getTextExtractor().toString();
			// System.out.println(info.title);

			Iterator<Element> cols = details.getAllElements("dl").iterator();
			while (cols.hasNext()) {
				Element col = cols.next();
				List<Element> dts = col.getContent().getAllElements("dt");
				List<Element> dds = col.getContent().getAllElements("dd");

				for (int i = 0; i < dts.size(); i++) {
					String dt = dts.get(i).getTextExtractor().toString();
					if (dt.equals("Type:")) {
						// Get cat
						String cat = dds.get(i).getFirstElement("a")
								.getAttributeValue("href");
						cat = cat.substring(cat.lastIndexOf("/") + 1);
						info.cat = Integer.parseInt(cat);
						
						////////////////////////////////////////////////////////////////
						/////    THIS IS WHERE WE FILTER OUT TORRENTS THAT AREN'T //////
						/////	 		EITHER MUSIC OR MOVIES. (CAT >300)		  //////
						////////////////////////////////////////////////////////////////
						if(info.cat>300)
						{
							log("Torrent " + id + " is not either video or sound.");
							info.error="invalid torrent type";
							notlogged=true;
							break;
							
						}
						// System.out.println("Category: "+info.cat);
					} else if (dt.equals("Size:")) {
						// Get size
						String size = dds.get(i).getTextExtractor().toString();
						size = size.substring(size.indexOf("(") + 1);
						size = size.substring(0, size.indexOf(" "));
						info.size = Long.parseLong(size);
						// System.out.println("Size: "+info.size+" bytes");
					} else if (dt.equals("Quality:")) {
						// Get rating
						String rating = dds.get(i).getTextExtractor()
								.toString();
						rating = rating.substring(rating.indexOf("(") + 1);
						rating = rating.substring(0, rating.indexOf(")"));
						if (rating.charAt(0) == '+') {
							rating = rating.substring(1);
						}
						info.rating = Integer.parseInt(rating);
						// System.out.println("Rating: "+info.rating);
					} else if (dt.equals("Seeders:")) {
						String seeders = dds.get(i).getTextExtractor().toString();
						info.seeders=Integer.parseInt(seeders);
						seeders=null;
					} else if (dt.equals("Leechers:")) {
						String leechers = dds.get(i).getTextExtractor().toString();
						info.leechers=Integer.parseInt(leechers);
						leechers=null;
					} else if (dt.equals("Uploaded:")) {
						// Get date
						String dateString = dds.get(i).getTextExtractor()
								.toString();
						DateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd hh:mm:ss z");
						try {
							Date date = format.parse(dateString);
							info.date = (int) (date.getTime() / 1000);
						} catch (ParseException E) {
							E.printStackTrace();
							log("Could not parse date for id " + id
									+ ". Date string is \"" + dateString
									+ "\".", true);
						}
						// System.out.println("Date: "+info.date);
						
					}
				}
			}

			// Get desc
			String desc = details.getFirstElementByClass("nfo")
					.getFirstElement("pre").getContent().toString();
			while ((start = desc.indexOf("<")) != -1) {
				int stop = desc.indexOf(">", start);
				desc = desc.substring(0, start) + desc.substring(stop + 1);
			}
			desc = desc.substring(0, desc.length() - 2);
			desc = CharacterReference.decode(desc);
			info.desc = desc;

			/*
			 * catch (Exception E) { BufferedWriter out = new BufferedWriter(new
			 * FileWriter("source-"+id+".txt")); out.write(source.toString());
			 * out.close(); }
			 */
			// System.out.println("Desc: "+desc);
			/*
			 * BufferedWriter out = new BufferedWriter(new
			 * FileWriter("desc-"+id+".txt")); out.write(desc); out.close();
			 */

			break;
		}
		if(!notlogged)
		{
		// Make sure we got everything
		if (info.title == null) {
			log("Title is null!", true);
			throw new RuntimeException();
		}
		if (info.cat == null) {
			log("Cat is null!", true);
			throw new RuntimeException();
		}
		if (info.date == null) {
			log("Date is null!", true);
			throw new RuntimeException();
		}
		if (info.rating == null) {
			log("Rating is null!", true);
			throw new RuntimeException();
		}
		if (info.size == null) {
			log("Size is null!", true);
			throw new RuntimeException();
		}
		if (info.desc == null) {
			log("Desc is null!", true);
			throw new RuntimeException();
		}
		}
		// Done
		return info;
	}

	public Torrent fetchTorrent(int id) throws InterruptedException {
		// Init
		Torrent torrent = new Torrent(id);
		String url = "http://torrents.thepiratebay.org/" + id + "/";

		// Read url
		for (;;) {
			try {
				// Open connection
				HttpURLConnection conn = (HttpURLConnection) new URL(url)
						.openConnection();
				torrent.code = conn.getResponseCode();
				if (torrent.code == 404) {
					break;
				}
				if (torrent.code != 200) {
					log("Id " + id + ": " + torrent.code + ", retrying.", true);
					Thread.sleep(2000);
					continue;
				}
				// Read file
				int size = conn.getContentLength();
				InputStream in = conn.getInputStream();
				torrent.file = new byte[size];
				int read;
				int pos = 0;
				while ((read = in.read(torrent.file, pos, size - pos)) != -1) {
					pos += read;
				}
				in.close();

				// Make sure we got everything
				if (pos != size) {
					log("Id " + id + ": Did not read whole torrent. Read "
							+ pos + " out of " + size + " bytes!", true);
					throw new RuntimeException();
				}
			} catch (IOException e) {
				e.printStackTrace();
				log("IOException when fetching torrent for id " + id
						+ ", retrying.", true);
				Thread.sleep(2000);
				continue;
			}
			break;
		}

		// Done
		return torrent;
	}

	public void log(String text, Boolean terminal) {
		if (logger != null) {
			logger.log(text, terminal);
		} else {
			System.out.println(text);
		}
	}

	public void log(String text) {
		log(text, false);
	}

	public static void main(String[] args) throws SQLException,
			InterruptedException {
		// Init
		int id = 5500031;
		Net net = new Net();
		/*
		 * DB db = new DB(); db.open("~/Public/Drop Box/tpb/tpb.db"); db.init();
		 */
		// Fetch
		TorrentInfo info = net.fetchInfo(id);
		System.out.println("Id " + id + ": " + info);
		Torrent torrent = net.fetchTorrent(id);
		System.out.println("Torrent: " + torrent);
		torrent.write(id + ".torrent");

		/*
		 * //Put in db ... System.out.println("Putting stuff in db.");
		 * db.begin(); db.insertInfo(info); db.insertTorrent(torrent);
		 * db.commit();
		 */
		// ... and get it back again (doesn't work with this driver - too many
		// rows I guess)
		/*
		 * System.out.println(); info = null; torrent = null;
		 * System.out.println("Getting stuff from db."); info = db.getInfo(id);
		 * torrent = db.getTorrent(id); System.out.println("Info: "+info);
		 * System.out.println("Torrent: "+torrent);
		 * torrent.write(id+"-db.torrent");
		 */

		// Clean up
		// db.close();

		System.out.println();
		System.out.println("Done!");
	}

}
