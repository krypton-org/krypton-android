/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.krypton.core;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.AfterAll;

public class RegisterTest {
	static KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
	static String email = "register.test12345@example.com";
	static String password = "ex@mplePassword123";

	@Test
	public void testRegister() throws Exception {

		client.register(email, password).get();
		
		try {
			client.register(email, password).get();
		} catch(Exception err) {
			err.printStackTrace();
		}

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