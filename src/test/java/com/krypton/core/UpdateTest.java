package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class UpdateTest {
	@Test
    public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "update1@example.com";
        String password = "ex@mplePassword123";
        String emailUpdate = "update2@example.com";
        HashMap<String, Object> change = new HashMap<String, Object>();
        change.put("email", emailUpdate);
        try {
        	client.register(email, password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            Map<String, Object> res = client.login(email, password);
            assertEquals(res.get("email"),email);
            Date beforeUpdateDate = client.getExpiryDate();
            
            res = client.update(change);
            assertEquals(res.get("email"),emailUpdate);
            assertTrue(beforeUpdateDate.getTime() > client.getExpiryDate().getTime() );
	           
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
        	client.delete(password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
    }
}
