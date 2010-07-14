package tpb;

public class Worker implements Runnable {
	Scheduler scheduler;
	Log logger;
	DB db;
	Net net;

	public Worker(Scheduler scheduler, Log logger, DB db, Net net) {
		this.scheduler = scheduler;
		this.logger = logger;
		this.db = db;
		this.net = net;
	}
	
	public void run() {
		int id = -1;
		try {
			logger.log("Starting worker thread.",true);
			
			while (!scheduler.exit && (id=scheduler.next()) != -1) {
				//Log
				String log = "Id "+id+": ";
				
				//Do not fetch if it already exists in database
				if (db.exists(id)) {
					if (db.broken(id)) {
						logger.log(id+" is in a broken state, retrying.",true);
					}
					else {
						log += "Already exists, skipping.";
						logger.log(log);
						continue;
					}
				}
				
				//Fetch torrent
				Torrent torrent = net.fetchTorrent(id);
				if (torrent.error()) {
					log += Integer.toString(torrent.code);
					logger.log(log);
					continue;
				}
				
				//Fetch info
				TorrentInfo info = net.fetchInfo(id);
				log += info.toString();
				if (info.error()) {
					logger.log(log);
					continue;
				}
				
				//Write to database
				synchronized(db) {
				//	db.begin();
					db.insertInfo(info);
					db.insertTorrent(torrent);
				//	db.commit();
				}
				logger.log(log);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Id "+id+" failed.",true);
		}
		finally {
			logger.log("Worker exiting."+(id==-1?" No more work.":""),true);
		}
	}
}
