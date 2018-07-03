package edu.gmu.cs475;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

import edu.gmu.cs475.struct.ITaggedFile;

public class TaggedFile implements ITaggedFile {
	
	public ConcurrentHashMap<String, Tag> tags= new ConcurrentHashMap<String, Tag>();
	public ArrayList<Tag>  tags1= new ArrayList<Tag>();;
	public StampedLock lock= new StampedLock();
	
	
	public StampedLock getLock() {
		StampedLock s= this.lock;
		return lock;
	}
	private Path path;
	public boolean addTag1( String st, Tag tag){
		if(tags.contains(tag)){
			return false;
		}
		if(!tags.contains(tag)){
			tags.put(st,tag);
		}
		
		return false;
	}
	public boolean removeTag1(String tag){
		if(tags.contains(tag)){
			tags.remove(tag);
			return true;
		}
		return false;
	}
	public ConcurrentHashMap<String,Tag> getTag(){
		
		return tags;
	}
	public TaggedFile(Path path)
	{

		this.path = path;
	}
	
	@Override
	public String getName() {
		return path.toString();
	}
	@Override
	public String toString() {
		return getName();
	}
	
}
