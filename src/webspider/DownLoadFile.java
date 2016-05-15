package webspider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class DownLoadFile {

	public String getFileNameByUrl(String url, String contentType) {
        
		int x = Integer.parseInt(url.substring(0, url.indexOf(":")));
		url = url.substring(url.indexOf(":")+1);
		url = url.substring(7);
		if (contentType.indexOf("html") != -1) {//这里可以将html改为text
			url = url.replaceAll("[\\?/:*|<>\"]", "_") + ".txt";
			url = x + "_" + url; 
			return url;
		} else {
			url = x + "_" + url; 
			return url.replaceAll("[\\?/:*|<>\"]", "_");
		}
	}

	public void saveToLocal(InputStream resStream, String filePath) {
		try {

			DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(filePath)));
			byte[] buff = new byte[100];
			int rc = 0;
			while ((rc = resStream.read(buff, 0, 100)) > 0) {
				out.write(buff, 0, rc);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String downloadFile(String path, String url) throws UnsupportedEncodingException {

		String filePath = null;
		String tempurl = url;
		url = url.substring(url.indexOf(":")+1);		
		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(60000);
		String url1 = url;
		GetMethod getMethod = new GetMethod(url1.replaceAll(" ", "%20"));
		getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: " + getMethod.getStatusLine());
				filePath = null;
			}
			InputStream resStream = getMethod.getResponseBodyAsStream();
			filePath = path + getFileNameByUrl(tempurl, getMethod.getResponseHeader("Content-Type").getValue());
			saveToLocal(resStream, filePath);

		} catch (HttpException e) {
			System.out.println("Please check your provided http address!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return filePath;
	}

}