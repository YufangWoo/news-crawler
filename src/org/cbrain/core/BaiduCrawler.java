package org.cbrain.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cbrain.entity.LinkDb;
import org.cbrain.util.FileUtil;
import org.cbrain.util.LogPattern;
import org.cbrain.util.TimeUtil;

public class BaiduCrawler {
	
	public static int okCount=100;//需存储的文档数目
	public static boolean okFlag=false;
	public static boolean saveFailFlag=false;
	public static int countSave=0;//统计成功抽取信息的url
	public static int upperPage=100;//设置检索的上限页
	public static int connectTimeOut=10000;//连接时间10s
	public static int dataTimeOut=20000;//数据传输时间20s
	public static int tryTimes=2;//超时爬取次数
	private static Logger log=	LogPattern.getInstance(BaiduCrawler.class.getName());
	private static HashMap<String,String> urlAuthor=new HashMap<String,String>();
	private static Set<String> visitedAuthors=new HashSet<String>();//用于判重
	private static HashMap<String,String> topicTime=new HashMap<String,String>();// key: search words  value: latest time
	private static int repTime=0;
	private static String firstUrl="http://news.baidu.com/ns?word=KEYWORD" 	//按时间降序显示检索结果
			+ "&sr=0&cl=2&rn=20&tn=news&ct=0&clk=sortbytime";
	private static String otherPage="http://news.baidu.com/ns?word=KEYWORD"
			+ "&pn=PAGE&cl=2&ct=0&tn=news&rn=20"
			+ "&ie=utf-8&bt=0&et=0";
	
	public static enum Mode{
		KWE,CORPUS
	}
	private  static Mode mode=Mode.CORPUS;//默认corpus
	
