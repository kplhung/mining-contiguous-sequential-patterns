package spade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Spade {

	public static void main(String[] args) {
		spade("src/reviews_sample.txt", 100);
		System.out.println("done");
	}
	
	static boolean spade(String filePath, int absoluteSupport) {
		Map<String, Integer> frequentSequences = mineContiguousSequentialPatterns(filePath, absoluteSupport);
		try {
			PrintWriter w = new PrintWriter("patterns.txt", "UTF-8");
			for (Map.Entry<String, Integer> entry : frequentSequences.entrySet()) {
				w.println(entry.getValue() + ":" + entry.getKey());
			}	
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	static HashMap<String, Integer> mineContiguousSequentialPatterns(String filePath, int support) {
		HashMap<String, Integer> sequenceCounts = new HashMap<String, Integer>();
		HashMap<String, Integer> frequentSequenceCounts = new HashMap<String, Integer>();
		HashMap<Integer, Map<Integer, String>> sequenceDatabase = new HashMap<Integer, Map<Integer, String>>();
		File file = new File(filePath);
		BufferedReader reader = null;
		
		// generate length-1 sequence counts
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String str;
		Set<String> seen = new HashSet<String>();
		try {
			String[] terms;
			while ((str = reader.readLine()) != null) {
				terms = str.split(" ");
				for (int i = 0; i < terms.length; i++) {
					if (!seen.contains(terms[i])) {
						sequenceCounts.put(terms[i], sequenceCounts.getOrDefault(terms[i], 0) + 1);
					}
					seen.add(terms[i]);
				}
				seen = new HashSet<String>();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// eliminate non-frequent length-1 sequences
		prune(frequentSequenceCounts, sequenceCounts, support);
		
		// generate length-1 sequence database
		try {
			reader = new BufferedReader(new FileReader(file));
			String[] terms;
			int row = 0;
			while ((str = reader.readLine()) != null) {
				terms = str.split(" ");
				HashMap<Integer, String> termIndex = new HashMap<Integer, String>();
				for (int i = 0; i < terms.length; i++) {
					if (frequentSequenceCounts.containsKey(terms[i])) {
						termIndex.put(i, terms[i]);
					}
				}
				sequenceDatabase.put(row, termIndex);
				row++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// generate length-2 candidate sequences
		sequenceCounts = new HashMap<String, Integer>();
		seen = new HashSet<String>();
		String concatenatedStr;
		for (Integer rowNum : sequenceDatabase.keySet()) {
			HashMap<Integer, String> row = (HashMap<Integer, String>) sequenceDatabase.get(rowNum);
			for (Map.Entry<Integer, String> entry : row.entrySet()) {
				if (row.containsKey(entry.getKey() + 1)) {
					// consecutive terms, store as candidate
					concatenatedStr = entry.getValue() + ";" + row.get(entry.getKey() + 1);
					if (!seen.contains(concatenatedStr)) {
						sequenceCounts.put(concatenatedStr, sequenceCounts.getOrDefault(concatenatedStr, 0) + 1);
					}
					seen.add(concatenatedStr);
				}
			}
			seen = new HashSet<String>();
		}
		
		// eliminate non-frequent sequences
		prune(frequentSequenceCounts, sequenceCounts, support);
		
		return frequentSequenceCounts;
	}
	
	static HashMap<String, Integer> prune(HashMap<String, Integer> frequentSequenceCounts, Map<String, Integer> sequenceCounts, int support) {
		for (Map.Entry<String, Integer> entry : sequenceCounts.entrySet()) {
			if (entry.getValue() >= support) {
				frequentSequenceCounts.put(entry.getKey(), entry.getValue());
			}
		}
		return frequentSequenceCounts;
	}
}
