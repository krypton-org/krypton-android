package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class SendVerificationEmailTest {
	@Test
    public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "send.verification.email@example.com";
        String password = "ex@mplePassword123";
        
        try {
        	client.register(email, password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            client.login(email, password);
            assertTrue(client.sendVerificationEmail());
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
