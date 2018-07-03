package edu.gmu.cs475;

import java.util.concurrent.locks.StampedLock;

public class File{
	private StampedLock lock = new StampedLock();
	private String content;
	private String path;
	
	public File( String c){
	//	this.path = path;
		content = c;
	}
	public void setContent(String c){
		content = c;
	}
	public StampedLock getLock() {
		StampedLock temp = this.lock;
		return temp;
	}
	public String getContent(){
		return content;
	}
	public String getPath(){
		return path;
	}
	
	
	
}
