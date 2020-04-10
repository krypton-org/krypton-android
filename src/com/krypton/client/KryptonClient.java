package com.krypton.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.krypton.queries.LoginQuery;

public class KryptonClient {
	private String endpoint;

	public KryptonClient(String endpoint) {
		this.endpoint = endpoint;
	}

	public void login(String email, String password) throws IOException {
		URL url = new URL(endpoint);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		parameters.put("password", password);
		LoginQuery query = new LoginQuery(parameters);
		String jsonInputString = query.toJson();
		con.setDoOutput(true);
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes();
			os.write(input, 0, input.length);
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			System.out.println(response.toString());
		}
		con.disconnect();
	}
	/*
	 * def __init__(self, endpoint: str): self.endpoint = endpoint self.session =
	 * requests.Session() self.token = None
	 * 
	 * def __post(self, **kwargs): if self.token:
	 * self.session.headers.update({"Authorization": self.token.header}) res =
	 * self.session.post(self.endpoint, **kwargs) return dict(res.json())
	 * 
	 * def __query(self, q): res = self.__post(json=q.to_dict())
	 * 
	 * if "errors" in res: error = res["errors"][0] raise KryptonException(error)
	 * 
	 * # We *must* have data if there is no errors. data = res["data"]
	 * 
	 * token = ( data.get("login", {}) or data.get("refreshToken", {}) or
	 * data.get("updateMe", {}) ).get("token")
	 * 
	 * if token: self.token = UserToken.from_token(token)
	 * 
	 * return data
	 * 
	 * def query(self, q): try: result = self.__query(q) except UnauthorizedError:
	 * self.refresh() result = self.__query(q) return result
	 * 
	 * def refresh(self): self.__query(RefreshQuery())
	 * 
	 * def register(self, email, password, **kwargs): fields = {"email": email,
	 * "password": password, **kwargs} self.query(RegisterQuery(fields=fields))
	 * 
	 * def login(self, email, password): self.query(LoginQuery(email=email,
	 * password=password))
	 * 
	 * def update(self, **kwargs): self.query(UpdateQuery(fields=kwargs))
	 * 
	 * def delete(self, password): self.query(DeleteQuery(password=password))
	 */
}
