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
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testDeleteUser() throws Exception {

		client.login(email, password).get();
		assertTrue(client.isLoggedIn().get());
		client.delete(password).get();
		assertFalse(client.isLoggedIn().get());

	}
}
