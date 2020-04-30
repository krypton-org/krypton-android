package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class RecoverPasswordTest {
    @Test
    public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "recover.password@example.com";
        String password = "ex@mplePassword123";
        
        try {
        	client.register(email, password);
        	boolean recovered = client.recoverPassword(email);
            assertFalse(recovered);
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
        	client.delete(password);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
    }

}
