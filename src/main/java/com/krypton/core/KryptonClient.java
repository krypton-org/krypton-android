package com.krypton.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.krypton.core.internal.exceptions.AlreadyLoggedInException;
import com.krypton.core.internal.exceptions.EmailAlreadyConfirmedException;
import com.krypton.core.internal.exceptions.EmailAlreadyExistsException;
import com.krypton.core.internal.exceptions.EmailNotSentException;
import com.krypton.core.internal.exceptions.GraphQLException;
import com.krypton.core.internal.exceptions.KryptonException;
import com.krypton.core.internal.exceptions.UnauthorizedException;
import com.krypton.core.internal.exceptions.UpdatePasswordTooLateException;
import com.krypton.core.internal.exceptions.UserNotFoundException;
import com.krypton.core.internal.exceptions.UserValidationException;
import com.krypton.core.internal.exceptions.UsernameAlreadyExistsException;
import com.krypton.core.internal.exceptions.WrongPasswordException;
import com.krypton.core.internal.queries.DeleteQuery;
import com.krypton.core.internal.queries.EmailAvailableQuery;
import com.krypton.core.internal.queries.LoginQuery;
import com.krypton.core.internal.queries.Query;
import com.krypton.core.internal.queries.RefreshQuery;
import com.krypton.core.internal.queries.RegisterQuery;
import com.krypton.core.internal.queries.SendPasswordRecoveryQuery;
import com.krypton.core.internal.queries.SendVerificationEmailQuery;
import com.krypton.core.internal.queries.UpdateQuery;
import com.krypton.core.internal.utils.StringData;
import com.krypton.core.internal.utils.UpdateData;
import com.krypton.core.internal.utils.AuthData;
import com.krypton.core.internal.utils.BooleanData;
import com.krypton.core.internal.utils.LoginData;
import com.krypton.core.internal.utils.QueryData;
import com.krypton.core.internal.utils.RefreshData;

public class KryptonClient {
	private String endpoint;
	private Date expiryDate;
	private String token;
	private Map<String, Object> user;
	private static final String COOKIES_HEADER = "Set-Cookie";
	private CookieManager cookieManager = new CookieManager();
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	public KryptonClient(String endpoint) {
		this.endpoint = endpoint;
		this.token = "";
	}

	public Object getUser() {
		return this.user;
	}

	public String getToken() {
		return this.token;
	}

	public Date getExpiryDate() {
		return this.expiryDate;
	}

	public String getAuthorizationHeader() {
		return "Bearer " + this.token;
	}

	public Map<String, ?> query(Query q, boolean isAuthTokenRequired) throws Exception {
		return query(q, isAuthTokenRequired, false);
	}

	private void saveCookies(HttpURLConnection req) {
		Map<String, List<String>> headerFields = req.getHeaderFields();
		List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

		if (cookiesHeader != null) {
			for (String cookie : cookiesHeader) {
				cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
			}
		}

	}


	public Map<String, ?> query(Query q, boolean isAuthTokenRequired, boolean isRefreshed)
			throws Exception {
		URL url = new URL(endpoint);
		HttpURLConnection req = (HttpURLConnection) url.openConnection();
		req.setRequestMethod("POST");
		req.setRequestProperty("Content-Type", "application/json");
		req.setRequestProperty("Accept", "application/json");
		QueryData res = null;
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
			if (q instanceof RefreshQuery) {
				res = new Gson().fromJson(response.toString(), RefreshData.class);
			}
			else if (q instanceof LoginQuery) {
				res = new Gson().fromJson(response.toString(), LoginData.class);
			}
			else if (q instanceof UpdateQuery) {
				res = new Gson().fromJson(response.toString(), UpdateData.class);
			}
			else if (q instanceof DeleteQuery || q instanceof RegisterQuery || q instanceof SendPasswordRecoveryQuery || q instanceof EmailAvailableQuery || q instanceof SendVerificationEmailQuery ) {
				res = new Gson().fromJson(response.toString(), BooleanData.class);
			}
			else {
				res = new Gson().fromJson(response.toString(), StringData.class);
			}
			
		} catch (Exception err) {
			System.out.println(err);
		}
		
