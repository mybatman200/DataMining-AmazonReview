package edu.gmu.cs475;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class FileManagerServer extends AbstractFileManagerServer {

	ConcurrentHashMap<String,File> fileMap = new ConcurrentHashMap<String,File>(); // cache datastructure
	ConcurrentHashMap<String, IFileReplica> repMap= new ConcurrentHashMap<String,IFileReplica>(); // replica file datastructure
	//ConcurrentHashMap<String, Integer> portMap= new ConcurrentHashMap<String,Integer>();
	ConcurrentHashMap<String, Long> lockMap = new ConcurrentHashMap<String, Long>(); // lock map
	AtomicInteger randomNumb = new AtomicInteger(1); // random number
	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();	 // lock
	Lock r = rwl.readLock();
	Lock w = rwl.writeLock();
	/**
	 * Initializes the server with the state of the files it cares about
	 *
	 * @param files list of the files
	 */
	public void init(List<Path> files){

		for(int i=0; i<files.size(); i++){
			try {
				StringBuilder sb = new StringBuilder();
				FileReader fr=new FileReader(files.get(i).toString()); // get the tostring
				BufferedReader br=new BufferedReader(fr);   
				String line =null;
				while((line = br.readLine()) != null) {
					sb.append(line); // append tostring to sb
				}   
				//System.out.println(sb.toString());
				File tempFile = new File(sb.toString()); // add data to datastructure
				fileMap.put(files.get(i).toString(), tempFile);

			} catch (IOException e) {
				e.printStackTrace();
			}    
		}
	}

	/**
	 * Registers a replica with the server, returning all of the files that currently exist.
	 *
	 * @param hostname   the hostname of the replica talking to you (passed again at disconnect)
	 * @param portNumber the port number of the replica talking to you (passed again at disconnect)
	 * @param replica    The RMI object to use to signal to the replica
	 * @return A HashMap of all of the files that currently exist, mapping from filepath/name to its contents
	 * @throws IOException in case of an underlying IOExceptino when reading the files
	 */
	@Override
	public HashMap<String, String> registerReplica(String hostname, int portNumber, IFileReplica replica) throws IOException {

		String tempCat = hostname + ":" + portNumber;
		rwl.writeLock().lock(); //lock	
		repMap.put(tempCat, replica); // put file to replica in datastructure
		rwl.writeLock().unlock(); //unlock


		HashMap<String, String> temp = new HashMap<String,String>();
		for(String path: fileMap.keySet()){ // put all data to datastructure
			temp.put(path, fileMap.get(path).getContent());
		}
		return temp; 

	}

	/**
	 * Write (or overwrite) a file.
	 * You must not allow a client to register or depart during a write.
	 *
	 *For part 1,  writeFile should: (1) take out a write-lock on the file, 
	 *(2) call   innerWriteFile on each cache client, passing transaction ID 0, 
	 *and (3) then update the file locally. 
	 *
	 * @param file    Path to file to write out
	 * @param content String representing the content desired
	 * @throws IOException if any IOException occurs in the underlying write, OR if the write is not successfully replicated
	 */
	@Override
	public void writeFile(String file, String content) throws RemoteException, IOException {

		Long lock =lockFile(file, true); //lock file
		long randomTime = startNewTransaction(); // start new transaction with randomnumber
		File f = new File(content); // 
		rwl.readLock().lock(); //lock
		try{
			for(IFileReplica path: repMap.values()){ //innerwritefile to all file
				boolean tf =path.innerWriteFile(file, content, randomTime);
				if(tf==false){
					throw new IOException();
				}
				
			}

			for(IFileReplica path: repMap.values()){ // commit transaction to all file
				path.commitTransaction(randomTime);
			}
			unLockFile(file,lock, true); // unlock file
			writeFileLocally(file, content); //write to allfile
			
			//f.setContent(content);
			fileMap.put(file, f);
			//System.out.println(f.getContent());
		}catch(IOException e){
			issueAbortTransaction(randomTime); // abort transaction when fail
			throw new IOException();
		}
		finally{
			rwl.readLock().unlock(); //unlock
		}

	}

	/**
	 * Write (or overwrite) a file. Broadcasts the write to all replicas and locally on the server
	 * You must not allow a client to register or depart during a write.
	 *
	 * @param file    Path to file to write out
	 * @param content String representing the content desired
	 * @param xid     Transaction ID to use for any replications of this write
	 * @return True if all replicas replied "OK" to this write
	 * @throws IOException if any IOException occurs in the underlying write, OR if the write is not succesfully replicated
	 */
	@Override
	public boolean writeFileInTransaction(String file, String content, long xid) throws RemoteException, IOException {
		File fileTemp = new File(content); // set file content
		rwl.readLock().lock(); // lock
		try{
			fileMap.put(file, fileTemp); // put name and content into datastructure
			for(IFileReplica path: repMap.values()){ // innerwrite to all files and write all locally
				if(path.innerWriteFile(file, content, xid)==true){
					writeFileLocally(file, content);
				}
			}

		}catch(IOException e){
			throw new IOException();
		}finally{
			rwl.readLock().unlock(); //unlock
		}
	return false;
}

/**
 * Acquires a read or write lock for a given file.
 *
 * @param name     File to lock
 * @param forWrite True if a write lock is requested, else false
 * @return A stamp representing the lock owner (e.g. from a StampedLock)
 * @throws NoSuchFileException If the file doesn't exist
 */
@Override
public long lockFile(String name, boolean forWrite) throws RemoteException, NoSuchFileException {
	StampedLock sl = new StampedLock();

	if(fileMap.containsKey(name) == false){ //check exception
		throw new NoSuchFileException(name);
	}
	if(forWrite==true){ //lock write from file class
		return fileMap.get(name).getLock().writeLock();

	}else{ // lock read
		return fileMap.get(name).getLock().readLock();
	}

}

/**
 * Releases a read or write lock for a given file.
 *
 * @param name     File to lock
 * @param stamp    the Stamp representing the lock owner (returned from lockFile)
 * @param forWrite True if a write lock is requested, else false
 * @throws NoSuchFileException          If the file doesn't exist
 * @throws IllegalMonitorStateException if the stamp specified is not (or is no longer) valid
 */
@Override
public void unLockFile(String name, long stamp, boolean forWrite) throws RemoteException, NoSuchFileException, IllegalMonitorStateException {
	StampedLock sl = new StampedLock();
	if(fileMap.containsKey(name)==false){ //check file exception
		throw new NoSuchFileException(name);
	}
	if(fileMap.get(name).getLock().equals(stamp)){ // check illegalmonitor exception
		throw new NoSuchFileException(name);
	}
	if(forWrite ==true){ // unlock write file 
		fileMap.get(name).getLock().unlockWrite(stamp);
	}else if(forWrite ==false){ //unlock read
		fileMap.get(name).getLock().unlockRead(stamp);
	}
}

/**
 * Notifies the server that a cache client is shutting down (and hence no longer will be involved in writes)
 *
 * @param hostname   The hostname of the client that is disconnecting (same hostname specified when it registered)
 * @param portNumber The port number of the client that is disconnecting (same port number specified when it registered)
 * @throws RemoteException
 */
@Override
public void cacheDisconnect(String hostname, int portNumber) throws RemoteException {


	String tempCat = hostname+":" +portNumber;
	rwl.writeLock().lock();//lock
	repMap.remove(tempCat);//remove file from datastructure
	//portMap.remove(hostname);
	rwl.writeLock().unlock();//unlock
}

/**
 * Request a new transaction ID to represent a new, client-managed transaction
 *
 * @return Transaction organizer-provided ID that will be used in the future to commit or abort this transaction
 */
@Override
public long startNewTransaction() throws RemoteException {
 return randomNumb.getAndIncrement();
}

/**
 * Broadcast to all replicas (and make updates locally as necessary on the server) that a transaction should be committed
 * You must not allow a client to register or depart during a commit.
 *
 * @param xid transaction ID to be committed (from startNewTransaction)
 * @throws IOException in case of any underlying IOException upon commit
 */
@Override
public void issueCommitTransaction(long xid) throws RemoteException, IOException {
	rwl.writeLock().lock();//lock
	try{
	for(IFileReplica path: repMap.values()){
		path.commitTransaction(xid); //commit all file
	}
	}finally{
		rwl.writeLock().unlock();
	}
}

/**
 * Broadcast to all replicas (and make updates locally as necessary on the server) that a transaction should be aborted
 * You must not allow a client to register or depart during an abort.
 *
 * @param xid transaction ID to be committed (from startNewTransaction)
 */
@Override
public void issueAbortTransaction(long xid) throws RemoteException {
	for(IFileReplica path: repMap.values()){ //abort all file
		path.abortTransaction(xid);
	}
}
}
