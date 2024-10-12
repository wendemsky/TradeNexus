package com.marshals.restcontroller;

public class DatabaseRequestResult {
	private int rowCount;
	
	public DatabaseRequestResult () {}
	
	public DatabaseRequestResult(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getRowCount() {
		return rowCount;
	}
	
}
