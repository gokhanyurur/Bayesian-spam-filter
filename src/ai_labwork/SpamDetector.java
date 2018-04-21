package ai_labwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/*
 * spam detector using Bayesian Algorithm
 * 
 * @author Gokhan Yurur IFU-5
 *
 */
public class SpamDetector {

	int spamC = 0;
	int hamC = 0;
	static Map<String, WordNode> map = new HashMap<String, WordNode>();
	List<String> commonWords = new ArrayList<String>();
	
	double spamicity = 0.6f;
	
	static int detectedSpam = 0;
	static int detectedHam = 0;	
	static int falsePositive = 0;
	static int trueNegative = 0;
	
	static int totalLexemes = 0;
	
	int lexemesN = 4;
	
	public void train() throws Exception {
		
		//read common words
		BufferedReader br = new BufferedReader( new FileReader( new File("mailFiles/common-words.txt")));
		String line = br.readLine();
		while(line != null) {
			commonWords.add(line);
			line = br.readLine();
		}
		
		//read spam files from directory one by one
		File locSpm = new File("mailFiles/spamTraining");
	 	File[] listofSpmFiles = locSpm.listFiles();
	 	
	 	for(File f: listofSpmFiles) {
	 		if(f.isFile()) {
	 			br = new BufferedReader( new FileReader(f));
	 			line = br.readLine();
	 			while(line != null) {
	 				line = line.toLowerCase();
	 				String[] words = line.split(" ");
	 				for( String s: words ) {
	 					if(s.length() > 3 && !commonWords.contains(s)) {
	 						spamC++;
	 						if( map.containsKey(s)) {
	 							map.get(s).spamCount++;
	 						} else {
	 							map.put(s, new WordNode( 1, 0));
	 						}
	 					}
	 				}
	 				line = br.readLine();
	 			}
	 		}
	 	}

		//read ham files from directory one by one
		File locHm = new File("mailFiles/hamTraining");
		File[] listofHmFiles = locHm.listFiles();
		
	 	for(File f: listofHmFiles) {
	 		if(f.isFile()) {
	 			br = new BufferedReader( new FileReader(f));
	 			line = br.readLine();
	 			while(line != null) {
	 				line = line.toLowerCase();
	 				String[] words = line.split(" ");
	 				for( String s: words ) {
	 					if(s.length() > 3 && !commonWords.contains(s)) {
	 						hamC++;
	 						if( map.containsKey(s)) {
	 							map.get(s).hamCount++;
	 						} else {
	 							map.put(s, new WordNode( 0, 1));
	 						}
	 					}
	 				}
	 				line = br.readLine();
	 			}
	 		}
	 		
	 	}
	 	
		//calculate weights of words
		Set<String> keys = map.keySet();
		for( String key: keys ) {
			WordNode node = map.get(key);
			double res = ((node.spamCount)/(double)(spamC))/(double)(((node.spamCount)/(double)(spamC)) + (node.hamCount)/(double)(hamC));
			//System.out.println(res);
			node.probability = res;
		}
		
		br.close();
	}
		
	public void detect(File f) throws IOException {
		//read file and put it so String 'fullText'
		String fullText ="";
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while(line != null) {
			fullText+=line;
			line = br.readLine();
		}
		
		boolean result = false;
		fullText = fullText.toLowerCase();
		String[] words = fullText.split(" ");
		TreeMap<Double, List<Double>> interestMap = new TreeMap<Double, List<Double>>(Collections.reverseOrder());
		for( String w: words ) {
			if(w.length()> 3 && !commonWords.contains(w)) {
				double i = 0.5;
				double p = 0.5; //spamicity for unseen lexemes
				if(map.containsKey(w)) {
					p = map.get(w).probability;					
				} 
				i = Math.abs(i - p);
				if( !interestMap.containsKey(i) ) {
					List<Double> values = new ArrayList<Double>();
					values.add(p);						
					interestMap.put(i, values);
				} else {
					interestMap.get(i).add(p);
				}
			}
		}
		totalLexemes+=interestMap.size();		
		List<Double> probabilities = new ArrayList<Double>();
		int count = 0;
		Set<Double> keySet = interestMap.keySet();
		for( Double key: keySet ) {
			List<Double> weightList = interestMap.get(key);
			for(Double x: weightList) {
				count++;
				probabilities.add(x);
				if(count == lexemesN) {
					break;
				}
			}
			if(count == lexemesN) {
				break;
			}
		}
				
		double res = 1;
		double numerator = 1;
		double denominator = 1;
		for( Double d: probabilities ) {
			numerator = numerator * d;
			denominator = denominator * (1-d);
		}
		res = numerator/(double)(numerator +denominator);
		if(res >= spamicity) {
			result = true;
		}
		
		if( result ) {
			detectedSpam++;		
			System.out.println(f.getName()+" is SPAM ");
		} 
		else{
			detectedHam++;			
			System.out.println(f.getName()+" is not a spam ");
		}
	
	}
		
	public void movieToSpam(File f) throws IOException {
		//read file put it into String 'fullText'
		String fullText ="";
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = br.readLine();
		while(line != null) {
			fullText+=line;
			line = br.readLine();
		}
		
		fullText = fullText.toLowerCase();
		String[] sArr = fullText.split(" ");
		for( String x: sArr ) {
			if(x.length()> 3 && !commonWords.contains(x)) {
				spamC++;
				if( map.containsKey(x)) {
					map.get(x).spamCount++;
				} else {
					map.put(x, new WordNode( 1, 0));
				}
			}
		}
		
		Set<String> keys = map.keySet();
		for( String key: keys ) {
			WordNode node = map.get(key);
			double res = ((node.spamCount)/(double)(spamC))/(double)(((node.spamCount)/(double)(spamC)) + (node.hamCount)/(double)(hamC));
			node.probability = res;
		}
	}
	
	public static void main(String[] args) throws Exception {
		SpamDetector spamDetector = new SpamDetector();
		spamDetector.train();
		
		
		//spamDetector.detect(new File("mailFiles/ham/2608_ne_spam.txt"));
		//spamDetector.detect(new File("mailFiles/spam/2576_spam.txt"));
		
		//spamDetector.movieToSpam(new File("mailFiles/spam/2576_spam.txt"));	
		//spamDetector.detect(new File("mailFiles/spam/2576_spam.txt"));
		
		String path = "mailFiles/spam";
		
		File locFiles = new File(path);
	 	File[] listofFiles = locFiles.listFiles();
	 	
	 	for(File f: listofFiles) {
	 		spamDetector.detect(f);
	 	}
	 	
	 	String type = path.split("/")[1];
	 	
	 	int total = (detectedHam+detectedSpam);
	 	System.out.println("\nTotal files : "+total);
	 	System.out.println("Total detected ham files : "+detectedHam);
	 	System.out.println("Total detected spam files : "+detectedSpam);
	 	
	 	if(type.equals("ham")) {
	 		float percentHam = (detectedHam * 100.0f) / total;
		 	
		 	System.out.println("Ratio of correctness : "+percentHam);
		 	System.out.println("\nTrue negative : "+detectedSpam);
		 	
		 	
	 	}
	 	else if(type.equals("spam")) {
	 		float percentSpam = (detectedSpam * 100.0f) / total;
		 	
		 	System.out.println("Ratio of correctness : "+percentSpam);
		 	System.out.println("\nFalse positive : "+detectedHam);
	 	}
		
	}
}
