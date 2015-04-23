package org.cbrain.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.cbrain.util.FileUtil;
import org.cbrain.util.LogPattern;


public class BaiduCrawlerForCorpus {
	
	
	public static void main(String[] args) {
		
		/**
		 * 命令行参数： 检索关键词txt文件  [爬虫结果存储路径] [是否存储失败链接]
		 * 1.检索关键词txt文件  （必选参数）格式：每行一个或一组检索关键词
		 * 2.爬虫结果存储路径  （可选参数）存储路径, 默认为"./data/baidu"
		 * 3.是否存储失败链接  （可选参数）此参数多用于调试， 1- save, 0 - ignore. 默认为1
		 */
		Logger log=	LogPattern.getInstance(BaiduCrawlerForCorpus.class.getName());
		BaiduCrawler.setMode(BaiduCrawler.Mode.CORPUS);
		BaiduCrawler.okCount=100000000;
		if(args.length<1){
			log.error("Error input!!!");
			return;
		}
		String savePath="./data/baidu";
		if(args.length==3){
			savePath=args[1];
			BaiduCrawler.saveFailFlag=Integer.parseInt(args[2])==0?false:true;
		}else if(args.length==2){
			savePath=args[1];
		}	
		String searchFile=args[0];
		
/*		String searchFile="./data/search.txt";
		String savePath="./data/baidu";
		saveFailFlag=0;*/
		
		List<String> searchWordsList=FileUtil.readSearchFile(searchFile);
		boolean startFlag=true;
		while(true){
			BaiduCrawler.setRepTime(0);
			for(String searchWords:searchWordsList){
				log.info("================Search words: "+searchWords+"==================");
				String dir=savePath+"/"+searchWords.replaceAll("\\s+", "&")+"/";
				if(startFlag){
					BaiduCrawler.initialize(dir);//初次启动，check本地磁盘是否有已爬取内容
				}
				BaiduCrawler.start(searchWords, dir,startFlag);
			}
			if(startFlag)
				startFlag=false;
			//睡眠
			try {
				if(BaiduCrawler.getRepTime()*1.0/searchWordsList.size()>0.5){
					log.info("More than half topics are repeat, try to sleep 1 min ~~~");
					Thread.sleep(60*1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
