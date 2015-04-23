package org.cbrain.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.cbrain.util.Arguments;
import org.cbrain.util.FileUtil;
import org.cbrain.util.LogPattern;


public class BaiduCrawlerForKWE {
	
	
	public static void main(String[] args) {
		/**
		 * 参数说明：
		 *  1) w : search words 支持多组查询，单组查询中若有多个词用空格隔开并用""括起来. 使用demo: -w "中国 军演"  反恐  （demo中含两组查询词，第一组 ”中国 军演“ ， 第二组 ”反恐“）
		 *  2) o : output directory.默认./data  使用demo: -o ./data/baidu。
		 *  3) n : upper number of crawled documents for each group of search words.
		 *  4) f : file path of search words. 也支持以文件形式输入插叙词，每行为一组查询词，若有多个词用空格隔开并用""括起来。demo: -f ./data/baidu/search.txt
		 *  5) p : file path of log4j.properties
		 */
		
		Arguments arguments=new Arguments(args);
    	LogPattern.propertyFile=arguments.getString("p", "log4j.properties");
		Logger log=	LogPattern.getInstance(BaiduCrawlerForKWE.class.getName());
		List<String> searchWordsList=null;
		if(arguments.hasOption("w")){
			searchWordsList=Arguments.listPostProcess(arguments.getString("w"));
		}else if(arguments.hasOption("f")){
			String filePath=arguments.getString("f");
			searchWordsList=FileUtil.readSearchFile(filePath);
		}
    	BaiduCrawler.okCount=arguments.getInteger("n", 100);
    	String outPath=arguments.getString("o","./data/baidu");
    	
		BaiduCrawler.setMode(BaiduCrawler.Mode.KWE);
		BaiduCrawler.saveFailFlag=false;
		BaiduCrawler.upperPage=100;//设置检索的上限页

/*		searchWordsList=new ArrayList<String>();
		searchWordsList.add("环保");
    	String outPath="./data";
    	BaiduCrawler.okCount=10;*/
    	log.info("search words : "+searchWordsList);
    	log.info("upper threshold : "+BaiduCrawler.okCount);
    	log.info("output path : "+ outPath);
    	log.info("log property path : "+ LogPattern.propertyFile);

    	if(searchWordsList==null||searchWordsList.size()==0){
    		log.error("Error input!! No search words!! ");
    		return;
    	}
		boolean startFlag=true;
	
		long consume=0;
		for(String searchWords:searchWordsList){
			BaiduCrawler.countSave=0;
			long start=System.currentTimeMillis();
			log.info("================Search words: "+searchWords+"==================");
			String path=outPath+"/"+searchWords.replaceAll("\\s+", "_")+".txt";
			FileUtil.checkDir(outPath);
			FileUtil.checkAndRemove(path);
			BaiduCrawler.start(searchWords, path,startFlag);
			long end=System.currentTimeMillis();
			long time=(end-start)/1000;
			consume+=time;
			log.info("Total crawling time is "+time+"s ~~~");		
		}
		log.info("Totally consume time is "+consume +"s ~~~");
	}
}
