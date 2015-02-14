import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;


public class viterbiAlg {
	static HashMap<String,Integer> tagSet = new HashMap<String,Integer>();
	static Set<String> wordSet = new HashSet<String>();
	static ArrayList<String> tags = new ArrayList<String>();
	static HashMap<String, Double> bigramTagProb = new HashMap<String, Double>();
	static HashMap<String, Integer> tagtagCount = new HashMap<String, Integer>();
	static HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
	static HashMap<String, Integer> wordTagPairCount = new HashMap<String,Integer>();
	static HashMap<String, Double> wordTagProb = new HashMap<String, Double>();
	static HashMap<String, Integer> maxUnkWordTag = new HashMap<String, Integer>();
	static double unkWordTagProb;
	static Set<String> unknownWords;
	static int choice;
	public static void main(String[] args) throws IOException{
		int i = 1;
		train(i);
		test();
		System.out.println("Enter 1 to continue...");
		Scanner in = new Scanner(System.in);
		int n = in.nextInt();
		while(n!=1){
			System.out.println("Do you want to continue? 1. Yes 2.No");
			n = in.nextInt();
		}
		if(n == 1){
		System.out.println("Application....");
		application();
		}

	}
	public static void train(int g) throws IOException{
		BufferedReader newBR = new BufferedReader(new FileReader("training.txt"));
		String line = "";
		while((line = newBR.readLine())!=null){
			line.replace("\t", "");
			line = "start/start "+ line+ " end/end";
			String[] wordTags = line.split("\\s+");
			for(String wordTag : wordTags){
				String[] wt = wordTag.split("/");
				if(!wordTag.contains("/") || wt[0].equals("") || wt[1].equals("")){
					continue;
				}
				Character c= new Character(wt[0].charAt(0));
				if(c.isUpperCase(wt[0].charAt(0))){
					if(maxUnkWordTag.containsKey(wt[1])){
						maxUnkWordTag.put(wt[1], maxUnkWordTag.get(wt[1])+1);
					}else{
						maxUnkWordTag.put(wt[1],1);
					}
				}
				if(tagSet.containsKey(wt[1])){
					tagSet.put(wt[1], tagSet.get(wt[1])+1);
				}else{
					tagSet.put(wt[1],  1);
				}
				wordSet.add(wt[0]);
				if(tagCount.containsKey(wt[1])){
					tagCount.put(wt[1], tagCount.get(wt[1])+1);
				}else{
					tagCount.put(wt[1], 1);
				}
				if(wordTagPairCount.containsKey(wt[0]+" "+wt[1])){
					wordTagPairCount.put(wt[0]+" "+wt[1], wordTagPairCount.get(wt[0]+" "+wt[1])+1);
				}else{
					wordTagPairCount.put(wt[0]+" "+wt[1], 1);
				}

			}

			for(int i = 0 ; i < wordTags.length-1; i++){
				String[] wt1 = wordTags[i].split("/");
				String[] wt2 = wordTags[i+1].split("/");
				if(tagtagCount.containsKey(wt1[1]+" "+wt2[1])){
					tagtagCount.put(wt1[1]+" "+wt2[1], tagtagCount.get(wt1[1]+" "+wt2[1])+1);

				}else{
					tagtagCount.put(wt1[1]+" "+wt2[1], 1);
				}
			}
		}
		newBR.close();
		for(String tag1: tagSet.keySet()){
			for(String tag2: tagSet.keySet()){
				int num = 0;
				if(tagtagCount.containsKey(tag1+" "+tag2)){
					num = tagtagCount.get(tag1+" "+tag2);
				}
				int den = tagSet.get(tag1);
				double prob = num*1.0/den;
				bigramTagProb.put(tag1+" "+tag2, prob);
			}
		}
		for(String wordTag: wordTagPairCount.keySet()){
			int num = wordTagPairCount.get(wordTag);
			String[] wt = wordTag.split(" ");
			int den = tagSet.get(wt[1]);
			double prob = num*1.0/den;
			wordTagProb.put(wordTag, prob);
		}
		tags.addAll(tagSet.keySet());
		tags.remove("start");
		tags.remove("end");
		unkWordTagProb = 1.0/tags.size();
		System.out.println("Training done.");
		System.out.println("1. Test on the test set.");
		System.out.println("2. Input your own sentence.");
		Scanner in = new Scanner(System.in);
		System.out.println("Enter your choice...\n");
		choice = in.nextInt();
		while(choice!=1 && choice!=2){
			System.out.println("Please enter a valid choice..");
			System.out.println("Enter your choice...\n");
			choice = in.nextInt();	
		}
	}
	public static void test() throws IOException{
		if(choice == 1){
			int lineNo = 0;
			BufferedReader newBR = new BufferedReader(new FileReader("testing.txt"));
			String line ="";
			int correct = 0;
			int total = 0;
			int unk=0;
			int unkWordsize = 0;
			while((line = newBR.readLine())!=null){
				lineNo++;
				System.out.println(lineNo+"%");
				line.replace("\t", "");
				String[] words = line.split("\\s+");
				unknownWords = new HashSet<String>();
				ArrayList<String> tagSeq = new ArrayList<String>();
				ArrayList<String> wordList = new ArrayList<String>();
				for(String word:words){
					String[] wt = word.split("/");
					if(!word.contains("/") || wt[0].equals("") || wt[1].equals("")){
						continue;
					}
					if(!wordSet.contains(wt[0])){
						unknownWords.add(wt[0]);
					}
					tagSeq.add(wt[1]);
					wordList.add(wt[0]);
				}
				//System.out.println(unknownWords);
				unkWordsize+=unknownWords.size();

				LinkedList<String> foundTags = viterbiDecode(wordList);
				java.util.Iterator<String> it = foundTags.iterator();
				int word=0;
				for(String tag: tagSeq){
					String w = wordList.get(word);
					String foundTag = it.next();
					if(unknownWords.contains(w)){
						Character c= new Character(w.charAt(0));
						if(c.isUpperCase(w.charAt(0))){
							foundTag = "np";
						}
					}
					if(tag.equalsIgnoreCase(foundTag)){
						correct++;
						if(unknownWords.contains(w)){
							unk++;
						}
					}
					total++;
					word++;
				}
				
			}
			newBR.close();
			System.out.println("Overall Accuracy = "+correct*100.0/total+"%");
			System.out.println("Total number of unique tags = "+tags.size());
			System.out.println("Correctly classified unknowns = "+unk);
			System.out.println("Total unknown words = "+unkWordsize);
			System.out.println("Unknown word accuracy = "+unk*100.0/unkWordsize+"%");
			System.out.println("Unk word accuracy increased from 1/"+tags.size()+"to 1/"+100.0/(unk*100.0/unkWordsize));
			System.out.println("Total tags classified correctly = "+correct);
			System.out.println("Total tags = "+total);
		}else{
			System.out.println("Enter a sentence: \nPlease follow the following convention.\nSample: I am an UTD student .");
			Scanner in = new Scanner(System.in);
			String line = in.nextLine();
			unknownWords = new HashSet<String>();
			String[] words = line.split("\\s+");
			ArrayList<String> wordList = new ArrayList<String>();
			for(String word:words){
				wordList.add(word);
				if(!wordSet.contains(word)){
					unknownWords.add(word);
				}
			}
			if(!wordList.get(wordList.size()-1).equals(".")){
				wordList.add(".");
			}
			LinkedList<String> foundTags = new LinkedList<String>();
			foundTags = viterbiDecode(wordList);
			StringBuffer sb = new StringBuffer("");
			java.util.Iterator<String> it = foundTags.iterator();
			for(String word: wordList){
				sb.append(word+"/"+it.next()+" ");
			}
			System.out.println("Tag Sequence. Please review report for meaning of each tag.");
			System.out.println(sb);
		}

	}
	public static LinkedList<String> viterbiDecode(ArrayList<String> words){
		String first = words.get(0);
		int numTags = tagSet.size();
		int max=-1;
		double[][] viterbi = new double[numTags][words.size()+1];
		int[][] back_pointer = new int[numTags][words.size()+1];
		viterbi[0][0] = 1.0;
		double maxProb =0.0;
		double multProb= 0.0;
		for(int i = 0; i< tags.size(); i++){
			if(tags.get(i).equals("start")){
				continue;
			}
			double transProb = bigramTagProb.get("start "+tags.get(i));
			double wordProb = 0.0;
			if(unknownWords.contains(first)){
				wordProb = unkWordTagProb;
			}else{
				if(wordTagProb.containsKey(first+ " "+tags.get(i))){
					wordProb = wordTagProb.get(first+ " "+tags.get(i));
				}else{
					wordProb = 0.0;
				}
			}
			multProb = transProb * wordProb;
			viterbi[i][1] = multProb;
			back_pointer[i][1] = 0;
		}
		for(int i = 1; i <words.size();i++){
			String word = words.get(i);
			for(int k = 0; k<tags.size();k++){
				double wordProb = 0.0;
				multProb = 0.0;
				maxProb = 0.0;
				for(int j = 0;j<tags.size();j++){
					double transProb = bigramTagProb.get(tags.get(j)+" "+tags.get(k));
					if(unknownWords.contains(word)){
						wordProb = unkWordTagProb;
					}else{
						if(wordTagProb.containsKey(word+" "+tags.get(k))){
							wordProb = wordTagProb.get(word+" "+tags.get(k));
						}else{
							wordProb = 0.0;
						}
					}
					multProb = transProb*wordProb*viterbi[j][i];
					if(multProb>=maxProb){
						maxProb=multProb;
						max=j;
					}
				}
				viterbi[k][i+1]=maxProb;
				back_pointer[k][i+1] = max;
			}
		}
		/*	for(int i =0 ; i<tags.size();i++){
			System.out.println(viterbi[i][3]+" "+tags.get(i));
		}*/	
		maxProb = 0;
		max = -1;
		for(int k = 0; k<tags.size();k++){
			double transProb = bigramTagProb.get(tags.get(k)+" "+"end");
			multProb = viterbi[k][words.size()]*transProb;
			if(multProb>=maxProb){
				maxProb = multProb;
				max = k;
			}
		}
		LinkedList<String> foundTags = new LinkedList<String>();
		if(max ==-1){
			int u = 0 ;
		}
		foundTags.addFirst(tags.get(max));
		for(int i=words.size();i>1;i--){
			max = back_pointer[max][i];
			foundTags.addFirst(tags.get(max));
		}
		//System.out.println(foundTags);
		return foundTags;

	}
	public static ArrayList<String> getListofFiles(String filePath){
		File folder = new File(filePath);
		ArrayList<String> list = new ArrayList<String>();
		for (final File fileEntry : folder.listFiles()) {
			list.add(fileEntry.getName());
		}
		return list;
	}
	public static void application() throws IOException{
		System.out.println("\n\n\nDeceptive review identification using Parts of Speech of words in review...");
		String filePath = "";
		applicationTrain(filePath);
	}
	public static void applicationTrain(String filePath) throws IOException{
		System.out.println("Deceptive reviews....");
		final String decTrain = "deceptive//training//";
		ArrayList<String>traindecFiles = getListofFiles(filePath+decTrain);
		int noOfFiles=0;
		int totalAdj = 0;
		int totalPRP = 0;
		for(String file: traindecFiles){
			int noOfAdj = 0;
			int noOfPRP = 0;
			int noOfWords = 0;
			BufferedReader reader = new BufferedReader(new FileReader(filePath+decTrain+file));
			String alllines = "";
			while((alllines = reader.readLine())!=null){
				String[] lines = alllines.split("\\. +");
				for(String line: lines){
					unknownWords = new HashSet<String>();
					ArrayList<String> wordList = new ArrayList<String>();
					String[] words = line.split("\\s+");
					for(String word: words){
						if(!wordSet.contains(word)){
							unknownWords.add(word);
						}
						wordList.add(word);
					}
					wordList.add(".");
					noOfWords+=wordList.size();
					LinkedList<String> foundTags = viterbiDecode(wordList);
					java.util.Iterator<String> it = foundTags.iterator();
					while(it.hasNext()){
						String tag = it.next();
						if(tag.contains("jj")){
							noOfAdj++;
						}
						else if(tag.contains("pp")){
							noOfPRP++;
						}
						
					}
					totalAdj+=noOfAdj;
					totalPRP+=noOfPRP;
				}
			}
			reader.close();
			noOfFiles++;
		}
		System.out.println("No of adjectives (all tags containing jj) per file = "+totalAdj*1.0/noOfFiles);
		System.out.println("No of personal pronouns (all tags containing pp) per file = "+totalPRP*1.0/noOfFiles);
		System.out.println("");
		System.out.println("Truthful reviews...");
		final String truTrain = "truthful//training//";
		ArrayList<String> traintruFiles = getListofFiles(filePath+truTrain);
		noOfFiles=0;
		totalAdj = 0;
		totalPRP=0;
		for(String file: traintruFiles){
			int noOfAdj = 0;
			int noOfWords = 0;
			int noOfPRP = 0;
			BufferedReader reader = new BufferedReader(new FileReader(filePath+truTrain+file));
			String alllines = "";
			while((alllines = reader.readLine())!=null){
				String[] lines = alllines.split("\\. +");
				for(String line: lines){
					unknownWords = new HashSet<String>();
					ArrayList<String> wordList = new ArrayList<String>();
					String[] words = line.split("\\s+");
					for(String word: words){
						if(!wordSet.contains(word)){
							unknownWords.add(word);
						}
						wordList.add(word);
					}
					wordList.add(".");
					noOfWords+=wordList.size();
					LinkedList<String> foundTags = viterbiDecode(wordList);
					java.util.Iterator<String> it = foundTags.iterator();
					while(it.hasNext()){
						String tag = it.next();
						if(tag.contains("jj")){
							noOfAdj++;
						}
						else if(tag.contains("pp")){
							noOfPRP++;
						}
					}
					totalAdj+=noOfAdj;
					totalPRP+=noOfPRP;
				}
			}
			noOfFiles++;
			reader.close();

		}
		System.out.println("No of adjectives (all tags containing jj) per review = "+totalAdj*1.0/noOfFiles);
		System.out.println("No of personal pronouns (all tags containing pp) per review = "+totalPRP*1.0/noOfFiles);
		System.out.println("");
		System.out.println("These values for deceptive and truthful reviews can be used to classify. (say k-means).");
	}

}
