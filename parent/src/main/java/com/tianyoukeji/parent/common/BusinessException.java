package com.tianyoukeji.parent.common;

import java.util.Date;


/**
 * 	
 * @author Administrator
 *
 */
public class BusinessException extends RuntimeException {

	private int status;
	private String error;
	
	
	/**
	 * 	业务异常，业务异常的业务码，一般在1000-5000一个数字，自定义，1000以内保留
	 * @author Administrator
	 *
	 */
	public BusinessException(int status, String error) {
		super(error);
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
