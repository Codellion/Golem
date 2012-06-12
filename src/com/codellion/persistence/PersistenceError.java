package com.codellion.persistence;

import org.springframework.dao.DataAccessException;

public class PersistenceError extends DataAccessException {

	private static final long serialVersionUID = 1600005244070516106L;

	public PersistenceError(String msg) {
		super(msg);
	}

	public PersistenceError(String msg, Throwable th) {
		super(msg, th);
	}
}
