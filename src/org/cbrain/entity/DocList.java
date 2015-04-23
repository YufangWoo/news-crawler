package org.cbrain.entity;

public class DocList {
	static int totalCount=0;
	private int id;
	private String url;
	private String title; 
	private String brief;
	private String source;
	private String time;
	
	public DocList(){
		this.id=totalCount;
		totalCount++;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBrief() {
		return brief;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}

	
	@Override
	public String toString() {
		return "DocList [id=" + id + ", url=" + url + ", title=" + title
				+ ", brief=" + brief + ", source=" + source + ", time=" + time
				+ "]";
	}
}
