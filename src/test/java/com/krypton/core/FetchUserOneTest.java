//package com.krypton.core;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//public class FetchUserOneTest {
//	
//	KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
//	String password = "ex@mplePassword123";
//	String email = "fetchuser@test.com";
//	
//	@BeforeAll
//	public void setUp() {
//		for (int i = 1; i <= 5; i++) {
//			try {
//				boolean isRegistered = client.register("fetchuser" + String.valueOf(i) + "@example.com", password);
//				assertTrue(isRegistered);
//			} catch (Exception e) {
//				System.out.println(e);
//				fail(e);
//			}
//		}
//	}
//
//	@Test
//	public void testFetchUser() {
//	
//	}
//	
//	@AfterAll
//	public void cleanUp() {
//		try {
//			client.login(email, password);
//		} catch (Exception e) {
//			System.out.println(e);
//			fail(e);
//		}
//		try {
//			client.delete(password);
//		} catch (Exception e) {
//			System.out.println(e);
//			fail(e);
//		}
//	}
//}
