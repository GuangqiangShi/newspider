package webspider;

import java.util.HashSet;
import java.util.Set;

public class LinkDB {

	public Set<String> visitedUrl = new HashSet<String>();

	public Queue<String> unVisitedUrl = new Queue<String>();

	public Queue<String> getUnVisitedUrl() {
		return unVisitedUrl;
	}

	public void addVisitedUrl(String url) {
		visitedUrl.add(url);
	}

	public void removeVisitedUrl(String url) {
		visitedUrl.remove(url);
	}

	public String unVisitedUrlDeQueue() {
		return unVisitedUrl.deQueue();
	}

	public boolean addUnvisitedUrl(String url) {
		if (url != null && !url.trim().equals("") && !visitedUrl.contains(url) && !unVisitedUrl.contians(url)) {
			unVisitedUrl.enQueue(url);
			return true;
		}
		return false;

	}

	public int getVisitedUrlNum() {
		return visitedUrl.size();
	}

	public boolean unVisitedUrlsEmpty() {
		return unVisitedUrl.empty();
	}
}