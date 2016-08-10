package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;
import java.lang.Math;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> tfMap;
	private Map<String, Double> tfidfMap;

	Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {
		@Override
		public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
			Integer int1 = entry1.getValue();
			Integer int2 = entry2.getValue();

			if (int1 == int2) {
				return 0;
			}		
			else if (int1 < int2) {
				return 1;
			}
			else {
				return -1;
			}
		}
	};

	Comparator<Entry<String, Double>> idfComparator = new Comparator<Entry<String, Double>>() {
                @Override
                public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
                        Double val1 = entry1.getValue();
                        Double val2 = entry2.getValue();

                        if (val1 == val2) {
                                return 0;
                        }
                        else if (val1 < val2) {
                                return 1;
                        }
                        else {
                                return -1;
                        }
                }
        };


	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> tfMap) {
		this.tfMap = tfMap;
	}

	public WikiSearch(Map<String, Double> tfidfMap, String term) {
		this.tfidfMap = tfidfMap;
	}

	public WikiSearch(Map<String, Integer> tfMap, Map<String, Double> tfidfMap) {
		this.tfMap = tfMap;
		this.tfidfMap = tfidfMap;
	}
	
	public WikiSearch() {
		this.tfMap = new HashMap<String, Integer>();
		this.tfidfMap = new HashMap<String, Double>();
	}

	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = tfMap.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	public void print() {
		List<Entry<String, Integer>> entries = sort();
		if (entries.isEmpty()) {
			System.out.println("No relevant pages found");
		}
		else {
			for (Entry<String, Integer> entry: entries) {
				System.out.println(entry);
			}
		}
	}

	public void printIDF() {
		if (tfMap.entrySet().isEmpty()) {
			System.out.println("No relevant pages found");	
		}
		else {
			List<Entry<String, Double>> entries = sortIDF();
			for (Entry<String, Double> entry: entries) {
				if (entry.getValue() == 0) {
					System.out.println("Query not specific enough");	
					break;
				}
				else {
					System.out.println(entry);
				}
			}
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> newMap = new HashMap<String, Integer>();
		newMap.putAll(tfMap);

		for (String url: that.tfMap.keySet()) {
			if (newMap.containsKey(url)) {
				Integer orRelevance = that.tfMap.get(url) + this.tfMap.get(url);
				newMap.put(url, orRelevance);
			}
			else {
				newMap.put(url, that.tfMap.get(url));
			}
		}

		Map<String, Double> newMapIDF = new HashMap<String, Double>();
		newMapIDF.putAll(tfidfMap);

                for (String url: that.tfidfMap.keySet()) {
                        if (newMapIDF.containsKey(url)) {
                                Double orRelevance = that.tfidfMap.get(url) + this.tfidfMap.get(url);
                                newMapIDF.put(url, orRelevance);
                        }
                        else {
                                newMapIDF.put(url, that.tfidfMap.get(url));
                        }
                }
		
		WikiSearch orSearch = new WikiSearch(newMap, newMapIDF);
		return orSearch;
	}

	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> newMap = new HashMap<String, Integer>();

                for (String url: this.tfMap.keySet()) {
                        if (that.tfMap.containsKey(url)) {
				Integer thisCount = tfMap.get(url);
				Integer thatCount = that.tfMap.get(url);
                                Integer andRelevance = thisCount + thatCount;
				newMap.put(url, andRelevance);
                        }
                }

		Map<String, Double> newMapIDF = new HashMap<String, Double>();

		for (String url: this.tfidfMap.keySet()) {
			if (that.tfidfMap.containsKey(url)) {
				Double thisRelevance = this.tfidfMap.get(url);
				Double thatRelevance = that.tfidfMap.get(url);
				//System.out.println("thisRelevance = " + thisRelevance);
				//System.out.println("thatRelevance = " + thatRelevance);
				Double andRelevance = thisRelevance + thatRelevance;
				newMapIDF.put(url, andRelevance);
			}
		}         

	        WikiSearch andSearch = new WikiSearch(newMap, newMapIDF);
                return andSearch;
	}
	
	//public static WikiSearch and(WikiSearch this

	/**
	 * Computes the difference of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> newMap = new HashMap<String, Integer>();
		newMap.putAll(tfMap);

		for (String url: that.tfMap.keySet()) {
			if (newMap.containsKey(url)) {
				newMap.remove(url);
			}
		}
		
		Map<String, Double> newMapIDF = new HashMap<String, Double>();
		newMapIDF.putAll(tfidfMap);
		
		for (String url: that.tfidfMap.keySet()) {
			if (newMapIDF.containsKey(url)) {
				newMapIDF.remove(url);
			}
		}

		WikiSearch minusSearch = new WikiSearch(newMap, newMapIDF);
                return minusSearch;
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
		List<Entry<String, Integer>> entries = new LinkedList<Entry<String, Integer>>();
		entries.addAll(tfMap.entrySet());
		Collections.sort(entries, comparator);
		return entries;
	}

	public List<Entry<String, Double>> sortIDF() {
		List<Entry<String, Double>> entries = new LinkedList<Entry<String, Double>>();
		entries.addAll(tfidfMap.entrySet());
		Collections.sort(entries, idfComparator);
		return entries;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static Map<String, Double> searchIDF(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		Map<String, Double> idfMap = new HashMap<String, Double>();

		double idfBase = (double) index.getNumPages()/index.getURLs(term).size();
		double idf = Math.log10(idfBase);

		Set<String> URLs = index.getURLs(term);
		for (String url: URLs) {
			int tf = map.get(url);
			double tfidf = tf * idf;
			idfMap.put(url, tfidf);
		}
		return idfMap;
	}

	public static Map<String, Integer> searchTF(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return map;
	}

	public void completeSearch(String term, JedisIndex index) {
		tfMap = searchTF(term, index);
		tfidfMap = searchIDF(term, index);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
	}
}
