package edu.gmu.cs475;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import edu.gmu.cs475.struct.ITag;
import edu.gmu.cs475.struct.ITaggedFile;
import edu.gmu.cs475.struct.NoSuchTagException;
import edu.gmu.cs475.struct.TagExistsException;

public class FileTagManager extends AbstractFileTagManager {
	ConcurrentHashMap<String, Tag> tag1= new ConcurrentHashMap<String, Tag>(); // datastructure to keep hold of tags
	ConcurrentHashMap<String, TaggedFile> tagFile= new ConcurrentHashMap<String, TaggedFile>(); // datastructure for file
	//ArrayList<Tag> tag2 = new ArrayList<Tag>(); // data structure for a list of tag
	//ArrayList<TaggedFile> taggedFile2 = new ArrayList<TaggedFile>();
	ReadWriteLock rwlock = new ReentrantReadWriteLock();
	
	
	@Override
	public Iterable<? extends ITag> listTags() {
		// TODO Auto-generated method stub
		return tag1.values(); // return values of the hashtable
	}

	@Override
	public ITag addTag(String name) throws TagExistsException {
		// TODO Auto-generated method stub
		Tag ntag = new Tag(name); // created new tag
		ntag.getLock(); // call lock
		synchronized(tag1){ // synchronized operation because of adding

			if(tag1.containsKey("untagged")){ // check if tag is "untagged", if so remove and replace with tag
				tag1.remove("untagged");
				tag1.put(name, ntag);
			}
			else if(tag1.containsKey(name)){  // check if tag is already available, if so then raise exception
				throw new TagExistsException();
			}else{
				tag1.put(name, ntag); // add tag to the hashmap
			}
		}
		return ntag;

	}

	@Override
	public ITag editTag(String oldTagName, String newTagName) throws TagExistsException, NoSuchTagException {
		// TODO Auto-generated method stub
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

		return ntag;
	}

	@Override
	public ITag deleteTag(String tagName) throws NoSuchTagException, DirectoryNotEmptyException {
		// TODO Auto-generated method stub
		Tag ntag = new Tag(tagName); // create new tag
		ntag.getLock(); // lock tag
		if(!tag1.containsKey(tagName)){ // check if there are such tag, if not then raise exception
			throw new NoSuchTagException();
		}
		for(int i=0;i<tagFile.size();i++){ // go through  of tagfile  to check if it store the tag, if so raise exeption
			if(tag1.containsKey(tagName)){
				throw new DirectoryNotEmptyException(tagName);
			}
		}
		if(tag1.containsKey(tagName)){ // if tag is in tag hashmap then remove it
			synchronized(tag1){ // synchronized since this is removing
				tag1.remove(tagName);
			}
		}
		return ntag;
	}

	@Override
	public void init(List<Path> files) { 
		// TODO Auto-generated method stub
		Tag ntag= new Tag("untagged"); //create new tag
		//synchronized(tag1){
		for(int i=0; i<files.size();i++){ // go through file path
			tag1.put("untagged", ntag); // add untagged to tag hashmap
			TaggedFile nfiles = new TaggedFile(files.get(i)); // create new file
			nfiles.tags.put("untagged",ntag); // put untagged onto tag that belongs to TaggedFile
			tagFile.put(files.get(i).toString(), nfiles); // add new file to TaggedFile
		}
		//}

	}

	@Override
	public Iterable<? extends ITaggedFile> listAllFiles() {

		// TODO Auto-generated method stub

		return  new ArrayList<TaggedFile>(tagFile.values()); // return a list of files stored in hashmap above
	}

	@Override
	public Iterable<? extends ITaggedFile> listFilesByTag(String tag) throws NoSuchTagException {
		// TODO Auto-generated method stub
		ArrayList<TaggedFile> fileByTag = new ArrayList<TaggedFile>(); // arraylist to temporary store list of file 
		
		if(tag1.containsKey(tag)){  // if tag is in the hashmap, then add it on the the datastructure
			fileByTag = new ArrayList<TaggedFile>(tagFile.values()); 
		}
		if(!tag1.containsKey(tag)){ // raise exception if the tag doesn't belong to tag hashmap
			throw new NoSuchTagException();
		}
		return fileByTag;

	}

