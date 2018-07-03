package edu.gmu.cs475;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import edu.gmu.cs475.internal.ServerMain;
import edu.gmu.cs475.struct.NoSuchTagException;
import edu.gmu.cs475.struct.TagExistsException;

public class FileTagManagerServer implements IFileTagManager {
	ConcurrentHashMap<String, Tag> tag1= new ConcurrentHashMap<String, Tag>(); // datastructure to keep hold of tags
	ConcurrentHashMap<String, TaggedFile> tagFile= new ConcurrentHashMap<String, TaggedFile>(); // datastructure for file

	ConcurrentHashMap<Long, String> writeStamp = new ConcurrentHashMap<Long, String>(); // datastructure to keep hold of tags
	ConcurrentHashMap<Long, String> readStamp = new ConcurrentHashMap<Long, String>(); // datastructure to keep hold of tags
	ConcurrentHashMap<Long, Integer> counterStamp = new ConcurrentHashMap<Long, Integer>(); // datastructure to keep hold of tags


	private final ScheduledThreadPoolExecutor timerExecutorService = new ScheduledThreadPoolExecutor(2);

	@Override
	public String readFile(String file) throws RemoteException, IOException {
		return new String(Files.readAllBytes(Paths.get(file)));
	}


	//TODO - implement all of the following methods:

	/**
	 * Initialize your FileTagManagerServer with files
	 * Each file should start off with the special "untagged" tag
	 * @param files
	 */
	public void init(List<Path> files) {
		Tag ntag= new Tag("untagged"); //create new tag
		//synchronized(tag1){
		for(int i=0; i<files.size();i++){ // go through file path
			tag1.put("untagged", ntag); // add untagged to tag hashmap
			TaggedFile nfiles = new TaggedFile(files.get(i)); // create new file
			nfiles.tags.put("untagged",ntag); // put untagged onto tag that belongs to TaggedFile
			tagFile.put(files.get(i).toString(), nfiles); // add new file to TaggedFile
		
		}
	}

	/**
	 * List all currently known tags.
	 *
	 * @return List of tags (in any order)
	 */
	@Override
	public Iterable<String> listTags() throws RemoteException {
		return new ArrayList<String>(tag1.keySet());
	}

	/**
	 * Add a new tag to the list of known tags
	 *
	 * @param name Name of tag
	 * @return The newly created Tag name
	 * @throws TagExistsException If a tag already exists with this name
	 */
	@Override
	public String addTag(String name) throws RemoteException, TagExistsException {
		Tag ntag = new Tag(name); // created new tag
		ntag.getLock(); // call lock
		synchronized(tag1){ // synchronized operation because of adding
			if(tag1.containsKey(name)){  // check if tag is already available, if so then raise exception
				throw new TagExistsException();
			}
			if(tag1.containsKey("untagged")){ // check if tag is "untagged", if so remove and replace with tag
				tag1.put(name, ntag);
			}
			else{
				tag1.put(name, ntag); // add tag to the hashmap
			}
		}
		return ntag.toString();

	}

	/**
	 * Update the name of a tag, also updating any references to that tag to
	 * point to the new one
	 *
	 * @param oldTagName Old name of tag
	 * @param newTagName New name of tag
	 * @return The newly updated Tag name
	 * @throws TagExistsException If a tag already exists with the newly requested name
	 * @throws NoSuchTagException If no tag exists with the old name
	 */
	@Override
	public String editTag(String oldTagName, String newTagName) throws RemoteException, TagExistsException, NoSuchTagException {
		Tag ntag = new Tag(newTagName); // create new tag
		ntag.getLock();
		synchronized(tag1){ // synchronized tag
			if(tag1.containsKey(newTagName)){ // check if the tagexist
				throw new TagExistsException();
			}
			if(!tag1.containsKey(oldTagName)){ //check if tag is in the hashmap
				throw new NoSuchTagException();
			}
			if(tag1.containsKey(oldTagName)){ //  else remove oldkey and add new key to hashmap
				tag1.remove(oldTagName);
				tag1.put(newTagName, ntag);
			}
		}

		return ntag.toString();
	}

	/**
	 * Delete a tag by name
	 *
	 * @param tagName Name of tag to delete
	 * @return The tag name that was deleted
	 * @throws NoSuchTagException         If no tag exists with that name
	 * @throws DirectoryNotEmptyException If tag currently has files still associated with it
	 */
	@Override
	public String deleteTag(String tagName) throws RemoteException, NoSuchTagException, DirectoryNotEmptyException {

		Tag tag = new Tag(tagName);
		// if tag does not exist, throw exception
		if( !(tag1.containsKey(tagName)) ) {
			throw new NoSuchTagException();
		}
		// if files has tag associated with it, throw exception
		else if(!tag1.get(tagName).files.isEmpty()) {
			throw new DirectoryNotEmptyException(tagName);
		}
		// if contains tag, remove
		else if(tag1.containsKey(tagName)) {
			synchronized(tag1){
				tag1.remove(tagName);
				Tag ntag1= new Tag("untagged"); //create new tag				
				tag1.put("untagged", ntag1);
			}
		}
		return tag.getName();
	}

