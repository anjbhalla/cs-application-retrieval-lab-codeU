package com.flatironschool.javacs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
        // keeps track of where we started
        private final String source;

        // the index where the results go
        private JedisIndex index;

        // queue of URLs to be indexed
        private Queue<String> queue = new LinkedList<String>();

        // fetcher used to get pages from Wikipedia
        final static WikiFetcher wf = new WikiFetcher();

        /**
         * Constructor.
         *
         * @param source
         * @param index
         */
	public WikiCrawler(String source, JedisIndex index) {
                this.source = source;
                this.index = index;
                queue.offer(source);
        }

        /**
         * Returns the number of URLs in the queue.
         *
         * @return
         */
        public int queueSize() {
                return queue.size();
        }

        /**
         * Gets a URL from the queue and indexes it.
         * @param b
         *
         * @return Number of pages indexed.
         * @throws IOException
         */
	public String crawl(boolean testing) throws IOException {
        // FILL THIS IN!
                if (queue.isEmpty()) {
                        return null;
                }
                // if queue isn't empty, get next url
                String url = queue.poll();
		System.out.println("Crawling " + url);

                // unless testing==true, url's aren't re-indexed
                if (testing==false && index.isIndexed(url)) {
                        return null;
                }

                Elements paragraphs;
                if (testing) {
                        //takes from file, not web
                        paragraphs = wf.readWikipedia(url);
                }
                else {
                        //takes from web
                        paragraphs = wf.fetchWikipedia(url);
                }
                index.indexPage(url, paragraphs);
		System.out.println("Index called in crawler\n");
                queueInternalLinks(paragraphs);
                return url;
        }

	 /**
         * Parses paragraphs and adds internal links to the queue.
         *
         * @param paragraphs
         */
        // NOTE: absence of access level modifier means package-level
        void queueInternalLinks(Elements paragraphs) {
        // FILL THIS IN!
                for (Element paragraph: paragraphs) {
                        Elements elts = paragraph.select("a[href]");
                        for (Element elt: elts) {
                                String relURL = elt.attr("href");
                                if (relURL.startsWith("/wiki/")) {
                                        String absURL = "https://en.wikipedia.org" + relURL;
                                        queue.offer(absURL);
                                }
                        }
                }
        }

	public static void main(String[] args) throws IOException {

                // make a WikiCrawler
                Jedis jedis = JedisMaker.make();
                JedisIndex index = new JedisIndex(jedis);
                String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";
                WikiCrawler wc = new WikiCrawler(source, index);

                // for testing purposes, load up the queue
                Elements paragraphs = wf.fetchWikipedia(source);
                wc.queueInternalLinks(paragraphs);

                // loop until we index a new page
                String res;
                do {
                        res = wc.crawl(false);

            // REMOVE THIS BREAK STATEMENT WHEN crawl() IS WORKING
                //break;
                } while (res == null);

                Map<String, Integer> map = index.getCounts("the");
                for (Entry<String, Integer> entry: map.entrySet()) {
                        System.out.println(entry);
                }
        }
}
