package org.cbrain.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.cbrain.util.InputStreamByteTransfer;
import org.cbrain.util.LogPattern;

/**
 * 请求页面，不解析
 * 
 * @author Yufang Wu
 * 
 */
public class WebpageCrawler {
	private HttpClient client = new DefaultHttpClient();
	private HttpHost proxy = new HttpHost("127.0.0.1", 8087);
	private String defaultEncode = "UTF-8";
	private static final Logger log = LogPattern.getInstance(WebpageCrawler.class.getName());
	{
		//设置连接超时和数据传输超时
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, BaiduCrawler.connectTimeOut);//连接时间
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  BaiduCrawler.dataTimeOut);//数据传输时间	
	}
	
	public WebpageCrawler() {
		// client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy); //用代理访问国内网站很慢
	}

	private String dump(HttpEntity entity, String charset) throws IOException {
		/**
		 * EntityUtils.toString()会自动获取请求中的编码，命令如下 String charset =
		 * EntityUtils.getContentCharSet(entity);
		 * 若没返回编码则使用传递的编码，若没有传递编码，则使用默认的ISO-8859-1编码
		 */
		String responseBody = EntityUtils.toString(entity, charset);// utf-8
		log.debug("%%%%responseBody>>" + responseBody);
		// 关闭httpcilent相关资源；
		EntityUtils.consume(entity);
		return responseBody;
	}

	public static String encodeSearchWords(String input) throws Exception {
		if (input.equals("") || input == null)
			return "";
		String results = "";
		String[] tmp = input.split("\\s+");
		for (int i = 0; i < tmp.length; i++) {
			results += URLEncoder.encode(tmp[i], "UTF-8")
					+ ((i == tmp.length - 1) ? "" : "+");
		}
		return results;
	}
	
	public String getResponseBody(HttpEntity entity,byte[] contentBytes) throws UnsupportedEncodingException{
		String responseBody=null;
		String charSet = EntityUtils.getContentCharSet(entity);
		if(charSet==null){
			// 判断页面的编码方式			
			charSet = defaultEncode;// 默认编码
			responseBody = new String(contentBytes, charSet); //UnsupportedEncodingException
			charSet = getCharSet(responseBody);	
			if (!charSet.equalsIgnoreCase(defaultEncode)
					&& !charSet.trim().equals("")) {
				responseBody = new String(contentBytes, charSet);
			}
		}else{
			responseBody = new String(contentBytes, charSet);
		}		
		log.debug("Charset : " + charSet);
		return StringEscapeUtils.unescapeJava(responseBody);
	}
	
public String crawl(String url){
	return crawl(url,0);
}
	
	public String crawl(String url,int times) {
		
		String result = null;
		HttpGet get =null;
		HttpEntity entity=null;
		try {
			get = new HttpGet(url);
			get.setHeader("User-Agent",
					"	Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			HttpResponse response = client.execute(get);//  ClientProtocolException, IOException
			log.debug( response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			InputStream inputStream = entity.getContent();// IllegalStateException, IOException
			log.debug("InputStream available : "
					+ inputStream.available());
			byte[] contentBytes = InputStreamByteTransfer
					.InputStreamToByte(inputStream); //IOException
			result=getResponseBody(entity,contentBytes);
			get.abort();
			EntityUtils.consume(entity); //IOException
		} catch(SocketTimeoutException ste){
			log.error(ste.getMessage());
			get.abort();
			try {
				EntityUtils.consume(entity);
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}		
			if(times<BaiduCrawler.tryTimes-1){
				log.info("Second try ~~~"); 
				return crawl(url,++times);
			}
		}catch(Exception e) {
			log.error("Crawl has encountered a problem!! " + e.getMessage());
			get.abort();
			try {
				EntityUtils.consume(entity);
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}		
			log.info("Try gizp format ~~~~~");
			return crawlGzip(url);
		}
		return result;
	}

	public String crawlGzip(String url){
		return crawlGzip(url,0);
	}
	public String crawlGzip(String url,int times) {

		String result = null;
		HttpGet get = null;
		HttpEntity entity = null;
		try {
			get = new HttpGet(url);
			get.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0");
			get.setHeader("Connection", "keep-alive");
			get.setHeader("Accept-Encoding", "gzip,deflate");//
			get.setHeader("Cache-Control", "max-age=0");//
			get.setHeader("Accept-Language",
					"	zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
			get.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			HttpResponse response = client.execute(get);
			// check the state of response
			log.debug("response status : "
					+ response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			log.debug(entity.getContentLength());
			log.debug(entity.getContentType());
			log.debug("InputStream available : "
					+ inputStream.available());
			GzipDecompressingEntity gizp = new GzipDecompressingEntity(entity);
			// byte[] contentBytes = IOUtils.toByteArray(inputStream);
			// byte[] contentBytes = EntityUtils.toByteArray(entity);
			byte[] contentBytes = InputStreamByteTransfer
					.InputStreamToByte(gizp.getContent());
			result=getResponseBody(entity,contentBytes);
			get.abort();
			EntityUtils.consume(entity);
		} catch(SocketTimeoutException ste){
			log.error(ste.getMessage());
			get.abort();
			try {
				EntityUtils.consume(entity);
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}		
			if(times<BaiduCrawler.tryTimes-1){
				log.info("Second try ~~~"); 
				return crawl(url,++times);
			}
		}catch (Exception e) {
			log.error("Crawl has encountered a problem!! " + e.getMessage());
			if(get!=null)
				get.abort();
			try {
				if(entity!=null)
					EntityUtils.consume(entity);
			} catch (IOException e1) {
					log.error(e1.getMessage());
			}
		}
		return result;
	}

	/**
	 * 正则获取字符编码
	 */
	private static String getCharSet(String content) {
		String regex = "<[mM][eE][tT][aA][^>]*?charset=\"*(\\w*[\\-]*\\d*)\"*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			return matcher.group(1);
		else
			return null;
	}

}
