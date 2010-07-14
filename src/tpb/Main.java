package tpb;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * Arguments: [num] [latest] [direction]
 * Create exit.txt to exit.
 */
public class Main {
	
	public static void main(String[] args) throws SQLException, InterruptedException {
		String dbfile = "/Users/nicholashaskell/Public/Drop Box/tpb/tpb.db";
		int numWorkers = 20;
		int rollback = 50; //numWorkers;
		
		//Open log
		Log logger = new Log(true);
		logger.open();
		
		//Open database
		DB db = new DB(logger);
		db.open(dbfile);
		db.init();
	//	db.sync("NORMAL");
		//logger.log(db.info());
		logger.log();
		
		//Open net
		Net net = new Net(logger);
		
		//Parse arguments
		int num = (args.length>=1? Integer.parseInt(args[0]): 10000);
		int start = (args.length>=2? Integer.parseInt(args[1]): -1);
		int step = -1;
		if (args.length >= 3) {
			if (args[2].equals("up")) {
				step = +1;
			}
			else if (args[2].equals("down")) {
				step = -1;
			}
		}
		//hack
		step=+1;
		num=400;
		start=5612011;
		
		
		///</hack>
		//Log arguments
		String arguments = "";
		for (String arg : args) {
			arguments += " "+arg;
		}
		logger.log("Arguments:"+arguments);
		logger.log();
		
		//Resolve broken torrents here...
		//... or not
		
		//Get start
		if (start == 0) {
			start = db.last();
			if (args.length < 3) {
				step = +1;
			}
		}
		else if (start == -1) {
//			start = db.first();
			if (start == 0) {
				throw new RuntimeException("You have to specify the start argument the first time.");
			}
			start--;
		}
		//Rollback
		start = start-step*rollback;
		num = num+rollback;
		//Log
		logger.log("Continuing at id "+start+" (rollback "+rollback+" ids). Stepping: "+step+". Caching "+num+" ids.");
		
		//Create scheduler
		Scheduler scheduler = new Scheduler(start, step, num);
		
		//Start workers
		try {
			//Start workers
			logger.log("Starting "+numWorkers+" worker threads.");
			logger.log();
			if (num > 1000) {
				logger.setTerminal(false);
			}
			ThreadPoolExecutor threads = (ThreadPoolExecutor)Executors.newFixedThreadPool(numWorkers);
			while (!threads.isShutdown()) {
				//Are we done?
				if (scheduler.getLeft() == 0) {
					threads.shutdown();
					scheduler.exit();
					logger.setTerminal(true);
					break;
				}
				
				//There is still work to do!
				if (threads.getActiveCount() < numWorkers) {
					Worker worker = new Worker(scheduler, logger, db, net);
			        threads.execute(worker);
				}
				
				//Check if we should exit
				File exit = new File("exit.txt");
				if (exit.exists()) {
					logger.setTerminal(true);
					logger.log("Detected exit.txt, aborting.");
					threads.shutdown();
					scheduler.exit();
					Thread.sleep(100); //Sleep so explorer doesn't print an error message when deleting too fast
					exit.delete();
				}
				
				Thread.sleep(10);
			}
			
			threads.awaitTermination(30, TimeUnit.SECONDS);
			if (threads.isTerminating()) {
				logger.log("Threads did not terminate within 30 seconds, exiting anyway.");
				threads.shutdownNow();
			}
		}
		finally {
			logger.setTerminal(true);
			logger.log();
			logger.log("Done!");
			logger.log(scheduler.processed+" out of "+num+" ids processed, "+Log.newTorrents+" new torrents.");
			logger.log();

			//Clean up
		//	db.close();
			logger.close();
		}
	}

}
