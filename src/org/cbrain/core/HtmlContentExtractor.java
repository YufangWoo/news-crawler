package org.cbrain.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.cbrain.entity.LinkDb;
import org.cbrain.util.LogPattern;
import org.cbrain.util.Md5;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;


public class HtmlContentExtractor {
	/**
	 * @param args
	 * @throws Exception
	 * 核心内容：
	 * 1.特定主题词检索结果中url抓取
	 * 2.url所指网页中特定主题信息抽取
	 */
	
	private static final Logger log=	LogPattern.getInstance(HtmlContentExtractor.class.getName());

    public static void main(String args[]) throws Exception {
    	

//		String  path= "http://stock.sohu.com/20140701/n401598368.shtml";//抓取内容为空(以div为标签)
//    	String path="http://money.163.com/14/0702/09/A04UJ0IU00254TI5.html";//卡死
//    	String path="http://news.163.com/14/0703/08/A07G4B3D0001121M.html";//卡死
//		String path="http://money.163.com/14/0702/10/A052R77I00254TI5.html";//Connection reset
//		String path="http://finance.qq.com/a/20140701/000982.htm";
//		String path="http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&bs=%E4%BF%84%E7%BD%97%E6%96%AF+%E4%B9%8C%E5%85%8B%E5%85%B0&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=news&word=%E4%B8%AD%E5%9B%BD+%E5%86%9B%E6%BC%94&rsv_sug3=12&rsv_sug4=360&rsv_sug1=9&rsv_sug2=0&inputT=3588";
    	String path="http://news.163.com/14/1125/11/ABT5KH3600014AEE.html";
    	path="http://v.ifeng.com/mil/arms/201411/01b6b128-eb31-4894-8ece-2f686b3065bc.shtml";
    	path="http://www.huaxia.com/thjq/jsxw/dl/2014/11/4165911.html";
    	path="http://news.163.com/14/1125/11/ABT5KH3600014AEE.html";
    	path="http://www.ibtimes.com.cn/articles/40383/20141107/872453.htm";//卡顿测试
    	path="http://www3.nhk.or.jp/news/html/20141210/t10013865431000.html";
    	path="http://mil.sohu.com/20060518/n243284960.shtml";
    	path="http://ent.qq.com/a/20150104/014190.htm";//负样本，会抽取出很多
    	path="http://img0.imgtn.bdimg.com/it/u=3523174185,1386953593&fm=21&gp=0.jpg";
//    	path="http://test.com";
		String searchWords = "中国军演";
//		String keyword = URLEncoder.encode(searchWords, "UTF-8");
//		String path="http://search.ifeng.com/sofeng/search.action?q="+keyword+"&c=1";
		String result=new WebpageCrawler().crawl(path);
		System.out.println("crawl results>> "+result);
//        extractTextByTagP(result);
//        extractTextByTextNode(result);
//        extractLink(result,searchWords);
        
    }
    
	public static String getWebPage(String path,String encoding) throws Exception{
		   URL url = new URL(path);
	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true); 
	        
