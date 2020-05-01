package com.krypton.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;

public class DeleteTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "deleteTest@example.com";
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() throws Exception {
		try {
			client.register(email, password);

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testDeleteUser() {
		try {
			client.login(email, password);
			assertTrue(client.isLoggedIn());
			boolean isDeleted = client.delete(password);
			assertFalse(client.isLoggedIn());
			assertTrue(isDeleted);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
