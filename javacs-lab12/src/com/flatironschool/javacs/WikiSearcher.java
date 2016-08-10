
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


public class WikiSearcher {

public static void main(String[] args) throws IOException {

                // make a JedisIndex
                Jedis jedis = JedisMaker.make();
                JedisIndex index = new JedisIndex(jedis);

                // search for the first term
                String term1 = "java";
                System.out.println("Query: " + term1);
                WikiSearch search1 = new WikiSearch();
		search1.completeSearch(term1, index);
                search1.print();
                search1.printIDF();

                // search for the second term
                String term2 = "programming";
                System.out.println("Query: " + term2);
                WikiSearch search2 = new WikiSearch();
		search2.completeSearch(term2, index);
                search2.print();
                search2.printIDF();

                // compute the intersection of the searches
                System.out.println("Query: " + term1 + " AND " + term2);
                WikiSearch intersection = search1.and(search2);
                intersection.print();
                intersection.printIDF();

                // compute the union of the searches
                System.out.println("Query: " + term1 + " OR " + term2);
                WikiSearch union = search1.or(search2);
                union.print();
                union.printIDF();

                // compute the difference of the searches
                System.out.println("Query: " + term2 + " MINUS " + term1);
                WikiSearch difference = search2.minus(search1);
		difference.print();
		difference.printIDF();

		System.out.println("Query: " + term1 + " MINUS " + term2);
		WikiSearch difference2 = search1.minus(search2);
		difference2.print();
		difference2.printIDF();

                // search for the
                String term3 = "the";
                System.out.println("Query: " + term3);
                WikiSearch search3 = new WikiSearch();
		search3.completeSearch(term3, index);
                search3.print();
                search3.printIDF();

		// search for "the AND computer"
		String term4 = "computer";
		WikiSearch search4 = new WikiSearch();
		search4.completeSearch(term4, index);
                System.out.println("Query: " + term3 + " AND " + term4);
		WikiSearch intersection2 = search3.and(search4);
		intersection2.print();
		intersection2.printIDF();

                // search for gymnastics
                String term5 = "gymnastics";
                System.out.println("Query: " + term5);
                WikiSearch search5 = new WikiSearch();
		search5.completeSearch(term5, index);
                search5.print();
                search5.printIDF();
}
}


