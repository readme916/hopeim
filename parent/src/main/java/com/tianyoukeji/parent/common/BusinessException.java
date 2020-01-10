package com.tianyoukeji.parent.common;

import java.util.Date;

public class BusinessException extends RuntimeException {

	private int status;
	private String error;
	public BusinessException(int status, String error) {
		super();
		this.status = status;
		this.error = error;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
}
