package webspider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Crawler {

	public static int x = 0;
	public static ArrayList<String> http = new ArrayList<String>();
	public static LinkDB linkQueue = new LinkDB();//两个队列要成对出现
	public static LinkDB tempQueue = new LinkDB();
	public static File webFile = new File("http/web.txt");
	public static File urlFile = new File("http/url.txt");
	public static File queueFile= new File("http/queue.txt");
	public static File pointFile = new File("http/point.txt");

	public static void crawling(LinkDB linkQueue, String http) throws IOException {

		HtmlParserTool htmlParserTool = new HtmlParserTool();

		LinkFilter filter = new LinkFilter() {
			public boolean accept(String url) {
				if (url.startsWith(http))
					return true;
				else
					return false;
			}
		};

		while (!linkQueue.unVisitedUrlsEmpty() && linkQueue.getVisitedUrlNum() <= 1000000) {

			String visitUrl = (String) linkQueue.unVisitedUrlDeQueue();
			String tempUrl = visitUrl;
			visitUrl = visitUrl.substring(visitUrl.indexOf(":") + 1);

			if (visitUrl == null)
				continue;

			String url = visitUrl;
			HttpClient httpClient = new HttpClient();
			httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
			GetMethod getMethod = new GetMethod(url.replaceAll(" ", "%20"));
			getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
			getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());

			try {
				int statusCode = httpClient.executeMethod(getMethod);
				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + getMethod.getStatusLine());
				}
				String Type = getMethod.getResponseHeader("Content-Type").getValue();
				Type = Type.substring(0, Type.indexOf("/"));

				DownLoadFile downLoader = new DownLoadFile();
				downLoader.downloadFile("spider/", tempUrl);

				writePoint(tempUrl);
				System.out.println(tempUrl);
				
				tempQueue.addVisitedUrl(visitUrl);				
				linkQueue.addVisitedUrl(tempUrl);
		
				if (Type.equals("text")) {
					Set<String> links = htmlParserTool.extracLinks(visitUrl, filter);
					for (String link : links) {
						if (tempQueue.addUnvisitedUrl(link)) {
							x++;
							linkQueue.addUnvisitedUrl(x + ":" + link);
							writeUrl(link);
							writeQueue(x, link);
						}
					}
				}
			} catch (HttpException e) {
				System.out.println("Please check your provided http address!");
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				getMethod.releaseConnection();
			}
		}
	}

	public static void main(String[] args) throws IOException {

//		timer();
		if (webFile.exists()) {
			initHttp();
			if (!queueFile.exists()) {
				for (int i = 0; i < http.size(); i++)
					unit(http.get(i));
			} else {
				initQueue();
				for (int i = 0; i < http.size(); i++)
					unit(http.get(i));
			}
		}
	}

	private static void initHttp() throws IOException {
		
		FileReader isr = new FileReader(webFile);
		BufferedReader br = new BufferedReader(isr);
		String str = br.readLine();
		while (str != null) {
			http.add(str);
			str = br.readLine();
		}
		br.close();
		isr.close();
	}

	private static void initQueue() throws IOException {

		FileReader isr = new FileReader(pointFile);
		BufferedReader br = new BufferedReader(isr);
		String str1 = br.readLine();
		br.close();

		isr = new FileReader(queueFile);
		br = new BufferedReader(isr);
		String str2 = br.readLine();
		while (!str2.equals(str1)) {
			tempQueue.addUnvisitedUrl(str2.substring(str2.indexOf(":")+1));
			linkQueue.addVisitedUrl(str2);			
			str2 = br.readLine();
		}
		linkQueue.addVisitedUrl(str2);
		str2 = br.readLine();
		while (str2 != null) {
			tempQueue.addUnvisitedUrl(str2.substring(str2.indexOf(":")+1));
			linkQueue.addUnvisitedUrl(str2);		
			str2 = br.readLine();
		}
		br.close();
		isr.close();

	}

	public static void unit(String url) throws IOException {

		if (queueFile.exists()) {
			FileReader isr = new FileReader(queueFile);
			BufferedReader br = new BufferedReader(isr);
			String str = br.readLine();
			while (str != null) {
				x = Integer.parseInt(str.substring(0, str.indexOf(":")));
				str = br.readLine();
			}

			br.close();
			isr.close();
		}
		
		x = x + 1;
		if (tempQueue.addUnvisitedUrl(url)) {
			linkQueue.addUnvisitedUrl(x + ":" + url);
			writeUrl(url);
			writeQueue(x, url);
		}
		crawling(linkQueue, url);
	}

	public static void writeUrl(String url) throws IOException {

		FileWriter fos = new FileWriter(urlFile, true);
		BufferedWriter bw = new BufferedWriter(fos);
		bw.write(url + "\r\n");
		bw.close();
		fos.close();
	}	

	public static void writeQueue(int x, String url) throws IOException {

		FileWriter fos = new FileWriter(queueFile, true);
		BufferedWriter bw = new BufferedWriter(fos);
		bw.write(x + ":" + url + "\r\n");
		bw.close();
		fos.close();
	}
	
	public static void writePoint(String point) throws IOException {

		FileWriter fos = new FileWriter(pointFile);
		BufferedWriter bw = new BufferedWriter(fos);
		bw.write(point + "\r\n");
		bw.close();
		fos.close();
	}

	public static void timer() {
		
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		String time = df.format(new Date());
		int temp = time.compareTo("02:00:00");
		while (temp != 0) {
			time = df.format(new Date());
			temp = time.compareTo("02:00:00");
			System.out.println(time + ",时间未到");
		}
		System.out.println("It's begin");
	}

}