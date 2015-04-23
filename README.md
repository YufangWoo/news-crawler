# news-crawler
特定主题新闻非定向爬虫



参数说明：
1) w : search words 支持多组查询，单组查询中若有多个词用空格隔开并用""括起来. 使用demo: -w "中国 军演"  反恐  （demo中含两组查询词，第一组 ”中国 军演“ ， 第二组 ”反恐“）
2) o : output directory.默认./data  使用demo: -o ./data/baidu。
3) n : upper number of crawled documents for each group of search words.
4) f : file path of search words. 也支持以文件形式输入插叙词，每行为一组查询词，若有多个词用空格隔开并用""括起来。demo: -f ./data/baidu/search.txt

Demo:
java -jar news-crawler-baidu-v4.2.jar -w 拆迁 "香港 游行" -o ./data -n 100


其他说明:
1.以当前检索关键词（拆迁.txt）命名检索结果。
2.此jar包是由jdk1.7编译导出的runnable类型jar包,不需额外配置路径。
3.log4j.properties文件与jar包放在同级目录。
