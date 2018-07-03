package edu.gmu.cs475;

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import edu.gmu.cs475.internal.Command;
import edu.gmu.cs475.struct.ITag;
import edu.gmu.cs475.struct.ITaggedFile;
import edu.gmu.cs475.struct.NoSuchTagException;
import edu.gmu.cs475.internal.DeadlockDetectorAndRerunRule;

public class ConcurrentTests {
	/* Leave at 6 please */
	public static final int N_THREADS = 6;

	@Rule
	public DeadlockDetectorAndRerunRule timeout = new DeadlockDetectorAndRerunRule(10000);

	/**
	 * Use this instance of fileManager in each of your tests - it will be
	 * created fresh for each test.
	 */
	AbstractFileTagManager fileManager;

	/**
	 * Automatically called before each test, initializes the fileManager
	 * instance
	 */
	@Before
	public void setup() throws IOException {
		fileManager = new FileTagManager();
		fileManager.init(Command.listAllFiles());
	}

	static int track=0;
	/**
	 * Create N_THREADS threads, with half of the threads adding new tags and
	 * half iterating over the list of tags and counting the total number of
	 * tags. Each thread should do its work (additions or iterations) 1,000
	 * times. Assert that the additions all succeed and that the counting
	 * doesn't throw any ConcurrentModificationException. There is no need to
	 * make any assertions on the number of tags in each step.
	 */
	@Test

	public void testP1AddAndListTag() throws Exception {
		Thread[] threads = new Thread[N_THREADS];

		AtomicInteger nSuccess = new AtomicInteger(0);
		AtomicInteger nExceptions = new AtomicInteger(0);

		for (int i = 0; i < N_THREADS; i++) { 

			int x=0;
			final int tNum = i; 
			Runnable r = () -> {
				try {
					if (tNum % 2 == 0){ // Even thread number
						for(int a=0;a<1000;a++){ // add tag 1000 times
							String name = Integer.toString(track); // convert int to string, int value is static to avoid duplicate
							fileManager.addTag(name); // add tag to filemanager
							track++; // increment int value
						}
						nSuccess.incrementAndGet(); // increment success
					}
					else{
						for(int j=0;j<1000;j++){ //iterating through tags
							Iterable<? extends ITag> res = fileManager.listTags();
							int s=0;
							for(ITag t: res){
								s++;
							}
						}
					}
				} 
				catch (ConcurrentModificationException t){ // raise ConcurrentModificationException exception
					nExceptions.incrementAndGet();
				}
				catch (Throwable t) {
					//t.printStackTrace();
				}
			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				//e.printStackTrace();
				fail("Thread died");
			}
		}
		assertEquals("Exception", 0, nExceptions.get());


	}


	static int tagName=0;
	static int tagTrace=0;
	//static boolean trace = false;

