
package com.krypton.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class ChangePasswordTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "changePassword@example.com";
	static String password = "ex@mplePassword123";
	static String newPassword = "0therPassword123";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testChangePassword() throws Exception {

		client.login(email, password).get();
		assertTrue(client.isLoggedIn().get());
		assertTrue(client.changePassword(password, newPassword).get());

	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password).get();
			client.delete(password).get();
		} catch (Exception e) {
			;
		}
		try {
			client.login(email, newPassword).get();
			client.delete(newPassword).get();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}