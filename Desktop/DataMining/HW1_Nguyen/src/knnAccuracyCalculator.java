import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class knnAccuracyCalculator {
 static HashMap<String, Integer> positiveWordMap = new HashMap<String, Integer>(); //all positivewords
 static HashMap<String, Integer> negativeWordMap = new HashMap<String, Integer>(); // all negativewords
 static HashMap<String, String> stopWords = new HashMap<String, String>();
 static List<String> listResult = new ArrayList<String>();
 static List<String> listFormat = new ArrayList<String>();

 static DecimalFormat decim = new DecimalFormat("#.##");


 public static void main(String[] args){

  storeTestResult("/Users/tringuyen/Desktop/DataMining/testResult6.data");
  storeFormat("/Users/tringuyen/Desktop/DataMining/format.data");
  storeStopWord("/Users/tringuyen/Desktop/DataMining/StopWords.data");
  storePositiveWord("/Users/tringuyen/Desktop/DataMining/positiveWordsList.data");
  int counter = 0;
  try{
   for(int i=0; i<listResult.size();i++){
    if(listResult.get(i).equals(listFormat.get(i))){
     counter++;
    }
   }
   for(String s : positiveWordMap.keySet()){
    //System.out.println(s+" " +positiveWordMap.get(s));
   }
   //System.out.println(positiveWordMap.get("THE"));
   int size = listResult.size();
   double accu = (double)counter/(double)size*100;
   System.out.println("Accuracy: "+ accu+"%");
  }catch(IndexOutOfBoundsException e){
   System.out.println(listResult.size());
  }
 }

 public static void storeStopWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){

    //System.out.print(token[0]+token[1]);

    stopWords.put(fileRead, fileRead);
    fileRead = br.readLine();

   }
   br.close();
  } catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }

 /* read and store list of positive words from positive file into positiveWordMap<word,count>*/
 public static void storeTestResult(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();
   int count =0;
   while(fileRead != null){
    //positiveWordMap.put(count, fileRead);
    listResult.add(fileRead);
    fileRead = br.readLine();
    count++;
   }
   br.close();
  } catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }

 /* read and store list of positive words from negative file into negative WordMap<word,count>*/
 public static void storeFormat(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();
   int count =0;
   while(fileRead != null){
    //negativeWordMap.put(count, fileRead);
    listFormat.add(fileRead);
    fileRead = br.readLine();
    count++;
   }
   br.close();
  } catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }


 /* read and store list of positive words from positive file into positiveWordMap<word,count>*/
 public static void storePositiveWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
    String[] token = fileRead.split("\t");
    //System.out.print(token[0]+token[1]);
    String posWord = token[0];
    if(stopWords.containsKey(posWord)){
     fileRead=br.readLine();
     continue;
    }else{
     int posCount = Integer.parseInt(token[1]);
     positiveWordMap.put(posWord, posCount);
     fileRead = br.readLine();
    }
   }
   br.close();
  } catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }

 /* read and store list of positive words from negative file into negative WordMap<word,count>*/
 public static void storeNegativeWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
    String[] token = fileRead.split("\t");
    //System.out.print(token[0]+token[1]);
    String posWord = token[0];
    if(stopWords.containsKey(posWord)){
     fileRead = br.readLine();
     continue;
    }else{
     int posCount = Integer.parseInt(token[1]);
     negativeWordMap.put(posWord, posCount);
     fileRead = br.readLine();
    }
   }
   br.close();
  } catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }

}