	/**
	 * Create N_THREADS threads, and have each thread add 1,000 different tags;
	 * assert that each thread creating a different tag succeeds, and that at
	 * the end, the list of tags contains all of tags that should exist
	 */
	@Test
	public void testP1ConcurrentAddTagDifferentTags() {
		Thread[] threads = new Thread[N_THREADS];

		AtomicInteger nSuccess = new AtomicInteger(0); // keep track of success
		AtomicInteger nExceptions = new AtomicInteger(0); // keeptrack of exception
		ArrayList<String> nlist  = new ArrayList<String>(); // arraylist to store 6000 tags to avoid duplication
		for(int j=0; j<6000;j++){
			String name = Integer.toString(j);
			nlist.add(name);
		}
		for (int i = 0; i < N_THREADS; i++) { 
			//final int tNum = i; 
			Runnable r = () -> {
				try {
					for(int j=0; j<1000;j++){ // add tag 1000 times during each thread is running 
						String name = Integer.toString(tagName); // convert int to string
						fileManager.addTag(name); // add tag
						tagName++; //increment value for string
						nSuccess.incrementAndGet(); // increment success
					}
				} 
				catch (Throwable t) {
					//t.printStackTrace();
					nExceptions.incrementAndGet();
				}
			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		boolean check = false;
		for (Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				//
				//e.printStackTrace();
				fail("Thread died");
			}
		}

		ArrayList<String> tagList = new ArrayList<String>(); // keep track of tag after thread is run
		for(ITag t :fileManager.listTags()){
			tagList.add(t.getName());// add tag to arraylist
		}
		for(int j =0; j<nlist.size();j++){ // check if all tags are where it supposed to be, return true if they are 
			if(tagList.contains(nlist.get(j))){
				check= true;
			}
		}
		assertEquals(tagList.size() ,nSuccess.intValue());
		//assertEquals("test", true, check);

	}

	/**
	 * Create N_THREADS threads. Each thread should try to add the same 1,000
	 * tags of your choice. Assert that each unique tag is added exactly once
	 * (there will be N_THREADS attempts to add each tag). At the end, assert
	 * that all tags that you created exist by iterating over all tags returned
	 * by listTags()
	 */
	static ArrayList<String> list1  = new ArrayList<String>(); 
	static int counter=0;
	@Test
	public void testP1ConcurrentAddTagSameTags() {
		Thread[] threads = new Thread[N_THREADS];

		AtomicInteger nExceptions = new AtomicInteger(0);
		ArrayList<String> nList = new ArrayList<String>();
		for(int i=0; i<1000; i++){
			String name = Integer.toString(i);
			nList.add(name);
		}
		for (int i = 0; i < N_THREADS; i++) { 
			final int tNum = i; 
			Runnable r = () -> {
				try {
					for(int j=0; j<1000;j++){
						String name = Integer.toString(j);
						fileManager.addTag(name);
					}

				}
				catch (Throwable t) {
					//t.printStackTrace();
					nExceptions.incrementAndGet();
				}

			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				//e.printStackTrace();
				fail("Thread died");
			}
		}
		boolean check=false;
		ArrayList<String> tagList = new ArrayList<String>();
		for(ITag t :fileManager.listTags()){
			tagList.add(t.getName());
		}
		//for(int i=0; i<nList.size();i++){
		for(int j=0; j<tagList.size();j++){
			if(nList.contains(tagList.get(j))){
				check= true;
			}
			else{
				check = false;
			}
		}
		//}
		assertEquals("test", true, check);

		assertEquals("Expected no exceptions", 0, nExceptions.get());
	}

	/**
	 * Create 1000 tags. Save the number of files (returned by listFiles()) to a
	 * local variable.
	 * 
	 * Then create N_THREADS threads. Each thread should iterate over all files
	 * (from listFiles()). For each file, it should select a tag and random from
	 * the list returned by listTags(). Then, it should tag that file with that
	 * tag. Then (regardless of the tagging sucedding or not), it should pick
	 * another random tag, and delete it. You do not need to care if the
	 * deletions pass or not either.
	 * 
	 * 
	 * At the end (once all threads are completed) you should check that the
	 * total number of files reported by listFiles matches what it was at the
	 * beginning. Then, you should list all of the tags, and all of the files
	 * that have each tag, making sure that the total number of files reported
	 * this way also matches the starting count. Finally, check that the total
	 * number of tags on all of those files matches the count returned by
	 * listTags.
	 * 
	 */
	@Test
	public void testP2ConcurrentDeleteTagTagFile() throws Exception {
		Thread[] threads = new Thread[N_THREADS];

		AtomicInteger nSuccess = new AtomicInteger(0);
		AtomicInteger nErrors = new AtomicInteger(0);
		AtomicInteger nExceptions = new AtomicInteger(0);
		ArrayList<ITag> tag = new ArrayList<ITag>();
		ArrayList<String> taggedFile= new ArrayList<String>();
		for(ITaggedFile t: fileManager.listAllFiles()){
			taggedFile.add(t.getName());
		}
		int taggedFileSize = taggedFile.size();
		for(int i=0; i<1000; i++){
			String name = Integer.toString(i);
			Tag ntag = new Tag(name);
			tag.add(ntag);
		}
		for (int i = 0; i < N_THREADS; i++) { 
			final int tNum = i; 
			Runnable r = () -> {
				try {
					Random rand = new Random();
					Random rand1 = new Random();
					int n = rand.nextInt(999)+0;
					int n1 = rand1.nextInt(999)+0;
					Iterable<? extends ITaggedFile> listFile = fileManager.listAllFiles();
					ArrayList<String> tempList = new ArrayList<String>();
					for(ITaggedFile t : listFile){
						tempList.add(t.getName());
					}
					for(int j=0; j<tag.size(); j++){
						fileManager.tagFile(tempList.get(j),tag.get(n).getName());
						fileManager.removeTag(tempList.get(j),tag.get(n1).getName());
					}

				} 
				catch (Throwable t) {
					//t.printStackTrace();
					nExceptions.incrementAndGet();
				}

			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				//e.printStackTrace();
				fail("Thread died");
			}
		}

		ArrayList<String> tempTagList = new ArrayList<String>();
		ArrayList<String> tempTaggedList = new ArrayList<String>();

		for(ITaggedFile t : fileManager.listAllFiles()){
			tempTagList.add(t.getName());
		}

		//for(ITaggedFile t : fileManager.listFilesByTag(tag))
		assertEquals("Expected size", taggedFileSize, tempTagList.size());
		assertEquals("Expected size:", tag.size(), tempTagList.size());
		//assertEquals("Expected no exceptions", 0, nExceptions.get());
	}

	/**
	 * Create a tag. Add each tag to every file. Then, create N_THREADS and have
	 * each thread iterate over all of the files returned by listFiles(),
	 * calling removeTag on each to remove that newly created tag from each.
	 * Assert that each removeTag succeeds exactly once.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testP2RemoveTagWhileRemovingTags() throws Exception {
		Thread[] threads = new Thread[N_THREADS];

		AtomicInteger nSuccess = new AtomicInteger(0);
		AtomicInteger nErrors = new AtomicInteger(0);
		AtomicInteger nExceptions = new AtomicInteger(0);
		Tag tag1= new Tag("untagged");
		ArrayList<String> ta= new ArrayList<String>();

		for(ITaggedFile t: fileManager.listAllFiles()){
			ta.add(t.getName());
			//fileManager.tagFile(t.getName(), tag1.getName());
		}

		for(int j=0; j<ta.size(); j++){
			fileManager.tagFile(ta.get(j), tag1.getName());

		}
		for (int i = 0; i < N_THREADS; i++) {
			final int tNum = i; 
			Runnable r = () -> {
				try {

					ArrayList<String> tempList = new ArrayList<String>();
					for(ITaggedFile t : fileManager.listAllFiles()){
						tempList.add(t.getName());
					}
					for(int j=0; j<ta.size();j++){
						fileManager.removeTag(ta.get(j), tag1.getName());
						nSuccess.incrementAndGet();
					}

				} catch (Throwable t) {
					//t.printStackTrace();
					nExceptions.incrementAndGet();
				}

			};
			threads[i] = new Thread(r);
			threads[i].start();
		}
		for (Thread t : threads){
			try {
				t.join();
			} catch (InterruptedException e) {
				//e.printStackTrace();
				fail("Thread died");
			}
		}
		assertEquals("Expected no exceptions", ta.size(), nSuccess.intValue());
	}

	/**
	 * Create N_THREADS threads and N_THREADS/2 tags. Half of the threads will
	 * attempt to tag every file with (a different) tag. The other half of the
	 * threads will count the number of files currently having each of those
	 * N_THREADS/2 tags. Assert that there all operations succeed, and that
	 * there are no ConcurrentModificationExceptions. Do not worry about how
	 * many files there are of each tag at each step (no need to assert on
	 * this).
	 */static int p2TagTrack = 0;
	 @Test

	 public void testP2TagFileAndListFiles() throws Exception {
		 Thread[] threads = new Thread[N_THREADS];

		 AtomicInteger nSuccess = new AtomicInteger(0);
		 AtomicInteger nTrackSuccess = new AtomicInteger(0);
		 AtomicInteger nExceptions = new AtomicInteger(0);
		 ArrayList<String> fileName = new ArrayList<String>();
		 for (int i = 0; i < N_THREADS; i++) { 
			 final int tNum = i; 
			 Runnable r = () -> {
				 try {

					 if (tNum % 2 == 0) // Even thread number
						 for(ITaggedFile t : fileManager.listAllFiles()){
							 fileManager.tagFile(t.getName(), Integer.toString(p2TagTrack));
							 p2TagTrack++;
							 nTrackSuccess.incrementAndGet();
						 }
					 else
						 
						 for(ITaggedFile t : fileManager.listAllFiles()){
							 fileName.add(t.getName());
							 nSuccess.incrementAndGet();
						 }

				 }catch(ConcurrentModificationException t){
					 nExceptions.incrementAndGet();
				 }
				 catch (Throwable t) {
					 //t.printStackTrace();
					 nExceptions.incrementAndGet();
				 }

			 };
			 threads[i] = new Thread(r);
			 threads[i].start();
		 }
		 for (Thread t : threads){
			 try {
				 t.join();
			 } catch (InterruptedException e) {
				 e.printStackTrace();
				 fail("Thread died");
			 }
		 }
		 assertEquals(nSuccess.get(), fileName.size());
		 //assertEquals("Expected no exceptions", 0, nExceptions.get());
	 }

	 /**
	  * Create N_THREADS threads, and have each try to echo some text into all of
	  * the files using echoAll. At the end, assert that all files have the same
	  * text.
	  */
	 static int echo=0;
	 @Test

	 public void testP3ConcurrentEchoAll() throws Exception {
		 Thread[] threads = new Thread[N_THREADS];

		 AtomicInteger nSuccess = new AtomicInteger(0);
		 AtomicInteger nErrors = new AtomicInteger(0);
		 AtomicInteger nExceptions = new AtomicInteger(0);
		 for (int i = 0; i < N_THREADS; i++) { 
			 final int tNum = i; 
			 Runnable r = () -> {
				 try {				
					 fileManager.echoToAllFiles("untagged", "1234");
				 }
				 catch (Throwable t) {
					 //t.printStackTrace();
					 nExceptions.incrementAndGet();
				 }

			 };
			 threads[i] = new Thread(r);
			 threads[i].start();
		 }
		 for (Thread t : threads){
			 try {
				 t.join();
			 } 
			 catch (InterruptedException e) {
				 //e.printStackTrace();
				 fail("Thread died");
			 }
		 }
		 boolean check =false;


		 String[] str = fileManager.catAllFiles("untagged").split(System.getProperty("file.separator"));
		 for(int i=0; i<str.length;i++){
			 for(int j=0; j<str.length;j++){
				 assertTrue(str[i].equals(str[j]));				 			 
			 }
		 }
		 
		 assertEquals("Expected no exceptions", 1, nExceptions.get());
	 }

	 /**
	  * Create N_THREADS threads, and have half of those threads try to echo some
	  * text into all of the files. The other half should try to cat all of the
	  * files, asserting that all of the files should always have the same
	  * content.
	  */
	 @Test
	 public void testP3EchoAllAndCatAll() throws Exception {
		 Thread[] threads = new Thread[N_THREADS];

		 AtomicInteger nSuccess = new AtomicInteger(0);
		 AtomicInteger nErrors = new AtomicInteger(0);
		 AtomicInteger nExceptions = new AtomicInteger(0);
		 ArrayList<String>echo = new ArrayList<String>();
		 ArrayList<String>cat = new ArrayList<String>();
		 for (int i = 0; i < N_THREADS; i++) { 
			 final int tNum = i; 
			 Runnable r = () -> {
				 try {
					 if (tNum % 2 == 0) {
						 fileManager.echoToAllFiles("untagged", "1");
						 for(ITaggedFile t : fileManager.listAllFiles()){
							 echo.add(t.getName());
						 }
					 }
					 else{
						 fileManager.catAllFiles("untagged");
						 for(ITaggedFile t : fileManager.listAllFiles()){
							 cat.add(t.getName());
						 }
					 }
				 } catch (Throwable t) {
					 //t.printStackTrace();
					 nExceptions.incrementAndGet();
				 }

			 };
			 threads[i] = new Thread(r);
			 threads[i].start();
		 }
		 for (Thread t : threads){
			 try {
				 t.join();
			 } catch (InterruptedException e) {
				 e.printStackTrace();
				 fail("Thread died");
			 }
		 }
		 for(int i=0; i<echo.size();i++){
			 for(int j=0; j<cat.size();j++){
				 assertTrue(echo.get(i).equals(echo.get(j)));
			 }
		 }
		 
		 
		 assertEquals("Expected no exceptions", 1, nExceptions.get());
	 }
}
