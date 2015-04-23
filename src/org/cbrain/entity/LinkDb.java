package org.cbrain.entity;

import java.util.HashSet;
import java.util.Set;

public class LinkDb {

	// 已访问的 url 集合
	private static Set<String> visitedUrl = new HashSet<String>();
	// 待访问的 url 集合
	private static Queue<String> unVisitedUrl = new Queue<String>();

	public static Queue<String> getUnVisitedUrl() {
		return unVisitedUrl;
	}

	public static void addVisitedUrl(String url) {
		visitedUrl.add(url);
	}

	public static void removeVisitedUrl(String url) {
		visitedUrl.remove(url);
	}

	public static String unVisitedUrlDeQueue() {
		return unVisitedUrl.deQueue();
	}

	// 保证每个 url 只被访问一次
	public static void addUnvisitedUrl(String url) {
		if (url != null && !url.trim().equals("") && !visitedUrl.contains(url)
				&& !unVisitedUrl.contians(url))
			unVisitedUrl.enQueue(url);
	}

	public static int getVisitedUrlNum() {
		return visitedUrl.size();
	}

	public static boolean unVisitedUrlsEmpty() {
		return unVisitedUrl.empty();
	}
	
	public static void clearUnVisitedUrl() {
		unVisitedUrl.resetQueue();
	}
	public static void clearVisitedUrl() {
		visitedUrl.clear();
	}


}
