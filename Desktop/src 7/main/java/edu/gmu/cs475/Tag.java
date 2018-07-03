package edu.gmu.cs475;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

import edu.gmu.cs475.struct.ITag;

public class Tag implements ITag {
	public ConcurrentHashMap<String, TaggedFile> files = new ConcurrentHashMap<String, TaggedFile>();
	public ArrayList<TaggedFile> files1 = new ArrayList<TaggedFile>();
	private StampedLock lock = new StampedLock();

	private String name;

	public Tag(String name) {
		this.name = name;
	}

	public StampedLock getLock() {
		return lock;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String newTagName) {
		this.name = newTagName;
	}
}
