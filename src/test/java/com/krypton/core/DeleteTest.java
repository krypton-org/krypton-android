package com.krypton.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DeleteTest {
	@Test
    public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "testregister@example.com";
        String password = "ex@mplePassword123";
        
        try {
            client.register(email, password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            client.login(email, password);
            assertTrue(client.isLoggedIn());
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
        	boolean isDeleted = client.delete(password);
        	assertFalse(client.isLoggedIn());
        	assertTrue(isDeleted);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
    }
}
