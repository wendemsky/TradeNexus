package com.marshals.integration;

public class FIPSException extends RuntimeException {

	private static final long serialVersionUID = 2L;

	public FIPSException() {
	}

	public FIPSException(String message) {
		super(message);
	}

	public FIPSException(Throwable cause) {
		super(cause);
	}

	public FIPSException(String message, Throwable cause) {
		super(message, cause);
	}

	protected FIPSException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
