package edu.gmu.cs475;

import edu.gmu.cs475.struct.ITaggedFile;
import edu.gmu.cs475.struct.NoSuchTagException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileTagManagerClient extends AbstractFileTagManagerClient {

	ConcurrentHashMap<Long, String> writeStamp = new ConcurrentHashMap<Long, String>(); // datastructure to keep hold of tags
	ConcurrentHashMap<Long, String> readStamp = new ConcurrentHashMap<Long, String>(); // datastructure to keep hold of tags


	public FileTagManagerClient(String host, int port) {
		super(host, port);

	}

	/**
	 * Used for tests without a real server
	 * 
	 * @param server
	 */
	public FileTagManagerClient(IFileTagManager server) {
		super(server);
		runningHeartBeat();
	}

	public void runningHeartBeat(){
		Runnable r=() ->{// send heart beat for 2 second to the server
			synchronized(this){
				for(long key: readStamp.keySet()){	// go through the set of stamps
					try{
						super.heartbeat(readStamp.get(key), key, false); //send heartbeat
					}catch(Exception e){ //unlock stamp if exception raised
						String file = readStamp.get(key);			
						unLockFileCalled(file, false, key); // unlock stamp
					}
				}
				for(long key: writeStamp.keySet()){	// go through the set of stamps
					try{
						super.heartbeat(writeStamp.get(key), key, true);//send heartbeat
					}catch(Exception e){//unlock stamp if exception raised
						
						String file = writeStamp.get(key);			
						unLockFileCalled(file, true, key);// unlock stamp
					}
				}
			}
		};timerExecutorService.scheduleAtFixedRate(r, 0, 2, TimeUnit.SECONDS);
	}

	/* It is strongly suggested that you use the timerExecutorService to manage your timers, but not required*/
	private final ScheduledThreadPoolExecutor timerExecutorService = new ScheduledThreadPoolExecutor(2);

	//TODO - implement the following methods; the rest are automatically stubbed out to the server

	/**
	 * Prints out all files that have a given tqg. Must internally synchronize
	 * to guarantee that the list of files with the given tag does not change
	 * during its call, and that each file printed does not change during its
	 * execution (using a read/write lock). You should acquire all of the locks,
	 * then read all of the files and release the locks. Your could should not
	 * deadlock while waiting to acquire locks.
	 *
	 * @param tag Tag to query for
	 * @return The concatenation of all of the files
	 * @throws NoSuchTagException If no tag exists with the given name
	 * @throws IOException        if any IOException occurs in the underlying read, or if the
	 *                            read was unsuccessful (e.g. if it times out, or gets
	 *                            otherwise disconnected during the execution
	 */
	@Override
	public String catAllFiles(String tag) throws NoSuchTagException, IOException {


		StringBuilder sb = new StringBuilder();

		Iterable<String> tempString = listFilesByTag(tag); //list all files by tag and add them to a list 

		//synchronized(this){
		for(String t : tempString){ // lock all file with associate tags
			long lock = lockFile(t, false);
			lockFileSuccess(t, false, lock);
		}
		//}
		try{
			for(String t : tempString){
				synchronized(this){
					sb.append(readFile(t)); //add file to stringbuilder
				}
			}

		}catch(IOException e){ // if exception raised, unlock all files
			Iterable<Long> st = readStamp.keySet();
			for(Long t : st){ 
				String file = readStamp.get(t);
				unLockFile(file, t, false);
				unLockFileCalled(file, false, t);
			}
		}
		Iterable<Long> st = readStamp.keySet();
		for(Long t : st){  //unlock all files
			String file = readStamp.get(t);
			unLockFile(file, t, false);
			unLockFileCalled(file, false, t);
		}
		//System.out.println(sb.toString());
		return sb.toString(); // return the string from stringbuilder

	}

	/**
	 * Echos some content into all files that have a given tag. Must internally
	 * synchronize to guarantee that the list of files with the given tag does
	 * not change during its call, and that each file being printed to does not
	 * change during its execution (using a read/write lock)
	 * <p>
	 * Given two concurrent calls to echoToAllFiles, it will be indeterminate
	 * which call happens first and which happens last. But what you can (and
	 * must) guarantee is that all files will have the *same* value (and not
	 * some the result of the first, qnd some the result of the second). Your
	 * could should not deadlock while waiting to acquire locks.
	 *
	 * @param tag     Tag to query for
	 * @param content The content to write out to each file
	 * @throws NoSuchTagException If no tag exists with the given name
	 * @throws IOException        if any IOException occurs in the underlying write, or if the
	 *                            write was unsuccessful (e.g. if it times out, or gets
	 *                            otherwise disconnected during the execution)
	 */
	@Override
	public void echoToAllFiles(String tag, String content) throws NoSuchTagException, IOException {

		try{
			Iterable<String> tempString = listFilesByTag(tag); //list all files by tag and add them to a list 

			for(String t : tempString){ //lock all files
				long lock = lockFile(t, true);
				lockFileSuccess(t, true, lock);
			}
			try{
				for(String t : tempString){
					synchronized(this){
						writeFile(t, content); //add files to stringbuilder
					}
				}
			}catch(IOException e){ //if exception thrown, then unlock all files
				Iterable<Long> st = writeStamp.keySet();
				for(Long t : st){ 
					String file = writeStamp.get(t);
					unLockFile(file, t, true);
					unLockFileCalled(file, true, t);
				}
			}
			Iterable<Long> st = writeStamp.keySet();
			for(Long t : st){  // unlock all file after finished writing
				String file = writeStamp.get(t);
				unLockFile(file, t, true);
				unLockFileCalled(file, true, t);
			}
		}catch(NoSuchTagException e){
			e.printStackTrace();
		}
	}


	/**
	 * A callback for you to implement to let you know that a lock was
	 * successfully acquired, and that you should start sending heartbeats for
	 * it
	 *
	 * @param name
	 * @param forWrite
	 * @param stamp
	 */
	@Override
	public synchronized void lockFileSuccess(String name, boolean forWrite, long stamp) {
		if(forWrite== true){
			writeStamp.put(stamp, name); // store stamp on the correct data structure with it file name
		}
		else if(forWrite== false ){
			readStamp.put(stamp, name); // store stamp on the correct data structure with it file name
		}
	}	

	/**
	 * A callback for you to implement to let you know a lock was relinquished,
	 * and that you should stop sending heartbeats for it.
	 *
	 * @param name
	 * @param forWrite
	 * @param stamp
	 */
	@Override
	public synchronized void unLockFileCalled(String name, boolean forWrite, long stamp) {
		if(forWrite== true){
			writeStamp.remove(stamp);  //// remove stamp on the correct data structure with it file name
		}
		else if(forWrite== false){
			readStamp.remove(stamp); // remove stamp on the correct data structure with it file name
		}
	}


}
