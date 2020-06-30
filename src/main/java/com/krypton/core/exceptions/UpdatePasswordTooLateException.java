package com.krypton.core.exceptions;

public class UpdatePasswordTooLateException extends KryptonException {

	private static final long serialVersionUID = -386397477702347024L;

	public UpdatePasswordTooLateException(String message) {
		super(message);
	}

}
