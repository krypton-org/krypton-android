
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
			client.register(email, password);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testChangePassword() {

		try {
			client.login(email, password);
			assertTrue(client.isLoggedIn());
			assertTrue(client.changePassword(password, newPassword));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password);
			client.delete(password);
		} catch (Exception e) {
			;
		}
		try {
			client.login(email, newPassword);
			client.delete(newPassword);
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}