		if (res.getErrors() != null && res.getErrors().size() > 0) {
			String errorType = res.getErrors().get(0).get("type");
			String message = res.getErrors().get(0).get("message");
			if (errorType == "UnauthorizedError" && !isRefreshed) {
				this.refreshToken();
				return this.query(q, isAuthTokenRequired, true);
			} else {
				throw errorStringToException(errorType, message);
			}
		}
		if (res instanceof AuthData) {
			this.token = ((AuthData) res).getToken();
			String expiryDatesrt= ((AuthData) res).getExpiryDate();
			this.expiryDate = DATE_FORMAT.parse(expiryDatesrt);
			this.saveCookies(req);
			this.decodeToken(token);
		}
		req.disconnect();
		return res.getData();
	}


	private KryptonException errorStringToException(String errorType, String message) {
		switch (errorType) {
		case "AlreadyLoggedInError":
			return new AlreadyLoggedInException(message);
		case "EmailAlreadyConfirmedError":
			return new EmailAlreadyConfirmedException(message);
		case "EmailAlreadyExistsError":
			return new EmailAlreadyExistsException(message);
		case "EmailNotSentError":
			return new EmailNotSentException(message);
		case "GraphQLError":
			return new GraphQLException(message);
		case "UpdatePasswordTooLateError":
			return new UpdatePasswordTooLateException(message);
		case "UsernameAlreadyExistsError":
			return new UsernameAlreadyExistsException(message);
		case "UnauthorizedError":
			return new UnauthorizedException(message);
		case "UserNotFoundError":
			return new UserNotFoundException(message);
		case "UserValidationError":
			return new UserValidationException(message);
		case "WrongPasswordError":
			return new WrongPasswordException(message);
		default:
			return new KryptonException(message);
		}	
	}


	private void addCookies(HttpURLConnection req) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(";");
			}
			sb.append(cookie.toString());
		}
		req.setRequestProperty("Cookie", sb.toString());
	}

	public void refreshToken() throws Exception {
		this.query(new RefreshQuery(), false, true);
	}
	
	public boolean isLoggedIn () throws ParseException {
		System.out.println(this.expiryDate.getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    String utcTime = sdf.format(new Date().getTime());
	    System.out.println(utcTime);
		if (this.token != null && this.expiryDate != null ) {
			return true;
		}
		else {
			try {
				this.refreshToken();
			}
			catch (Exception err) {
				return false;
			}
			return true;
		}
	}

	public boolean register(String email, String password, Map<String, Object> otherFields) throws Exception {
		if (otherFields == null) {
			otherFields = Collections.emptyMap();
		}
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HashMap<String, Object> fields = new HashMap<String, Object>(otherFields);
		fields.put("email", email);
		fields.put("password", password);
		parameters.put("fields", fields);
		Map<String, ?> res = this.query(new RegisterQuery(parameters), false, false);
		return (boolean) res.get("register");
	}

	public boolean register(String email, String password) throws Exception {
		Map<String, Object> empty = Collections.emptyMap();
		return this.register(email, password, empty);
	}

	public Map<String, Object> login(String email, String password) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		parameters.put("password", password);
		this.query(new LoginQuery(parameters), false, false);
		return this.user;
	}

	public Map<String, Object> update(Map<String, Object> otherFields) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HashMap<String, Object> fields = new HashMap<String, Object>(otherFields);
		parameters.put("fields", fields);
		this.query(new UpdateQuery(parameters), true, false);
		return this.user;
	}

	public boolean delete(String password) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("password", password);
		Map<String, ?> res = this.query(new DeleteQuery(parameters), true, false);
		return (boolean) res.get("deleteMe");
	}

	public boolean recoverPassword(String email) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		Map<String, ?> res = this.query(new SendPasswordRecoveryQuery(parameters), true, false);
		return (boolean) res.get("sendPasswordRecoveryEmail");
	}

	public boolean isEmailAvailable(String email) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("email", email);
		Map<String, ?> res =this.query(new EmailAvailableQuery(parameters), false, false);
		return (boolean) res.get("emailAvailable");
	}

	public void changePassword(String password, String previousPassword) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put("password", password);
		fields.put("PreviousPassword", previousPassword);
		parameters.put("fields", fields);
		this.query(new UpdateQuery(parameters), true, false);
	}

	public boolean sendVerificationEmail() throws Exception {
		Map<String, ?> res = this.query(new SendVerificationEmailQuery(), true, false);
		return (boolean) res.get("sendVerificationEmail");
	}

//	 public void fetchUserOne() throws Exception {
//	 this.query(new UserOneQuery(parameters),true,false);
//	 }
//	
//	 public void fetchUserByIds(String email) throws Exception {
//	 this.query(new UserByIdsQuery(parameters),true,false);
//	 }
//	
//	 public void fetchUserMany(String email) throws Exception {
//	 this.query(new UserManyQuery(parameters),true,false);
//	 }
//	
//	 public void fetchUserCount(String email) throws Exception {
//	 this.query(new UserCountQuery(parameters),true,false);
//	 }
//	
//	 public void fetchUserWithPagination(String email) throws Exception {
//	 this.query(new UserPaginationQuery(parameters),false,false);
//	 }
//	
//	 public void publicKey() throws Exception {
//	 this.query(new PublicKeyQuery(),true,false);
//	 }
	

	private void decodeToken(String token) {
		byte[] decodedBytes = Base64.getDecoder().decode(token.split("[.]")[1]);
		String decodedtoken = new String(decodedBytes);
		user = new Gson().fromJson(decodedtoken, Map.class);
	}
}
