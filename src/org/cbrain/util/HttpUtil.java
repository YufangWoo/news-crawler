package org.cbrain.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;
import java.util.zip.*;

public class HttpUtil {
	/**
	 * 自动判断网页编码。使用的是java自带的HttpURLConnection。
	 * （HttpURLConnection可以实现一些基本的抓取页面的功能，但是对于一些比较高级的功能，比如重定向的处理，HTML标记的去除，仅仅使用HttpURLConnection还是不够的。）
	 * 
	 * 算法：
	 * 1.响应头查找Content-Type中的charset，若找到了charset则跳过步骤2，3，直接进行第4步
	 * 2.若步骤1得不到charset，则先读取网页内容，解析meta里面的charset得到页面编码
	 * 3.若步骤2种还是没有得到页面编码，那没办法了设置默认编码为UTF-8
	 * 4.使用得到的charset重新读取响应流
	 * 
	 * 注意：
	 * 1.现在站点几乎都启用了gzip压缩支持，所以在请求头里面加上Accept-Encoding:gzip,deflate，这样站点会返回压缩流，能显著的提高请求效率
	 * 2.由于网络流不支持流查找操作，也就是只能读取一次，为了提高效率，所以这里建议将http响应流先读取到内存中，以方便进行二次解码，没有必要重新请求去重新获取响应流
	 */

    public static String sendGet(String url) throws Exception {
        return send(url, "GET", null, null);
    }

    public static String sendPost(String url, String param) throws Exception {
        return send(url, "POST", param, null);
    }

    public static String send(String url, String method, String param, Map<String, String> headers) throws Exception {
        String result = null;
        HttpURLConnection conn = getConnection(url, method, param, headers);
        String charset = conn.getHeaderField("Content-Type");
        charset = detectCharset(charset);
        InputStream input = getInputStream(conn);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int count;
        byte[] buffer = new byte[4096];
        while ((count = input.read(buffer, 0, buffer.length)) > 0) {  //将响应流读到内存中
            output.write(buffer, 0, count);
        }
        input.close();
        // 若已通过请求头得到charset，则不需要去html里面继续查找
        if (charset == null || charset.equals("")) {
            charset = detectCharset(output.toString());
            // 若在html里面还是未找到charset，则设置默认编码为utf-8
            if (charset == null || charset.equals("")) {
                charset = "utf-8";
            }
        }
        
        result = output.toString(charset);
        output.close();

        // result = output.toString(charset);
        // BufferedReader bufferReader = new BufferedReader(new
        // InputStreamReader(input, charset));
        // String line;
        // while ((line = bufferReader.readLine()) != null) {
        // if (result == null)
        // bufferReader.mark(1);
        // result += line;
        // }
        // bufferReader.close();

        return result;
    }

    private static String detectCharset(String input) {
        Pattern pattern = Pattern.compile("charset=\"?([\\w\\d-]+)\"?;?", Pattern.CASE_INSENSITIVE);
        if (input != null && !input.equals("")) {
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private static InputStream getInputStream(HttpURLConnection conn) throws Exception {
        String ContentEncoding = conn.getHeaderField("Content-Encoding");
        if (ContentEncoding != null) {
            ContentEncoding = ContentEncoding.toLowerCase();
            if (ContentEncoding.indexOf("gzip") != 1)
                return new GZIPInputStream(conn.getInputStream());
            else if (ContentEncoding.indexOf("deflate") != 1)
                return new DeflaterInputStream(conn.getInputStream());
        }

        return conn.getInputStream();
    }

    static HttpURLConnection getConnection(String url, String method, String param, Map<String, String> header) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setRequestMethod(method);

        // 设置通用的请求属性
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.117 Safari/537.36");
        conn.setRequestProperty("Accept-Encoding", "gzip,deflate"); //站点会返回压缩流,可提高请求效率

        String ContentEncoding = null;
        if (header != null) {
            for (Entry<String, String> entry : header.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("Content-Encoding"))
                    ContentEncoding = entry.getValue();
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (method == "POST") {
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (param != null && !param.equals("")) {
                OutputStream output = conn.getOutputStream();
                if (ContentEncoding != null) {
                    if (ContentEncoding.indexOf("gzip") > 0) {
                        output=new GZIPOutputStream(output);
                    }
                    else if(ContentEncoding.indexOf("deflate") > 0) {
                        output=new DeflaterOutputStream(output);
                    }
                }
                output.write(param.getBytes());
            }
        }
        // 建立实际的连接
        conn.connect();
        return conn;
    }
    
    public static void main(String[] args) throws Exception{
    	System.out.println(sendGet("http://news.xinhuanet.com/mil/2014-07/03/c_126703866_2.htm"));
    }
}
