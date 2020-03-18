package com.tianyoukeji.parent.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="thumbnail")
public class ThumbnailProperities {
	
	private Rect large;
	private Rect middle;
	private Rect small;
	
	
	public Rect getLarge() {
		return large;
	}


	public void setLarge(Rect large) {
		this.large = large;
	}


	public Rect getMiddle() {
		return middle;
	}


	public void setMiddle(Rect middle) {
		this.middle = middle;
	}


	public Rect getSmall() {
		return small;
	}


	public void setSmall(Rect small) {
		this.small = small;
	}


	public static class Rect{
		private int width;
		private int height;
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		public int getHeight() {
			return height;
		}
		public void setHeight(int height) {
			this.height = height;
		}
		
	}
}


