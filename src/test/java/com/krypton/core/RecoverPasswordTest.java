package com.krypton.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RecoverPasswordTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "recover.password@example.com";
	static String password = "ex@mplePassword123";

	@BeforeAll
	public static void setUp() {
		try {
			client.register(email, password).get();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Test
	public void testRecoverPassword() throws Exception {
		boolean recovered = client.recoverPassword(email).get();
		assertTrue(recovered);

	}

	@AfterAll
	public static void cleanUp() {
		try {
			client.login(email, password).get();
			client.delete(password).get();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