	public static Mode getMode() {
		return mode;
	}
	public static void setMode(Mode mode) {
		BaiduCrawler.mode = mode;
	}
	public static int getRepTime() {
		return repTime;
	}
	public static void setRepTime(int repTime) {
		BaiduCrawler.repTime = repTime;
	}
	public static void prepareUrl(String searchWords){
		String keyword="";
		String[] tmp=searchWords.split("\\s+");
		try {
			if(tmp.length==1){
				keyword=WebpageCrawler.encodeSearchWords(tmp[0]);
			}else{
				StringBuffer sb=new StringBuffer();
				for(int i=0;i<tmp.length;i++){
					sb.append(WebpageCrawler.encodeSearchWords(tmp[i])+(i==tmp.length-1?"":"+"));
				}
				keyword=sb.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Wrong with search words encoding!!");
			System.exit(0);
		}	
		//按时间降序显示检索结果
		firstUrl.replace("KEYWORD", keyword);
		otherPage.replace("KEYWORD", keyword);
	}
	
	public static boolean checkRepeatByTime(String searchWords,String newtime){
		if(newtime==null)
			return true;
		if(!topicTime.containsKey(searchWords))
			return false;
		String oldtime=topicTime.get(searchWords);
		if(TimeUtil.compareTime(newtime, oldtime)>0) //
			return false;
		else
			return true;//repreat
	}
	
	public static void initialize(String dir){
		if(FileUtil.checkDir(dir)){
			urlAuthor.clear(); //清空使urlAuthor局部化（只含本主题相关的url）
			visitedAuthors.clear();
			visitedAuthors.addAll(FileUtil.readAllFileName(dir));//load磁盘中的数据，用于判重
		}
	}
	
	/**
	 * 
	 * @param searchWords - 检索词，可以是多个，用于文件名时词语之间的空格用&取代
	 * @param path - 不同模式下path不一样，KWE模式下具体到文件名：savePath/searchWords.txt，CORPUS模式下具体到文件夹：savePath/searchWord/
	 * @param startFlag - 是否是第一次检索该词语
	 */
	public static void start(String searchWords,String path,boolean startFlag){
		okFlag=false;
		String latestTime=null;
		int count=0;//统计已爬取的url
		HashSet<String> failedUrl=new HashSet<String>();
		WebpageCrawler wc=new WebpageCrawler();
		prepareUrl(searchWords);
		String url="";
		for(int page=1;page<=upperPage;page++){
			log.info("Page "+page+" of "+searchWords+" .......");
			if(page==1) url=firstUrl;
			else url=otherPage.replace("PAGE", (page-1)*20+"");
			log.info("url>>"+url);
			String result = wc.crawl(url);
			if (result == null || result.trim().equals("")) {
				continue;
			}
			//从检索结果页面中抓取url
			log.info("======================Extract Links========================");
			HtmlContentExtractor.extractBDSearchResultsByReg(result,urlAuthor);
		//	HtmlContentExtractor.extractLink(result,searchWords);
			//解析抓取的url
			log.info("================Crawl Links and Parse Pages===================");
			while(!LinkDb.unVisitedUrlsEmpty()){
				//队头 URL 出对
				String visitUrl=LinkDb.unVisitedUrlDeQueue();
				if(visitUrl==null)
					continue;
				String author=urlAuthor.get(visitUrl);
				String newtime=author.substring(author.indexOf("@")+1);
				if(latestTime==null && newtime!=null)
					latestTime=newtime;//第一个url对应的时间是最近的时间
				//抓取之前判重(第一次哈希全部查重，后续按时间判重（可提前退出）)
				if(startFlag&&visitedAuthors.contains(author)){
					log.info(" Already Crawled news from "+author);
					urlAuthor.remove(visitUrl);//移除已访问url
					continue;						
				}else if(!startFlag && checkRepeatByTime(searchWords,newtime)){
					//按时间判重
					repTime++;
					log.info("Repeat by Time!! Stop crawl!!");
					LinkDb.clearUnVisitedUrl();//退出之前清空UnvisitedQ
					//更新时间
					if( !checkRepeatByTime(searchWords,latestTime))
						topicTime.put(searchWords, latestTime);
					log.info("Latest time for #"+searchWords+"# is : " + topicTime.get(searchWords) +"; Latest time for this round is : " + latestTime  );	
					return;
				}
				count++;
				log.info("Crawl Count "+count+": "+urlAuthor.get(visitUrl)+"\t"+visitUrl);
				String content= wc.crawl(visitUrl);
				List<String> doc=new ArrayList<String>();
				doc = HtmlContentExtractor.extractTextByTagP(content);
				if(doc.size()==0){
					doc = HtmlContentExtractor.extractTextByTextNode(content);//按<P></P>抽不到内容再按普通文本节点抽取一遍
					if(doc.size()==0){
						failedUrl.add(visitUrl);
						log.error("Warning: Extraction failed url  "+visitUrl);
						continue;
					}
				}
				if(mode.equals(Mode.CORPUS)){
					FileUtil.save(path, visitUrl, urlAuthor.get(visitUrl), doc);
				}else if(mode.equals(Mode.KWE)){
					FileUtil.saveAsLine(path,doc);
				}
				visitedAuthors.add(urlAuthor.get(visitUrl));
				urlAuthor.remove(visitUrl);//移除已访问url
				countSave++;
				log.info("Save Count "+countSave);
				LinkDb.addVisitedUrl(visitUrl);
				if(countSave>=okCount){
					okFlag=true;
					log.info("The number of crawled docs is OK~~~");
					return;
				}
			}
			if(LinkDb.getVisitedUrlNum()>=1000){//目前暂未通过VisitedUrl判重，因此事实上没必要存储。
				LinkDb.clearVisitedUrl();
			}
			//判断是否还有下一页
			Pattern p=Pattern.compile("<a.*?下一页.*?</a>");
			Matcher m=p.matcher(result);
			boolean continueFlag=false;
			while(m.find()){
				continueFlag=true;
			}
			if(!continueFlag)  //跳出for循环
				break;
		}
		//更新爬取的最新时间，留待下一轮判重比较用
		if( !checkRepeatByTime(searchWords,latestTime))
			topicTime.put(searchWords, latestTime);
		log.info("Latest time for #"+searchWords+"# is : " + topicTime.get(searchWords) +"; Latest time for this round is : " + latestTime  );	
		//save failed urls
		if(mode.equals(Mode.CORPUS)&&saveFailFlag){
			String file=path+"failedUrl.txt";
			FileUtil.writeLines(file,new ArrayList<String>(failedUrl),true);//追加写
		}
		log.info("Totla Save Count "+countSave);
	}

}
