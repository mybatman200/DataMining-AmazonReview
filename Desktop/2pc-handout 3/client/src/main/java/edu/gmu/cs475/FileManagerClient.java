package edu.gmu.cs475;

import edu.gmu.cs475.struct.NoSuchTagException;

import java.io.IOException;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileManagerClient extends AbstractFileManagerClient {

	ConcurrentHashMap<String,String> fileMap = new ConcurrentHashMap<String,String>(); // data structure to manage cache
	ConcurrentHashMap<Long, ConcurrentHashMap<String,String>> transaction= new ConcurrentHashMap<Long, ConcurrentHashMap<String,String>>(); //data structure to manage transaction
	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();	 //lock
	public FileManagerClient(String host, int port) {
		super(host, port);
		startReplica();
	}

	/**
	 * Used for tests without a real server
	 *
	 * @param server
	 */
	public FileManagerClient(IFileManagerServer server) {
		super(server);
		startReplica();
	}

	/**
	 * Initialzes this read-only replica with the current set of files
	 * @param files A map from filename to file contents
	 */
	@Override
	protected void initReplica(HashMap<String, String> files) {
		fileMap.putAll(files); //copy files to cache
	}

	/**
	 * Lists all of the paths to all of the files that are known to the client
	 *
	 * @return the file paths
	 */
	@Override
	public Iterable<String> listAllFiles() {
		ArrayList<String> list = new ArrayList<String>(fileMap.keySet()); // list all file in cache
		return list;
	}

	/**
	 * Prints out all files. Must internally synchronize
	 * to guarantee that the list of files does not change
	 * during its call, and that each file printed does not change during its
	 * execution (using a read/write lock). You should acquire all of the locks,
	 * then read all of the files and release the locks. Your code should not
	 * deadlock while waiting to acquire locks.
	 *
	 * @return The concatenation of all of the files
	 * @throws NoSuchTagException If no tag exists with the given name
	 * @throws IOException        if any IOException occurs in the underlying read, or if the
	 *                            read was unsuccessful (e.g. if it times out, or gets
	 *                            otherwise disconnected during the execution
	 */
	@Override
	public String catAllFiles() throws NoSuchTagException, IOException {
		StringBuilder sb = new StringBuilder(); 
		rwl.readLock().lock(); // lock read

		for(String key: fileMap.keySet()){ // go through the file, and lock the file and unlock it
			long stamp = lockFile(key, false);	
			unLockFile(key, stamp, false);
		}
		rwl.readLock().unlock();// unlock read

		return sb.toString(); // return the string
	}

	/**
	 * Echos some content into all files. Must internally
	 * synchronize to guarantee that the list of files does
	 * not change during its call, and that each file being printed to does not
	 * change during its execution (using a read/write lock)
	 * <p>
	 * Given two concurrent calls to echoToAllFiles, it will be indeterminate
	 * which call happens first and which happens last. But what you can (and
	 * must) guarantee is that all files will have the *same* value (and not
	 * some the result of the first, qnd some the result of the second). Your
	 * code should not deadlock while waiting to acquire locks.
	 * <p>
	 * Must use a transaction to guarantee that all writes succeed to all replicas (or none).
	 *
	 * @param content The content to write out to each file
	 * @throws NoSuchTagException If no tag exists with the given name
	 * @throws IOException        if any IOException occurs in the underlying write, or if the
	 *                            write was unsuccessful (e.g. if it times out, or gets
	 *                            otherwise disconnected during the execution)
	 */
	@Override
	public void echoToAllFiles(String content) throws IOException {
		ConcurrentHashMap <String, Long> lock = new ConcurrentHashMap <String, Long>(); // keep track of lock

		long id=startNewTransaction();; //start randomnumber 
		rwl.writeLock().lock(); //lock write
		boolean fl =true;

		long stamp =0;
		for(String key :fileMap.keySet()){ // lock all file
			stamp = lockFile(key,true);
			lock.put(key, stamp); //put lock stamp on lock datastrucutre
		}
		for(String key:fileMap.keySet()){ // writefilet to all file
			fl =writeFileInTransaction(key, content, id);
			if(fl==false){
				issueAbortTransaction(id); //abort if fails
			}
		}
		if(fl==true){ // commit if it's true
			issueCommitTransaction(id);
		}

		for(String key:lock.keySet()){ //unlock all file
			unLockFile(key,lock.get(key),true);
		}
		
		rwl.writeLock().unlock();// unlock write




	}

	/**
	 * Return a file as a byte array.
	 *
	 * @param file Path to file requested
	 * @return String representing the file
	 * @throws IOException if any IOException occurs in the underlying read
	 */
	@Override
	public String readFile(String file) throws RemoteException, IOException {
		return fileMap.get(file);
	}

	/**
	 * Write (or overwrite) a file
	 *
	 * @param file    Path to file to write out
	 * @param content String representing the content desired
	 * @param xid     Transaction ID, if this write is associated with any transaction, or 0 if it is not associated with a transaction
	 *                If it is associated with a transaction, then this write must not be visible until the replicant receives a commit message for the associated transaction ID; if it is aborted, then it is discarded.
	 * @return true if the write was successful and we are voting to commit
	 * @throws IOException if any IOException occurs in the underlying write
	 */
	@Override
	public boolean innerWriteFile(String file, String content, long xid) throws RemoteException, IOException {

		if( xid == 0){
			rwl.readLock().lock(); // readlock
			fileMap.put(file, content); // put file and content into datastructure
			rwl.readLock().unlock(); //unlock
			return true;
		}
		else if(xid !=0){
			ConcurrentHashMap<String, String> temp = new ConcurrentHashMap<String, String>();
			temp.put(file, content); // put file and content in transaction datastructure
			transaction.put(xid, temp);
			return true;
		}


		return false;
	}

	/**
	 * Commit a transaction, making any pending writes immediately visible
	 *
	 * @param id transaction id
	 * @throws RemoteException
	 * @throws IOException     if any IOException occurs in the underlying write
	 */
	@Override
	public void commitTransaction(long id) throws RemoteException, IOException {
		rwl.writeLock().lock();// lock
		if(transaction.containsKey(id)){
			ConcurrentHashMap<String, String> temp =transaction.get(id);
			fileMap.putAll(temp);// put transaction into cache
			transaction.remove(id); // remove element from transaction
		}

		rwl.writeLock().unlock();//unlock

	}

	/**
	 * Abort a transaction, discarding any pending writes that are associated with it
	 *
	 * @param id transaction id
	 * @throws RemoteException
	 */
	@Override
	public void abortTransaction(long id) throws RemoteException {
		rwl.writeLock().lock(); //lock
		try{
			transaction.remove(id); //remove
		}
		finally{
			rwl.writeLock().unlock();//unlock
		}
	}
}

