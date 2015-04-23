package org.cbrain.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;
import org.htmlparser.visitors.HtmlPage;

public class HtmlParserTest {

	// 循环访问所有节点，输出包含关键字的值节点
	public static void extractKeyWordText(String url, String keyword) {
		try {
			// 生成一个解析器对象，用网页的 url 作为参数
			Parser parser = new Parser(url);
			// 设置网页的编码,这里只是请求了一个 gb2312 编码网页
			parser.setEncoding("utf-8");// gb2312
			// 迭代所有节点, null 表示不使用 NodeFilter
			NodeList list = parser.parse(null);
			// 从初始的节点列表跌倒所有的节点
			processNodeList(list, keyword);
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	private static void processNodeList(NodeList list, String keyword) {
		// 迭代开始
		SimpleNodeIterator iterator = list.elements();
		while (iterator.hasMoreNodes()) {
			Node node = iterator.nextNode();
			// 得到该节点的子节点列表
			NodeList childList = node.getChildren();
			// 孩子节点为空，说明是值节点
			if (null == childList) {
				// 得到值节点的值
				String result = node.toPlainTextString();
				// 若包含关键字，则简单打印出来文本
				if (result.indexOf(keyword) != -1)
					System.out.println(result);
			} // end if
				// 孩子节点不为空，继续迭代该孩子节点
			else {
				processNodeList(childList, keyword);
			}// end else
		}// end wile
	}

	// 获取一个网页上所有的链接和图片链接
	public static void extracLinks(String url) {
		try {
			Parser parser = new Parser(url);
			parser.setEncoding("utf-8");// gb2312
			// 过滤 <frame> 标签的 filter，用来提取 frame 标签里的 src 属性所、表示的链接
			NodeFilter frameFilter = new NodeFilter() {
				public boolean accept(Node node) {
					if (node.getText().startsWith("frame src=")) {
						return true;
					} else {
						return false;
					}
				}
			};
			// OrFilter 来设置过滤 <a> 标签，<img> 标签和 <frame> 标签，三个标签是 or 的关系
			OrFilter orFilter = new OrFilter(
					new NodeClassFilter(LinkTag.class), new NodeClassFilter(
							ImageTag.class));
			OrFilter linkFilter = new OrFilter(orFilter, frameFilter);
			// 得到所有经过过滤的标签
			NodeList list = parser.extractAllNodesThatMatch(linkFilter);
			for (int i = 0; i < list.size(); i++) {
				Node tag = list.elementAt(i);
				if (tag instanceof LinkTag)// <a> 标签
				{
					LinkTag link = (LinkTag) tag;
					String linkUrl = link.getLink();// url
					String text = link.getLinkText();// 链接文字
					System.out.println(linkUrl + "**********" + text);
				} else if (tag instanceof ImageTag)// <img> 标签
				{
					ImageTag image = (ImageTag) list.elementAt(i);
					System.out.print(image.getImageURL() + "********");// 图片地址
					System.out.println(image.getText());// 图片文字
				} else// <frame> 标签
				{
					// 提取 frame 里 src 属性的链接如 <frame src="test.html"/>
					String frame = tag.getText();
					int start = frame.indexOf("src=");
					frame = frame.substring(start);
					int end = frame.indexOf(" ");
					if (end == -1)
						end = frame.indexOf(">");
					frame = frame.substring(5, end - 1);
					System.out.println(frame);
				}
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
	}

	public static String getWebPage(String path) throws Exception{
		   URL url = new URL(path);
	        URLConnection conn = url.openConnection();
	        conn.setDoOutput(true); 
	        
	        InputStream inputStream = conn.getInputStream();
	        InputStreamReader isr = new InputStreamReader(inputStream, "utf8");
	        StringBuffer sb = new StringBuffer();
	        BufferedReader in = new BufferedReader(isr);
	        String inputLine;
	        
	        while ((inputLine = in.readLine()) != null) {
	            sb.append(inputLine);
	            sb.append("\n");
	        }
	        
	       return sb.toString();
	}
	
    /**
     * 按页面方式处理.解析标准的html页面
     * @param content 网页的内容
     * @throws Exception
     */
    public static void readByHtml(String content) throws Exception {
        Parser myParser;
        myParser = Parser.createParser(content, "utf8");
        HtmlPage visitor = new HtmlPage(myParser);
        myParser.visitAllNodesWith(visitor);

        String textInPage = visitor.getTitle();
        System.out.println(textInPage);
        NodeList nodelist;
        nodelist = visitor.getBody();
        
        System.out.print(nodelist.asString().trim());
    }
	

    /**
     * 分别读纯文本和链接.
     * @param result 网页的内容
     * @throws Exception
     */
    public static void readTextAndLinkAndTitle(String result) throws Exception {
        Parser parser;
        NodeList nodelist;
        parser = Parser.createParser(result, "utf8");

        NodeFilter textFilter = new NodeClassFilter(TextNode.class);
        NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
        NodeFilter titleFilter = new NodeClassFilter(TitleTag.class);
        OrFilter lastFilter = new OrFilter();
        lastFilter.setPredicates(new NodeFilter[] { textFilter, linkFilter, titleFilter });
        nodelist = parser.parse(lastFilter);
        Node[] nodes = nodelist.toNodeArray();
        String line = "";
        
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            if (node instanceof TextNode) {
                TextNode textnode = (TextNode) node;
                line = textnode.getText();
            } else if (node instanceof LinkTag) {
                LinkTag link = (LinkTag) node;
                line = link.getLink();
            } else if (node instanceof TitleTag) {
                TitleTag titlenode = (TitleTag) node;
                line = titlenode.getTitle();
            }
            
            if (isTrimEmpty(line))
                continue;
            System.out.println(line);
        }
    }
    /**
     * 去掉左右空格后字符串是否为空
     */
    public static boolean isTrimEmpty(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        }
        if (isBlank(astr.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 字符串是否为空:null或者长度为0.
     */
    public static boolean isBlank(String astr) {
        if ((null == astr) || (astr.length() == 0)) {
            return true;
        } else {
            return false;
        }
    }
	public static void main(String[] args) throws Exception {
		String searchWords = "中国军演";
		String keyword = URLEncoder.encode(searchWords, "UTF-8");
		// String
		// url="http://search.ifeng.com/sofeng/search.action?q="+keyword+"&c=1";
		// String url="http://www.blogjava.net/amigoxie";
		String url = "http://news.ifeng.com/a/20140617/40769156_0.shtml";
		extracLinks(url);
		// extractKeyWordText(url,"军演");
	}

}