	        InputStream inputStream = conn.getInputStream();
	        InputStreamReader isr = new InputStreamReader(inputStream, encoding);//utf-8, gb2312
	        StringBuffer sb = new StringBuffer();
	        BufferedReader in = new BufferedReader(isr);
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	            sb.append(inputLine);
	            sb.append("\n");
	        }
	        
	       return StringEscapeUtils.unescapeJava(sb.toString());
	}
    
    public static boolean isInformative(Node node,HashMap<String,Integer> parentWeight){
    	/**@algorithm
    	 * 依靠四種特征判斷節點內的文本是否是有用信息：
    	 * <本身信息>
    	 * 1.文本密度：節點中中文字符長度比上整個節點的長度。denThre=0.5
    	 * 2.文本長度：節點中中文字符長度。lenThre=20
    	 * 3.標點數目：節點中中文標點的個數。numThre=1  PS:正文內容一般以標點符號結尾，可用此過濾掉其他鏈接的新聞
    	 * <上下文信息>
    	 * 4.數據區域適當放鬆條件：如段落title,可能沒有標點，字數也少。但是若他與其他數據區域的父節點是同一個節點，
    	 * 		則該節點也很有可能屬於數據區域。具體做法是，當每個節點被判定為數據區域時，則其父節點的權重加1。weiThre=2
    	 * 5.該節點與上一個數據區域節點中間間隔的<p>標籤數。tagThre=2  (暂时没用)
    	 * 
    	 * @param Node node - 待判断的节点
    	 * @param HashMap<String,Integer> parentWeight - key: 节点(某个节点的父节点)的md5值，value: 其确定为有效数据的子节点的个数
    	 */
    	
    	boolean flag=false;
//    	double denLowerThre=0.5;
    	double denUperThre=0.95;
    	int titleLenThre=30;
    	int lenThre=30;
    	int weiThre=2;
		String fatherMd5=null;
		if(node.getParent()!=null){
			 fatherMd5=Md5.getMD5(node.getParent().toHtml().getBytes());
		}
    	String text=node.toPlainTextString().trim();
    	if(text.length()==0){
    		return false;
    	}
    	//1.判断是否以标点结尾
    	if(text.matches(".*[。！？“]$")){
    		//判断是否为其他新闻
    		//直接判断文本长度，一般有标点，且大于20个字符的文本，通常为正文。
    		//先判断长度的好处：1.文本密度可能将用于文本去掉，2.父节点权重可能将文本开头去掉。
    		if(text.length()>=lenThre){
				flag=true;
			}else{
				//2.计算文本密度
	    		double den=1.0*text.length()/node.toHtml().length();
    			if(den>=denUperThre){//文本密度大于denUperThre直接save
    	    		flag=true;
    			}else{
    				//3.check父节点的权重
    				if(parentWeight.containsKey(fatherMd5)&&parentWeight.get(fatherMd5)>=weiThre){
        	    		flag=true;
    				}
    			}
			}
    	}else{
    		//判断是否为段落标题(加长度约束)
    		if(parentWeight.containsKey(fatherMd5)&&parentWeight.get(fatherMd5)>=weiThre&&text.length()<titleLenThre){
	    		flag=true;
			}
    	}
    	if(flag){
    		if(parentWeight.containsKey(fatherMd5)){
    			parentWeight.put(fatherMd5, parentWeight.get(fatherMd5)+1);
    		}else{
    			parentWeight.put(fatherMd5, 1);
    		}
    	}
    	 return flag;
    }
    
    public static boolean isInformativeStricter(Node node,HashMap<String,Integer> parentWeight){
    	/**@algorithm
    	 * 用于普通文本节点判断，为了避免引入噪声，抽取的要求设置更严格。
    	 * 两点改变：1.文本长度严格按中文和标点符号计算（不包括数字和字母）
    	 * 				  2.去掉文本密度 （噪声可能也有较高的密度）
    	 */
    	
    	boolean flag=false;
    	int lenThre=30;
    	int weiThre=2;
		String fatherMd5=null;
		if(node.getParent()!=null){
			 fatherMd5=Md5.getMD5(node.getParent().toHtml().getBytes());
		}
    	String text=node.toPlainTextString().trim();
    	int length=0;
    	Pattern p=Pattern.compile("[\\u4e00-\\u9fa5，。！？“”：——]");
    	Matcher m=p.matcher(text);
    	while(m.find()){
    		length++;
    	}
    	if(length==0){
    		return false;
    	}else{
//    		System.out.println(node.toString()+"	"+length);
    	}
    	//1.判断是否以标点结尾
    	if(text.matches(".*[。！？“]$")){
    		if(length>=lenThre){
				flag=true;
			}else{
				//3.check父节点的权重
				if(parentWeight.containsKey(fatherMd5)&&parentWeight.get(fatherMd5)>=weiThre){
    	    		flag=true;
				}	
			}
    	}else{
    		//判断是否为段落标题
    		if(parentWeight.containsKey(fatherMd5)&&parentWeight.get(fatherMd5)>=weiThre){
	    		flag=true;
			}
    	}
    	if(flag){
    		if(parentWeight.containsKey(fatherMd5)){
    			parentWeight.put(fatherMd5, parentWeight.get(fatherMd5)+1);
    		}else{
    			parentWeight.put(fatherMd5, 1);
    		}
    	}
    	 return flag;
    }
    public static List<String> extractTextByTextNode(String content){
    	List<String> doc=new ArrayList<String>();//每个元素为一个段落
    	 if (content == null) {
		    	return doc;
		  }
    	 try{
			     Parser parser = Parser.createParser(content, "utf8");      
			     NodeFilter textFilter = new NodeClassFilter(TextNode.class);
		        NodeList nodelist=parser.extractAllNodesThatMatch(textFilter);
		        HashMap<String,Integer> parentWeight=new HashMap<String,Integer>();
		        for (int i = 0; i < nodelist.size(); i++) {
		        	Node textnode = (Node) nodelist.elementAt(i);
		        	if(textnode.toPlainTextString().trim().length()>0)
		        		log.debug(i+": "+" content: "+textnode.toPlainTextString());
		        	if(isInformativeStricter(textnode,parentWeight)){
			        	log.debug(i+": "+" content: "+textnode.toPlainTextString());
			        	doc.add(textnode.toPlainTextString());
		        	}        	
		        }  
		}catch(Exception e){
			e.printStackTrace();
			log.error("Text extractor  has encountered a problem!! "+e.getMessage());
		}
    	 
	     return doc;

    }
    
	public static List<String> extractTextByTagP(String content){
			List<String> doc=new ArrayList<String>();//每个元素为一个段落
			try{
				 if (content == null) {
				    	return doc;
				    }
				     Parser parser = Parser.createParser(content, "utf8");      
			        TagNameFilter paraFilter=new TagNameFilter("p");//get content between <p> </p>
//			        TagNameFilter paraFilter2=new TagNameFilter("br");//get content between <br> </br>
//			        NodeFilter filter = new OrFilter(paraFilter, paraFilter2);
			        NodeList nodelist=parser.extractAllNodesThatMatch(paraFilter);//报错！！
			        HashMap<String,Integer> parentWeight=new HashMap<String,Integer>();
			        for (int i = 0; i < nodelist.size(); i++) {
			        	Node textnode = (Node) nodelist.elementAt(i);
			        	log.debug(i+": "+" content: "+textnode.toPlainTextString());

			        	if(isInformative(textnode,parentWeight)){
				        	log.debug(i+": "+" content: "+textnode.toPlainTextString());
				        	doc.add(textnode.toPlainTextString());
			        	}        	
			        }  
			}catch(Exception e){
				e.printStackTrace();
				log.error("Text extractor  has encountered a problem!! "+e.getMessage());
			}
	        return doc;
	}
	
	
	public static void extractLink(String content, String keyword) {
		/**
		 * 通过判断链接中是否含keyword确定是否为有效链接。
		 * 注：keyword可能是一组词语或者是一个短语，检索出的内容或许只是匹配上keyword中部分词语
		 */
		try {
		    Parser  parser = Parser.createParser(content, "utf8");
	        NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
			NodeList nodelist = parser.extractAllNodesThatMatch(linkFilter);
			int lastNodeID=0;//上一个确定为有效链接的node ID
			int disThre=8; //通常检索出来的有效链接的id是连续的，因此可用此区分那些广告信息
			  for (int i = 0; i < nodelist.size(); i++) {
		        	Node node = (Node) nodelist.elementAt(i);
		        	LinkTag link = (LinkTag) node;
					String linkUrl = link.getLink();// url
					String text = link.getLinkText();// 链接文字
/*					//simple keywords test for debug
					boolean flag=false;
					String[] tmps=keyword.split("\\s+");
					for(String tmp:tmps){
						if(text.contains(tmp)){
							flag=true;break;
						}
					}
					if(flag){*/
		        	if(containKeyword(text,keyword)){
		        		if(lastNodeID>0 &&i-lastNodeID>disThre){
		        			log.debug("Noisy link!!!");
		        			continue;
		        		}
			        	if(!linkUrl.startsWith("http")) continue;
			        	log.debug(i+":"+linkUrl+", "+text);
			        	lastNodeID=i;
			        	LinkDb.addUnvisitedUrl(linkUrl);
		        	}else{
/*		        		if(text.contains("下一页")){
			        		System.out.println(i+":"+linkUrl+", "+text);
		        		}*/
		        	}
		      }    
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Link extractor  has encountered a problem!! "+e.getMessage());
		}
		
	}
	
	public static void extractBDSearchResultsByReg(String content,HashMap<String,String> urlAuthor) {
		String reg="<li class=\"result\" id=\"\\d+\">.*?<a href=\"(.*?)\"[\\s\\S]*?<p class=\"c-author\">(.*?)</p>";
		Pattern p=Pattern.compile(reg);
		Matcher m=p.matcher(content);
		log.debug(content);
		while(m.find()){
			String url=m.group(1);
			String author=m.group(2).replaceAll("(&nbsp;)+","@").replaceAll("\\s+", "_").replaceAll("[\\-:]", "");
			log.info(m.group(2).replaceAll("(&nbsp;)+", "  ")+"\t"+url);
			urlAuthor.put(url, author);
			LinkDb.addUnvisitedUrl(url);//全局(与visitedUrl判重了)
		}
	}
	
	public static boolean containKeyword(String text,String keyword) throws Exception{
		 boolean flag=false;
		 List<Term> tokens=ToAnalysis.parse(keyword);
		 for(Term t:tokens){
				String token=t.getName();
				if(text.contains(token)){
					flag=true;
					break;
				}
		}		
		 return flag;
	}
	
	
}