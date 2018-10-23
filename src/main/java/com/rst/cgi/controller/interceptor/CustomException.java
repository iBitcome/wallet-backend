package com.rst.cgi.controller.interceptor;

import com.rst.cgi.common.constant.Error;

/**
 * @author hujia
 */
public class CustomException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5536093022350862167L;
	
	private Error error;

	public CustomException(Error error) {
		super(error.getMsg());
		this.error = error;
	}

	public Error getError() {
		return error;
	}

	public static void response(Error error) {
		throw new CustomException(error);
	}

	public static void response(String msg) {
		throw new CustomException(Error.make(-1, msg));
	}

	public static void response(int code, String msg) {
		throw new CustomException(Error.make(code, msg));
	}
}