	/**
	 * List all files, regardless of their tag
	 *
	 * @return A list of all files. Each file must appear exactly once in this
	 * list.
	 */
	@Override
	public Iterable<String> listAllFiles() throws RemoteException {
		return  new ArrayList<String>(tagFile.keySet()); // return a list of files stored in hashmap above
	}

	/**
	 * List all files that have a given tag
	 *
	 * @param tag Tag to look for
	 * @return A list of all files that have been labeled with the specified tag
	 * @throws NoSuchTagException If no tag exists with that name
	 */
	@Override
	public Iterable<String> listFilesByTag(String tag) throws RemoteException, NoSuchTagException {
		ArrayList<String> fileByTag = new ArrayList<String>(); // arraylist to temporary store list of file 
		if(!tag1.containsKey(tag)){ // raise exception if the tag doesn't belong to tag hashmap
			throw new NoSuchTagException();
		}
		synchronized(this){
			for(TaggedFile tagFile : tagFile.values()) { //go through all files
				if(tagFile.tags.containsKey(tag)) { // if tag are within files then add it to the arraylist
					fileByTag.add(tagFile.getName());
				}
			}
		}
		return fileByTag;//return the arraylist
	} 

	/**
	 * Label a file with a tag
	 * <p>
	 * Files can have any number of tags - this tag will simply be appended to
	 * the collection of tags that the file has. However, files can be tagged
	 * with a given tag exactly once: repeatedly tagging a file with the same
	 * tag should return "false" on subsequent calls.
	 * <p>
	 * If the file currently has the special tag "untagged" then that tag should
	 * be removed - otherwise, this tag is appended to the collection of tags on
	 * this file.
	 *
	 * @param file Path to file to tag
	 * @param tag  The desired tag
	 * @throws NoSuchFileException If no file exists with the given name/path
	 * @throws NoSuchTagException  If no tag exists with the given name
	 * @returns true if succeeding tagging the file, false if the file already
	 * has that tag
	 */
	@Override
	public boolean tagFile(String file, String tag) throws RemoteException, NoSuchFileException, NoSuchTagException {

		Tag ntag = new Tag(tag); // create new tag
		if(!tagFile.containsKey(file)){ // if there are no such file then raise exception
			throw new NoSuchFileException(file);
		}
		if(!tag1.containsKey(tag)){ // if tag is not in the hashmap then raise exception
			throw new NoSuchTagException();
		}
		if(tagFile.get(file).tags.containsKey("untagged")){ // if there's untagged tag then remove it 
			tagFile.get(file).tags.remove("untagged");
			tag1.get(tag).files.put(file,tagFile.get(file));
		}	
		if(!tagFile.get(file).tags.contains(tag)){ // if there are no such tag in the file, then put it in  
			tagFile.get(file).tags.put(tag, ntag);
		}
		if(tagFile.containsKey(file)){ // if the tagfile already have the same tag then raise exception
			return false;
		}
		return true;
	}

	/**
	 * Remove a tag from a file
	 * <p>
	 * If removing this tag causes the file to no longer have any tags, then the
	 * special "untagged" tag should be added.
	 * <p>
	 * The "untagged" tag can not be removed (return should be false)
	 *
	 * @param file Path to file to untag
	 * @param tag  The desired tag to remove from that file
	 * @throws NoSuchFileException If no file exists with the given name/path
	 * @throws NoSuchTagException  If no tag exists with the given name
	 * @returns True if the tag was successfully removed, false if there was no
	 * tag by that name on the specified file
	 */
	@Override
	public boolean removeTag(String file, String tag) throws RemoteException, NoSuchFileException, NoSuchTagException {
		synchronized(tagFile){ 
			if(tag.equals("untagged")){ // if tag is untagged then can't remove it
				return false;
			}
			if(!tag1.containsKey(tag)){ // if there are no tag then raise exception
				throw new NoSuchTagException();
			}

			if(!tagFile.containsKey(file)){ // if there are no file then raise exception
				throw new NoSuchFileException(file);
			}
			if(tagFile.get(file).tags.containsKey(tag)){ // if there are tag and file
				tagFile.get(file).tags.remove(tag); // then remove tag from file data structure
				Tag ntag = new Tag("untagged"); // create new tag that's untagged 
				if(tagFile.get(file).tags.size() ==0){ // if tag is empty then add untagged tag
					tagFile.get(file).tags.put("untagged", ntag);
				}
				return true; // true statement
			}
		}
		return false;
	}

