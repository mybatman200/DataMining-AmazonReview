import java.io.*;
import java.util.*;
import java.text.*;

public class tfidf {

 static Map<Integer, double[]> testSetCommentMap = new HashMap<Integer, double[]>(); //counter, [pos, neg, +/-]
 static HashMap<String, Integer> positiveWordMap = new HashMap<String, Integer>(); //all positivewords
 static HashMap<String, Integer> negativeWordMap = new HashMap<String, Integer>(); // all negativewords
 static HashMap<String, Integer> wordRepCount = new HashMap<String,Integer>();
 static Map<Double, double[]> commentsMap = new TreeMap<Double, double[]>(); // pos, neg

 static Map<String, String> stopWords = new HashMap<String, String>();
 static DecimalFormat decim = new DecimalFormat("#.##");

 public static void main(String[] args){
  storeStopWord("/Users/tringuyen/Desktop/DataMining/StopWords.data");
  storePositiveWord("/Users/tringuyen/Desktop/DataMining/positiveWordsList.data");
  storeNegativeWord("/Users/tringuyen/Desktop/DataMining/negativeWordsList.data");
  storePositiveComment("/Users/tringuyen/Desktop/DataMining/positiveTrainingSet.data");
  storeNegativeComment("/Users/tringuyen/Desktop/DataMining/negativeTrainingSet.data");
  storeTestMap("/Users/tringuyen/Desktop/DataMining/testSet1.data");

    for(int i: testSetCommentMap.keySet()){
     //System.out.println(testSetCommentMap.get(i)[0]+" " + testSetCommentMap.get(i)[1]);
     System.out.println(calEucl(testSetCommentMap.get(i)[0], testSetCommentMap.get(i)[1], 7));
     double disInt =calEucl(testSetCommentMap.get(i)[0], testSetCommentMap.get(i)[1], 7);
     try {
      BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/tringuyen/Desktop/DataMining/testResult6.data", true));
      if(disInt>0){
       writer.write("+1" + "\n");
      }else{
       writer.write("-1"+ "\n");
      }
      writer.close();
  
     } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println("exception Caught");
      e.printStackTrace();
     }
    }

 }

 public static double[] wordsScoreCalculator(String input){
  double positive = 0.000000;
  double negative = 0.000000;
  double negativeDocCount = 901530;
  double positiveDocCount = 970901;
  double[] posNegScore = new double [2];

  //input = "hello The World World Hello Hello the end is here alo hola";

  String upperCaseInput = input.toUpperCase();
  String [] wordSplit = upperCaseInput.split("\\s");
  HashMap<String, Integer> tempHashMap = new HashMap<String, Integer>();
  double stringLength = wordSplit.length;

  int trace = 0, counter = 0;

  while(trace <wordSplit.length){
   counter =0;
   if(tempHashMap.containsKey(wordSplit[trace])){
    trace++;
    continue;
   }else{
    for(int i=0; i<wordSplit.length; i++){

     if(wordSplit[trace].equals(wordSplit[i])){
      counter++;
     }
    }
    tempHashMap.put(wordSplit[trace], counter);
    trace++;
   }
  }
  
  
  
  double occur = 0, temp=0;
  
  for(String i : tempHashMap.keySet()){
   double answer = 0;
   if(positiveWordMap.get(i) != null && stopWords.get(i)==null){
    occur = tempHashMap.get(i);
    answer = ((occur/stringLength)*(Math.log(positiveDocCount)/positiveWordMap.get(i)));
    positive = positive + answer;
   }
   if(negativeWordMap.get(i) != null && stopWords.get(i)==null){
    occur = tempHashMap.get(i);
    answer = ((occur/stringLength)*(Math.log(negativeDocCount)/negativeWordMap.get(i)));
    negative = negative +answer;
   }
   if(positiveWordMap.get(i) == null && negativeWordMap.get(i)==null){
    occur = tempHashMap.get(i);
    answer = ((occur/stringLength)*(Math.log(1)));
    //temp = temp + answer;
   }
   
  }
  //System.out.println(positive+" " + negative);
  posNegScore[0] = positive + temp;
  posNegScore[1] = negative + temp;
  return posNegScore;
 }

 public static double calEucl(double a, double b, int k){
  double a1=0, b1=0, result=0;
  TreeMap <Double, Double> tempMap = new TreeMap<Double, Double>();
  for(double i : commentsMap.keySet()){
   a1 = commentsMap.get(i)[0];
   b1 = commentsMap.get(i)[1];
   result = cosineSim(a,b,a1,b1);
   tempMap.put(result, commentsMap.get(i)[3]);
  }
  int count=0, countPos=0, countNeg=0;
  for(double i: tempMap.keySet()){
   //System.out.println(count);
   if(count>= k-1){
    break;
   }
   if(tempMap.get(i)==1){
    countPos++;
   }
   if(tempMap.get(i)==-1){
    countNeg++;
   }
   count++;
  }
  if(countPos>countNeg){
   return 1.0;
  }
  else{
   return -1.0;
  }


 }

 public static double cosineSim(double posA, double posB, double negA, double negB){
  double result = posA*negA;
  double result2 = posB*negB;
  double dotProduct = result+result2;
  double normA = Math.pow(posA, 2);
  double normB = Math.pow(posB, 2);
  double negA2 = Math.pow(negA, 2);
  double negB2 = Math.pow(negB, 2);
  double finalA = Math.sqrt(normA + normB);
  double finalB = Math.sqrt(negA2 + negB2);
  double resultCosine = dotProduct/(finalA*finalB);
  return resultCosine; 
 }


 public static double EucledianCal(double posA, double posB, double negA, double negB){
  double ans =0,ans1=0;

  ans = Math.pow(posA- posB,2);
  ans1 = Math.pow(negA- negB, 2);
  return (Math.sqrt(ans+ans1));
 }




 public static void storeTestMap(String fileFolderName){
  int counterTest =0 ;
  try{
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
    String [] comment = fileRead.split("\n");
    double[] i = wordsScoreCalculator(comment[0]);
    double a = i[0];
    double b = i[1];
    double[] arr = new double[5];
    arr[0]=a;
    arr[1]=b;
    arr[2]= counterTest;
    arr[3]= 0;
    double average = counterTest;
    double averageRounded = Double.parseDouble(decim.format(average));
    while(testSetCommentMap.get(averageRounded) != null){
     averageRounded = averageRounded + 0.1;
     averageRounded = Math.round(averageRounded*100.00)/100.00;
    }

    testSetCommentMap.put(counterTest, arr);

    counterTest++;
    fileRead = br.readLine();

   }
   br.close();

  }catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }

 public static void storeStopWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
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
 public static void storePositiveWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
    String[] token = fileRead.split("\t");
    String posWord = token[0];
    //    if(stopWords.containsKey(posWord)){
    //     fileRead=br.readLine();
    //     continue;
    //    }else{
    int posCount = Integer.parseInt(token[1]);
    positiveWordMap.put(posWord, posCount);
    fileRead = br.readLine();
    //    }
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
    String posWord = token[0];
    //    if(stopWords.containsKey(posWord)){
    //     fileRead=br.readLine();
    //     continue;
    //    }else{
    int posCount = Integer.parseInt(token[1]);
    negativeWordMap.put(posWord, posCount);
    fileRead = br.readLine();
    //    }
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



 public static void storePositiveComment(String fileFolderName){
  try{
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();
   int counterPos = 0;
   while(fileRead != null){
    String [] comment = fileRead.split("\n");
    double[] i = wordsScoreCalculator(comment[0]);
    double a = i[0];
    double b = i[1];
    double[] arr = new double[4];
    arr[0]=a;
    arr[1]=b;
    arr[2]= counterPos;
    arr[3]= 1;
    double average = counterPos;
    double averageRounded = average;
    if(commentsMap.containsKey(averageRounded)){
     while(commentsMap.containsKey(averageRounded)){
      averageRounded = averageRounded + 0.01;
      averageRounded = Math.round(averageRounded*100.00)/100.00;
     }
    }

    commentsMap.put(averageRounded, arr);

    //System.out.println("Positive: "+ a+" "+b+ " "+ average);
    fileRead = br.readLine();
    counterPos++;
   }

   br.close();

  }catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }
 /*read positive comments from file, calculate the score using wordsScoreCalculator and store the score into positiveWordMap<counter, score in array> */
 public static void storeNegativeComment(String fileFolderName){
  try{
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   int counterNeg = 0;
   while(fileRead != null){
    String [] comment = fileRead.split("\n");
    double[] i = wordsScoreCalculator(comment[0]);
    double a = i[0];
    double b = i[1];
    double[] arr = new double[4];
    arr[0]=a; //positive
    arr[1]=b;//negative
    arr[2]= counterNeg; //counterr
    arr[3]= -1;
    double average = counterNeg;

    //double averageRounded = Double.parseDouble(decim.format(average));
    double averageRounded = average;
    if(commentsMap.containsKey(averageRounded)){
     while(commentsMap.containsKey(averageRounded)){
      averageRounded = averageRounded + 0.01;
      averageRounded = Math.round(averageRounded*100.00)/100.00;
     }
    }

    //System.out.println(averageRounded);
    commentsMap.put(averageRounded, arr);

    //System.out.println("Negative: " + a+" "+b+ " "+ average);
    fileRead = br.readLine();
    counterNeg--;
   }
   br.close();

  }catch (FileNotFoundException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  } catch (IOException e) {
   // TODO Auto-generated catch block
   e.printStackTrace();
  }
 }




}
