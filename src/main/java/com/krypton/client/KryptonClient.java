package main.java.com.krypton.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import main.java.com.krypton.queries.*;

public class KryptonClient {
	private String endpoint;
	private Date expiryDate;
	private String token;
	private UserToken user;
	private static final String COOKIES_HEADER = "Set-Cookie";
	private CookieManager cookieManager = new CookieManager();
	
	public KryptonClient(String endpoint) {
		this.endpoint = endpoint;
		this.token ="";
		this.expiryDate = new Date();
	}
	
	public Object getUser() {
		return this.user;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public String getAuthorizationHeader() {
		return "Bearer"+this.token;
	}
	
	public Map<String, Object> query(Query q, boolean isAuthTokenRequired) throws IOException {
		return query(q, isAuthTokenRequired, false);
	}
	
	private void saveCookies(HttpURLConnection req) {
		Map<String, List<String>> headerFields = req.getHeaderFields();
		List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

		if (cookiesHeader != null) {
		    for (String cookie : cookiesHeader) {
		        cookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
		    }               
		}
		
	}
	
	private void saveToken(Data res, String mutationName) {
		Map<String, Map<String, Object>> data = res.data;
		Map<String, Object> login = data.get(mutationName);
		token= (String) login.get("token");
	}
	
	public Map<String, Object> query(Query q, boolean isAuthTokenRequired, boolean isRefreshed ) throws IOException {
		URL url = new URL(endpoint);
		HttpURLConnection req = (HttpURLConnection) url.openConnection();
		req.setRequestMethod("POST");
		req.setRequestProperty("Content-Type", "application/json");
		req.setRequestProperty("Accept", "application/json");
		Data res;
		if (isAuthTokenRequired) {
			req.setRequestProperty("Authorization", this.getAuthorizationHeader());
		}
		if (cookieManager.getCookieStore().getCookies().size() > 0) {
		    addCookies(req);
		}
		req.setDoOutput(true);
		String jsonInputString = q.toJson();
		try (OutputStream os = req.getOutputStream()) {
			byte[] input = jsonInputString.getBytes();
			os.write(input, 0, input.length);
		}
		try (BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			res = new Gson().fromJson(response.toString(), Data.class);
		}
		
		if (q instanceof LoginQuery) {
//			{
//				data: {
//					login:{
//						token: "",
//						expiryDate: "",
//					}
//				}
//			}
			
			
//			{
//				errors: [
//				  {
//				       type: "UserNotFoundError"
//				  }
//				]
//			}
			this.saveToken(res, "login");
			this.saveCookies(req);
			this.setState(token);
			System.out.print(user.getUser());
		} else if (q instanceof UpdateQuery) {
			this.saveToken(res,"updateMe");
			this.saveCookies(req);
		} else if (q instanceof RefreshQuery) {
			this.saveToken(res,"refreshToken");
			this.saveCookies(req);
		}
		
		req.disconnect();
		return null;
	}
	
	public class Data {
	    public Map<String, Map<String, Object>> data;
	    
	    public List<Map<String, String>> errors;

	}

	private void addCookies(HttpURLConnection req) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for(HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
			if (isFirst) {
				isFirst = false;
			} else {
		    	sb.append(";");
			}
			sb.append(cookie.toString());
		} 		
		req.setRequestProperty("Cookie", sb.toString());
	}

//	public void login(String email, String password) throws IOException {
//		URL url = new URL(endpoint);
//		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//		con.setRequestMethod("POST");
//		con.setRequestProperty("Content-Type", "application/json");
//		con.setRequestProperty("Accept", "application/json");
//		HashMap<String, Object> parameters = new HashMap<String, Object>();
//		parameters.put("email", email);
//		parameters.put("password", password);
//		LoginQuery query = new LoginQuery(parameters);
//		String jsonInputString = query.toJson();
//		con.setDoOutput(true);
//		try (OutputStream os = con.getOutputStream()) {
//			byte[] input = jsonInputString.getBytes();
//			os.write(input, 0, input.length);
//		}
//
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
//			StringBuilder response = new StringBuilder();
//			String responseLine = null;
//			while ((responseLine = br.readLine()) != null) {
//				response.append(responseLine.trim());
//			}
//			System.out.println(response.toString());
//		}
//		con.disconnect();
//	}
	public void refreshToken() throws IOException {
		this.query(new RefreshQuery(),false, true);
	}
	
	public void register(String email, String password) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		parameters.put("password", password);
		this.query(new RegisterQuery(parameters),false, false);
	}
	
	public void login(String email, String password) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		parameters.put("password", password);
		this.query(new LoginQuery(parameters),false,false);
	}
	
	public void delete(String password) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("password", password);
		this.query(new DeleteQuery(parameters),true,false);
	}
	
	public void recoverPassword(String email) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		this.query(new UpdateQuery(parameters),true,false);
	}
	
	public void emailAvailable(String email) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		this.query(new EmailAvailableQuery(parameters),false,false);
	}
	
	public void changePassword(String password, String previousPassword) throws IOException {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("password", password);
		parameters.put("PreviousPassword", previousPassword);
		this.query(new UpdateQuery(parameters),true ,false);
	}
	
	public void sendVerificationEmail() throws IOException {
		this.query(new SendVerificationEmailQuery(),true,false);
	}
	
//	public void fetchUserOne() throws IOException {
//		this.query(new UserOneQuery(parameters),true,false);
//	}
//	
//	public void fetchUserByIds(String email) throws IOException {
//		this.query(new UserByIdsQuery(parameters),true,false);
//	}
//	
//	public void fetchUserMany(String email) throws IOException {
//		this.query(new UserManyQuery(parameters),true,false);
//	}
//	
//	public void fetchUserCount(String email) throws IOException {
//		this.query(new UserCountQuery(parameters),true,false);
//	}
//	
//	public void fetchUserWithPagination(String email) throws IOException {
//		this.query(new UserPaginationQuery(parameters),false,false);
//	}
//	
//	public void publicKey() throws IOException {
//		this.query(new PublicKeyQuery(),true,false);
//	}
//	
	
	private void setState(String token) {
		this.user= UserToken.fromToken(token);
	}
}