	/**
	 * List all of the tags that are applied to a file
	 *
	 * @param file The file to inspect
	 * @return A list of all tags that have been applied to that file in any
	 * order
	 * @throws NoSuchFileException If the file specified does not exist
	 */
	@Override
	public Iterable<String> getTags(String file) throws RemoteException, NoSuchFileException {
		ArrayList<String> al = new ArrayList<String>();// create new tag arraylist

		if( !tagFile.containsKey(file)) { //catch exception
			throw new NoSuchFileException(file);
		}

		for(Tag tag : tagFile.get(file).tags.values()) { // add tags to the arraylist if it exist
			al.add(tag.getName());
		}
		return al; // return arraylist of tags
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
		if(tagFile.containsKey(name) == false) { // catch NoFileException
			throw new NoSuchFileException(name);
		}


		TaggedFile t = tagFile.get(name); // get the file with the tag

		if(forWrite == true) { //if forwrite is true then store lock onto write stamp
			Long lock = t.getLock().writeLock(); 
			writeStamp.put(lock, name); // store lock and stamp into writestamp hashmap
			counterStamp.put(lock, 0); // store the time and it time counter

			Runnable r=() ->{ 
				for(Long stamp: counterStamp.keySet()){// update times every second 
						Integer temp = counterStamp.get(stamp);
						temp++;
						counterStamp.put(stamp, temp);
					}			

			};timerExecutorService.scheduleAtFixedRate(r, 0, 1, TimeUnit.SECONDS);
			return lock;
		}
		if(forWrite == false){  //if forwrite is true then store lock onto read stamp
			Long lock = t.getLock().readLock();
			readStamp.put(lock, name); // store lock and stamp into readstamp hashmap
			counterStamp.put(lock, 0); // store the time and it time counter

			Runnable r=() ->{
				for(Long stamp: counterStamp.keySet()){		//update times every second	
						Integer temp = counterStamp.get(stamp);
						temp++; //increment the time by one
						counterStamp.put(stamp, temp);
					}			

			};timerExecutorService.scheduleAtFixedRate(r, 0, 1, TimeUnit.SECONDS);
			return lock;
		}	
		return 0;
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
		TaggedFile t = tagFile.get(name); //get file
		
		if(tagFile.containsKey(name) == false) { //check for file existence
			throw new NoSuchFileException(name);
		}
		if(counterStamp.containsKey(stamp) == false){ //check for stamp existence
			throw new IllegalMonitorStateException();
		}
		//if stamp exist but have more than 3 second, then unlock and remove the stamp from both hashmap
		if(counterStamp.containsKey(stamp)== true && counterStamp.get(stamp)>3){ 
			if(forWrite == true) {
				t.getLock().unlockWrite(stamp); 
				writeStamp.remove(stamp);
				counterStamp.remove(stamp);
			}
			else if(forWrite == false) {
				t.getLock().unlockRead(stamp);
				readStamp.remove(stamp);
				counterStamp.remove(stamp);
			}
			throw new IllegalMonitorStateException();
		}
		// else remove stamp from both data structure
		if(forWrite == true) {
			t.getLock().unlockWrite(stamp); 
			writeStamp.remove(stamp);
			counterStamp.remove(stamp);

		}
		else if(forWrite == false) {
			t.getLock().unlockRead(stamp);
			readStamp.remove(stamp);
			counterStamp.remove(stamp);

		}

	}


	/**
	 * Notifies the server that the client is still alive and well, still using
	 * the lock specified by the stamp provided
	 *
	 * @param file    The filename (same exact name passed to lockFile) that we are
	 *                reporting in on
	 * @param stampId Stamp returned from lockFile that we are reporting in on
	 * @param isWrite if the heartbeat is for a write lock
	 * @throws IllegalMonitorStateException if the stamp specified is not (or is no longer) valid, or if
	 *                                      the stamp is not valid for the given read/write state
	 * @throws NoSuchFileException          if the file specified doesn't exist
	 */
	@Override
	public void heartbeat(String file, long stampId, boolean isWrite) throws RemoteException, IllegalMonitorStateException, NoSuchFileException {

		TaggedFile t = tagFile.get(file); // get the file
		if(tagFile.containsKey(file) == false){ // catch no file exception
			throw new NoSuchFileException(file);
		}

		if(counterStamp.containsKey(stampId) == false){ // catch invalid stamp
			throw new IllegalMonitorStateException();
		}
		
		counterStamp.put(stampId, 0); // reset the stamp timer

	}

	/**
	 * Get a list of all of the files that are currently write locked
	 */
	@Override
	public List<String> getWriteLockedFiles() throws RemoteException {
		List<String> tempString = new ArrayList<String>(writeStamp.values());

		return tempString; // return the list of writestamp
	}

	/**
	 * Get a list of all of the files that are currently read locked
	 */
	@Override
	public List<String> getReadLockedFiles() throws RemoteException {
		List<String> tempString = new ArrayList<String>(readStamp.values());

		return tempString;//return the list of read stamp
	}

	@Override
	public void writeFile(String file, String content) throws RemoteException, IOException {
		Path path = Paths.get(file);
		if (!path.startsWith(ServerMain.BASEDIR))
			throw new IOException("Can only write to files in " + ServerMain.BASEDIR);
		Files.write(path, content.getBytes());

	}
}
