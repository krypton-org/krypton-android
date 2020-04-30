/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.krypton.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EmailAvailableTest {
	@Test
	public void test1() {
        KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
        String email = "testregqqsdissdqsdqsdter@exampqsdle.com";
        String password = "ex@mplePassword123";
        try {
            boolean isAvailable = client.isEmailAvailable(email);
            assertTrue(isAvailable);
        } catch (Exception e) {
        	System.out.println(e);
            fail(e);
        }
        try {
            boolean isRegistered = client.register(email, password);
            assertTrue(isRegistered);
            boolean isAvailable = client.isEmailAvailable(email);
            assertFalse(isAvailable);
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

