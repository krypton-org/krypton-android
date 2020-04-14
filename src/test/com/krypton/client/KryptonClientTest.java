package test.com.krypton.client;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import main.java.com.krypton.client.KryptonClient;

public class KryptonClientTest {
	@Test
	public void test1() {
		KryptonClient client = new KryptonClient("https://nusid.net/krypton-auth");
		try {
			client.register("toto@toto.com", "totototo");
		} catch (IOException e) {
			fail();
		}
		try {
			client.login("toto@toto.com", "totototo");
		} catch (IOException e) {
			fail();
		}
	}
}
