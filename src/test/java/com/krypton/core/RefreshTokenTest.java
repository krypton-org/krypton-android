package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class RefreshTokenTest {
	 @Test
	    public void test1() {
	        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	        String email = "refreshtoken@example.com";
	        String password = "ex@mplePassword123";
	        
	        try {
	        	client.register(email, password);
	        } catch (Exception e) {
	        	System.out.println(e);
	            fail(e);
	        }
	        try {
	            Map<String, Object> res = client.login(email, password);
	            assertEquals(res.get("email"), email);
	            Date beforeUpdateDate = client.getExpiryDate();
	            
	            client.refreshToken();
	            assertTrue(beforeUpdateDate.getTime() > client.getExpiryDate().getTime() );
	            
	        } catch (Exception e) {
	        	System.out.println(e);
	            fail(e);
	        }
	        try {
	        	client.login(email, password);
	        	client.delete(password);
	        } catch (Exception e) {
	        	System.out.println(e);
	            fail(e);
	        }
	    }
}