	@Override
	public boolean tagFile(String file, String tag) throws NoSuchFileException, NoSuchTagException {
		// TODO Auto-generated method stub
		Tag ntag = new Tag(tag); // create new tag
		if(tagFile.get(file).tags.containsKey("untagged")){ // if there's untagged tag then remove it 
			tagFile.get(file).tags.remove("untagged");
		}

		if(!tag1.containsKey(tag)){ // if tag is not in the hashmap then raise exception
			throw new NoSuchTagException();
		}
		if(!tagFile.get(file).tags.contains(tag)){ // if there are no such tag in the file, then put it in  
			tagFile.get(file).tags.put(tag, ntag);
		}
		if(tagFile.containsKey(file)){ // if the tagfile already have the same tag then raise exception
			return false;
		}

		if(!tagFile.containsKey(file)){ // if there are no such file then raise exception
			throw new NoSuchFileException(file);
		}

		return false;
	}

	@Override
	public boolean removeTag(String file, String tag) throws NoSuchFileException, NoSuchTagException {
		// TODO Auto-generated method stub

		//ntag.getLock();
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

	@Override
	public Iterable<? extends ITag> getTags(String file) throws NoSuchFileException {
		// TODO Auto-generated method stub
		Collection<Tag> ntag = new ArrayList<Tag>(); // create new tag arraylist
		if(tagFile.containsKey(file)){ // if files has the tag then add it into the arraylist
			synchronized(tag1){
				ntag = tagFile.get(file).tags.values();
			}
		}

		return ntag;
	}

	@Override
	public String catAllFiles(String tag) throws NoSuchTagException, IOException {
		// TODO Auto-generated method stub

		if(tag1.containsKey(tag)){	 
			StringBuilder sb = new StringBuilder(); // use string builder to return the string
			Iterable<? extends ITaggedFile> tempString = listFilesByTag(tag); //list all files by tag and add them to a list 
			ArrayList<TaggedFile> tempList = new ArrayList<TaggedFile>(); //list
			synchronized(this){
				for(ITaggedFile t : tempString){
					tempList.add((TaggedFile) t); // add file to the list
				}
				for(int i=0; i<tempList.size(); i++){ 

					sb.append(readFile(tempList.get(i).getName())); //add list to stringbuilder
				}
				//System.out.println(sb.toString());
			}
			return sb.toString(); // return the string from stringbuilder

		}

		if(!tag1.containsKey(tag)){ // throw exception if tag is not found
			throw new NoSuchTagException();
		}

		return null;

	}

	@Override
	public void echoToAllFiles(String tag, String content) throws NoSuchTagException, IOException {
		// TODO Auto-generated method stub
		if(tag1.containsKey(tag)){ // check if tag is available
			//long wl = sl.readLock();

			Iterable<? extends ITaggedFile> tempString = listFilesByTag(tag); //list all files by tag and add them to a list 
			ArrayList<TaggedFile> tempList = new ArrayList<TaggedFile>(); //list
			synchronized(this){
				for(ITaggedFile t : tempString){
					tempList.add((TaggedFile) t);// add file to the list
				}
				//sl.writeLock();
				for(int i=0; i<tempList.size(); i++){
					writeFile(tempList.get(i).getName(),content); // use given writeFile method to write to file
				}
			}
			//sl.unlockRead(wl);
		}
		if(!tag1.containsKey(tag)){ // throw exception if tag is not found
			throw new NoSuchTagException();
		}


	}

	@Override
	public long lockFile(String name, boolean forWrite) throws NoSuchFileException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void unLockFile(String name, long stamp, boolean forWrite) throws NoSuchFileException {
		// TODO Auto-generated method stub

	}


}
