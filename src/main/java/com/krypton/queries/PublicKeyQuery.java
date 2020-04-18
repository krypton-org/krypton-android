package com.krypton.queries;

import java.util.HashMap;

public class PublicKeyQuery extends Query {
	
	public PublicKeyQuery() {
		this.getQuery();
	}
	public void getQuery() {	
		StringBuilder sb = new StringBuilder();
		sb.append("query {")
		  .append("publicKey")
		  .append("}");
		this.query = sb.toString();
	}

}
