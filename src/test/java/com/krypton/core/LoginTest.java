package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class LoginTest {
	@Test
	public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "user1@example.com";
        String password = "ex@mplePassword123";
        try {
        	client.register(email, password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
        	assertFalse(client.isLoggedIn());
            Map<String, Object> res = client.login(email, password);
            assertEquals(res.get("email"),email);
            assertNotNull(res.get("_id"));
            assertFalse((boolean) res.get("verified"));	
            assertTrue(client.isLoggedIn());
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
