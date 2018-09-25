import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class knn1 {
 static HashMap<String, Integer> positiveWordMap = new HashMap<String, Integer>(); //all positivewords
 static HashMap<String, Integer> negativeWordMap = new HashMap<String, Integer>(); // all negativewords
 static Map<Double, double[]> commentsMap = new TreeMap<Double, double[]>(); // pos, neg
 static Map<String, String> stopWords = new HashMap<String, String>();
 //static Map<Double, int[]> negativeCommentMap = new TreeMap<Double, int[]>(); //pos, neg
 static Map<Double, double[]> trainSetCommentMap = new HashMap<Double, double[]>(); //pos, neg
 static Map<Integer, Integer> testResult = new TreeMap<Integer, Integer>(); //id, +1/-1
 static double [] posHashKeyArr;  static double [] negHashKeyArr;  static double [] trainHashKeyArr;
 //static ArrayList<String> positiveComments; 
 static int counterPos = 0;
 static int counterNeg = 0;
 static int counterTest = 0;
 static DecimalFormat decim = new DecimalFormat("#.##");

 public static void main(String[] args){
  storeStopWord("/Users/tringuyen/Desktop/DataMining/StopWords.data");
  storePositiveWord("/Users/tringuyen/Desktop/DataMining/positiveWordsList.data");
  storeNegativeWord("/Users/tringuyen/Desktop/DataMining/negativeWordsList.data");
  storePositiveComment("/Users/tringuyen/Desktop/DataMining/positiveTrainingSet.data");
  storeNegativeComment("/Users/tringuyen/Desktop/DataMining/negativeTrainingSet.data");
  storeTrainSetComment("/Users/tringuyen/Desktop/DataMining/testSet1.data");
  //knnEucl(5, 388.0);
  for(Double trainSetDouble : trainSetCommentMap.keySet()){
   //System.out.println(trainSetDouble);
   calculateKnn(7, trainSetDouble);
   double i = trainSetCommentMap.get(trainSetDouble)[3];
   int disInt = (int) i;
   try {
    BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/tringuyen/Desktop/DataMining/testResult.data", true));
    if(disInt>0){
     writer.write("+1" + "\n");
    }else{
     writer.write(disInt+ "\n");
    }
    writer.close();

   } catch (IOException e) {
    // TODO Auto-generated catch block
    System.out.println("exception Caught");
    e.printStackTrace();
   }

  }

  //System.out.println(trainSetCommentMap.size());


  //System.out.println(commentsMap.size());
  //System.out.println(commentsMap.keySet());
  //System.out.println(commentsMap.get(3.0199999999999996)[3]);
  //System.out.println(trainSetCommentMap.size());
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
 public static void storePositiveWord(String fileFolderName){
  try {
   BufferedReader br = new BufferedReader(new FileReader(fileFolderName));
   String fileRead = br.readLine();

   while(fileRead != null){
    String[] token = fileRead.split("\t");
    //System.out.print(token[0]+token[1]);
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
    //System.out.print(token[0]+token[1]);
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
    double average = (a+b)/2.0;
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


   while(fileRead != null){
    String [] comment = fileRead.split("\n");
    double[] i = wordsScoreCalculator(comment[0]);
    double a = i[0];
    double b = i[1];
    double[] arr = new double[4];
    arr[0]=a;
    arr[1]=b;
    arr[2]= counterNeg;
    arr[3]= -1;
    double average = (a+b)/2.0;

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

 public static void storeTrainSetComment(String fileFolderName){
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
    double average = (a+b)/2.0;
    double averageRounded = Double.parseDouble(decim.format(average));
    while(trainSetCommentMap.get(averageRounded) != null){
     averageRounded = averageRounded + 0.1;
     averageRounded = Math.round(averageRounded*100.00)/100.00;
    }

    trainSetCommentMap.put(averageRounded, arr);

    //System.out.println("TrainSet: " + arr[0]+" "+arr[1]+ " "+ arr[2]+" "+ average);
    counterTest++;
    //System.out.println(comment[0]);
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

 /*calculate score by reading every word of the input string and store into positiveCommentMap<counter, [pos][neg]> */
 public static double[] wordsScoreCalculator(String input){
  double positive = 0;
  double negative =0;
  double negativeDocCount = 901530;
  double positiveDocCount = 970901;
  double[] posNegScore = new double [2];
  String upperCaseInput = input.toUpperCase();
  String [] wordSplit = upperCaseInput.split("\\s");


  for(int i = 0; i<wordSplit.length; i++){

   if(positiveWordMap.get(wordSplit[i])!=null){
    if(stopWords.containsKey(wordSplit[i])){
     positive = positive+0.5;
    }
    if(positiveWordMap.get(wordSplit[i])!=null && negativeWordMap.get(wordSplit[i])!=null){
     positive = positive+1;
    }
    positive= positive+10;
   }
   if(negativeWordMap.get(wordSplit[i])!=null){
    if(stopWords.containsKey(wordSplit[i])){
     negative = negative + 0.5;
    }
    if(positiveWordMap.get(wordSplit[i])!=null && negativeWordMap.get(wordSplit[i])!=null){
     negative = negative+1;
    }
    negative = negative + 8;
   }
   if(positiveWordMap.get(wordSplit[i])==null || negativeWordMap.get(wordSplit[i])==null){
    continue;
   }


  }
  //System.out.println(positive+" " + negative);
  posNegScore[0] = positive;
  posNegScore[1] = negative;
  return posNegScore;

  //  double positive = 0.000000;
  //  double negative = 0.000000;
  //  double negativeDocCount = 901530;
  //  double positiveDocCount = 970901;
  //  double[] posNegScore = new double [2];
  //
  //  //input = "hello The World World Hello Hello the end is here alo hola";
  //
  //  String upperCaseInput = input.toUpperCase();
  //  String [] wordSplit = upperCaseInput.split("\\s");
  //  HashMap<String, Integer> tempHashMap = new HashMap<String, Integer>();
  //  double stringLength = input.length();
  //
  //  int trace = 0, counter = 0;
  //
  //  while(trace <wordSplit.length){
  //   counter =0;
  //   if(tempHashMap.containsKey(wordSplit[trace])){
  //    trace++;
  //    continue;
  //   }else{
  //    for(int i=0; i<wordSplit.length; i++){
  //
  //     if(wordSplit[trace].equals(wordSplit[i])){
  //      counter++;
  //     }
  //    }
  //    tempHashMap.put(wordSplit[trace], counter);
  //    trace++;
  //   }
  //  }
  //  double occur = 0;
  //
  //  for(String i : tempHashMap.keySet()){
  //   double answer = 0;
  //   if(positiveWordMap.get(i) != null){
  //    occur = tempHashMap.get(i);
  //    answer = ((occur/stringLength)*(Math.log(positiveDocCount)/positiveWordMap.get(i)));
  //    positive = positive + answer;
  //   }
  //   if(negativeWordMap.get(i) != null){
  //    occur = tempHashMap.get(i);
  //    answer = ((occur/stringLength)*(Math.log(negativeDocCount)/negativeWordMap.get(i)));
  //    negative = negative +answer;
  //   }
  //   if(positiveWordMap.get(i) == null && negativeWordMap.get(i)==null){
  //    continue;
  //
  //   }
  //
  //  }
  //  //System.out.println(positive+" " + negative);
  //  posNegScore[0] = positive;
  //  posNegScore[1] = negative;
  //  return posNegScore;
 }

 public static void calculateKnn(int i, double trainDouble){
  //System.out.println(trainDouble);
  if(trainDouble == 0){
   double [] arr= new double[4];
   arr[0] = trainSetCommentMap.get(trainDouble)[0];
   arr[1] = trainSetCommentMap.get(trainDouble)[1];
   arr[2] = trainSetCommentMap.get(trainDouble)[2];
   arr[3] = 1;
   trainSetCommentMap.put(trainDouble, arr);
   return;
  }
  double tempTrainDouble = trainDouble;
  HashMap<Double, double[]> tempMap = new HashMap<Double,double[]>();
  int counter=0;
  int iInc = Math.round(i/2);
  int iDec = i-iInc;
  if(commentsMap.containsKey(trainDouble)==false){
   while(commentsMap.containsKey(tempTrainDouble)==false && ((TreeMap<Double, double[]>) commentsMap).ceilingKey(trainDouble)!=null){
    tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).ceilingKey(trainDouble);
    tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
    //counter++;
   }
   if(((TreeMap<Double, double[]>) commentsMap).ceilingKey(trainDouble)==null){
    while(counter<iDec){
     tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble);
     counter++;
    }
   }
  }
  double tempTrainDouble1= tempTrainDouble;
  if(commentsMap.containsKey(tempTrainDouble)){
   //System.out.println("Test: " + trainDouble + ": " + trainSetCommentMap.get(trainDouble));
   int a =0;



   while(a<iDec){
    if(commentsMap.containsKey(tempTrainDouble)==false && ((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble) != null){
     //while(commentsMap.containsKey(tempTrainDouble)==false){

     tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble);
     tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));

     //}
    }

    tempMap.put(tempTrainDouble, commentsMap.get(tempTrainDouble));
    tempTrainDouble = tempTrainDouble - 0.01;
    tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
    //System.out.println("tempTrainDouble"+ tempTrainDouble);

    if(((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble)==null){
     while(commentsMap.containsKey(tempTrainDouble)==false && ((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble) != null){
      tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble);
      tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
     }
    }
    a++;


   }
   //System.out.println("\n"+commentsMap.size()+" "+ i);
   tempTrainDouble = tempTrainDouble1;
   while(tempMap.size()<i){
    if(commentsMap.containsKey(tempTrainDouble)==false && ((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble) != null){
     while(commentsMap.containsKey(tempTrainDouble)==false && ((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble) != null){
      tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble);
      tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
     }
    }
    //tempTrainDouble = tempTrainDouble + 0.1;

    tempMap.put(tempTrainDouble, commentsMap.get(tempTrainDouble));
    tempTrainDouble = tempTrainDouble +0.01; 
    tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
    // System.out.println("tempTrainDouble"+ tempTrainDouble);

    if(((TreeMap<Double, double[]>) commentsMap).ceilingKey(tempTrainDouble)==null){
     tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble);
     while(tempMap.size()<i){
      if(commentsMap.containsKey(tempTrainDouble)==false){
       while(commentsMap.containsKey(tempTrainDouble)==false){
        tempTrainDouble = ((TreeMap<Double, double[]>) commentsMap).floorKey(tempTrainDouble);
        tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
       }
      }
      //tempTrainDouble = tempTrainDouble + 0.1;

      tempMap.put(tempTrainDouble, commentsMap.get(tempTrainDouble));
      tempTrainDouble = tempTrainDouble - 0.01;
      tempTrainDouble = Double.parseDouble(decim.format(tempTrainDouble));
      //System.out.println("tempTrainDouble"+ tempTrainDouble);
     }
    }
   }  
  }
  int posCount =0;
  int negCount =0;
  for(double key : tempMap.keySet()){
   // System.out.println(key+ ": "+ tempMap.get(key)[0]+ " " + tempMap.get(key)[1] + " " + tempMap.get(key)[2] + " " + tempMap.get(key)[3]);
   if(tempMap.get(key)[3] == -1){
    negCount++;
   }
   if(tempMap.get(key)[3] == 1){
    posCount++;
   }
  }
  //  System.out.println(tempMap.keySet());
  if(posCount==negCount){
   System.out.println(negCount+ " " + posCount);
  }
  if(posCount>negCount){
   double [] arr= new double[4];
   arr[0] = trainSetCommentMap.get(trainDouble)[0];
   arr[1] = trainSetCommentMap.get(trainDouble)[1];
   arr[2] = trainSetCommentMap.get(trainDouble)[2];
   arr[3] = 1;
   trainSetCommentMap.put(trainDouble, arr);
  }
  else if(posCount<negCount){
   double [] arr= new double[4];
   arr[0] = trainSetCommentMap.get(trainDouble)[0];
   arr[1] = trainSetCommentMap.get(trainDouble)[1];
   arr[2] = trainSetCommentMap.get(trainDouble)[2];
   arr[3] = -1;
   trainSetCommentMap.put(trainDouble, arr);
  }
  double disNumb = trainSetCommentMap.get(trainDouble)[3];
  int disInt = (int) disNumb;
  //System.out.println(disInt);


 }

}
