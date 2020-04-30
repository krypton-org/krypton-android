
package com.krypton.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ChangePasswordTest {
    @Test
    public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "changePassword@example.com";
        String password = "ex@mplePassword123";
        String newPassword = "0therPassword123";
        
        try {
            boolean isRegistered = client.register(email, password);
            assertTrue(isRegistered);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            client.login(email, password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        
        try {
            client.changePassword(password, newPassword);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            client.login(email, newPassword);
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