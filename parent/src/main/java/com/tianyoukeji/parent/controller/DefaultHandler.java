package com.tianyoukeji.parent.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.tianyoukeji.parent.common.BusinessException;


public abstract class DefaultHandler {

	@ExceptionHandler(TransactionSystemException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object validatorHandler(TransactionSystemException ex) {
		Throwable rootCause = ex.getRootCause();
		if (rootCause instanceof ConstraintViolationException) {
			ConstraintViolationException rootEx = (ConstraintViolationException) rootCause;
			Set<ConstraintViolation<?>> constraintViolations = rootEx.getConstraintViolations();
			HashMap<String, String> errors = new HashMap<String, String>();
			Response response = null ;
			if (!constraintViolations.isEmpty()) {
				for (ConstraintViolation<?> constraint : constraintViolations) {
					response = new Response(new BusinessException(1500, constraint.getMessage()));
				}
			}
			return response;
		}else {
			ex.printStackTrace();
			return null;
		}
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object ExceptionHandler(DataIntegrityViolationException ex) {
		BusinessException business503Exception = new BusinessException(1503,"唯一索引不允许重复");
		ex.printStackTrace();
		Response response = new Response(business503Exception);
		return response;
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object ExceptionHandler(IllegalArgumentException ex) {
		BusinessException business503Exception;
		if( ex.getCause() instanceof UnrecognizedPropertyException) {
			 business503Exception = new BusinessException(1503,((UnrecognizedPropertyException)ex.getCause()).getPropertyName()+" 字段不存在");			
		}else {
			 business503Exception = new BusinessException(1503," 字段错误");	
		}
		ex.printStackTrace();
		Response response = new Response(business503Exception);
		return response;
	}
	
	@ExceptionHandler(BusinessException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object BusinessExceptionHandler(BusinessException ex) {
		ex.printStackTrace();
		Response response = new Response(ex);
		return response;
	}
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object ExceptionHandler(Exception ex) {
		ex.printStackTrace();
		Response response = new Response(ex);
		return response;
	}

	public static class Response {

		private int status;
		private String error;
		@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
		private Date timestamp;
		private String message;

		public Response(BusinessException ex) {
			this.status = ex.getStatus();
			this.error = ex.getError();
			this.timestamp = new Date();
			this.message = ex.getMessage();
		}

		public Response(Exception ex) {
			this.status = 500;
			this.error = "内部错误";
			this.timestamp = new Date();
			this.message = ex.getMessage();
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

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}
}
