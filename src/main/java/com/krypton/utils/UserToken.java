package com.krypton.utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
public class UserToken {
	private DecodedJWT user;
	private String token;
	
	
	public UserToken(DecodedJWT user, String token) {
		this.user=user;
		this.token=token;
	}
	
	public DecodedJWT getUser(){
		return this.user;
	}
	
	public static UserToken fromToken(String token) {
		DecodedJWT user=JWT.decode(token);
		return new UserToken(user, token);
	}
}
