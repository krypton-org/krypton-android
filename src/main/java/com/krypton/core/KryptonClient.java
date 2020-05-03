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
import java.util.ArrayList;
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
import com.krypton.core.internal.queries.PublicKeyQuery;
import com.krypton.core.internal.queries.Query;
import com.krypton.core.internal.queries.RefreshQuery;
import com.krypton.core.internal.queries.RegisterQuery;
import com.krypton.core.internal.queries.SendPasswordRecoveryQuery;
import com.krypton.core.internal.queries.SendVerificationEmailQuery;
import com.krypton.core.internal.queries.UpdateQuery;
import com.krypton.core.internal.queries.UserByIdsQuery;
import com.krypton.core.internal.queries.UserCountQuery;
import com.krypton.core.internal.queries.UserManyQuery;
import com.krypton.core.internal.queries.UserOneQuery;
import com.krypton.core.internal.queries.UserPaginationQuery;
import com.krypton.core.internal.utils.AuthData;
import com.krypton.core.internal.utils.DeleteData;
import com.krypton.core.internal.utils.EmailAvailableData;
import com.krypton.core.internal.utils.LoginData;
import com.krypton.core.internal.utils.PublicKeyData;
import com.krypton.core.internal.utils.QueryData;
import com.krypton.core.internal.utils.RefreshData;
import com.krypton.core.internal.utils.RegisterData;
import com.krypton.core.internal.utils.SendPasswordRecoveryData;
import com.krypton.core.internal.utils.SendVerificationEmailData;
import com.krypton.core.internal.utils.StringData;
import com.krypton.core.internal.utils.UpdateData;
import com.krypton.core.internal.utils.UserCountData;
import com.krypton.core.internal.utils.UserManyData;
import com.krypton.core.internal.utils.UserOneData;
import com.krypton.core.internal.utils.UserPaginationData;

public class KryptonClient {
	private String endpoint;
	private Date expiryDate;
	private String token;
	private Map<String, Object> user;
	private static final String COOKIES_HEADER = "Set-Cookie";
	private CookieManager cookieManager;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final int DELTA_TIME = 120000;

	public KryptonClient(String endpoint) {
		this.endpoint = endpoint;
		this.token = "";
		this.user = Collections.emptyMap();
		this.expiryDate = new Date(0);
		this.cookieManager = new CookieManager();
	}

	public Object getUser() {
		return this.user;
	}

	public String getToken() throws Exception {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		Date currentDate = localDateFormat.parse(simpleDateFormat.format(new Date()));
		if (this.token != null && this.expiryDate != null
				&& this.expiryDate.getTime() < currentDate.getTime() + DELTA_TIME) {
			this.refreshToken();
		}
		return this.token;
	}

	public Date getExpiryDate() {
		return this.expiryDate;
	}

	public String getAuthorizationHeader() throws Exception {
		return "Bearer " + this.getToken();
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

	public Map<String, ?> query(Query q, boolean isAuthTokenRequired, boolean isRefreshed) throws Exception {
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
			res = convertData(q, response);

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
			String expiryDatesrt = ((AuthData) res).getExpiryDate();
			this.expiryDate = DATE_FORMAT.parse(expiryDatesrt);
			this.saveCookies(req);
			this.decodeToken(token);
		}
		req.disconnect();
		return res.getData();
	}

	private QueryData convertData(Query q, StringBuilder response) {
		QueryData res;
		if (q instanceof RefreshQuery) {
			res = new Gson().fromJson(response.toString(), RefreshData.class);

		} else if (q instanceof LoginQuery) {
			res = new Gson().fromJson(response.toString(), LoginData.class);

		} else if (q instanceof UpdateQuery) {
			res = new Gson().fromJson(response.toString(), UpdateData.class);

		} else if (q instanceof DeleteQuery) {
			res = new Gson().fromJson(response.toString(), DeleteData.class);

		} else if (q instanceof EmailAvailableQuery) {
			res = new Gson().fromJson(response.toString(), EmailAvailableData.class);

		} else if (q instanceof RegisterQuery) {
			res = new Gson().fromJson(response.toString(), RegisterData.class);

		} else if (q instanceof SendPasswordRecoveryQuery) {
			res = new Gson().fromJson(response.toString(), SendPasswordRecoveryData.class);

		} else if (q instanceof SendVerificationEmailQuery) {
			res = new Gson().fromJson(response.toString(), SendVerificationEmailData.class);

		} else if (q instanceof UserOneQuery) {
			res = new Gson().fromJson(response.toString(), UserOneData.class);

		} else if (q instanceof UserManyQuery) {
			res = new Gson().fromJson(response.toString(), UserManyData.class);

		} else if (q instanceof PublicKeyQuery) {
			res = new Gson().fromJson(response.toString(), PublicKeyData.class);

		} else if (q instanceof UserCountQuery) {
			res = new Gson().fromJson(response.toString(), UserCountData.class);

		} else if (q instanceof UserPaginationQuery) {
			res = new Gson().fromJson(response.toString(), UserPaginationData.class);

		} else {
			res = new Gson().fromJson(response.toString(), StringData.class);
		}
		return res;
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

	public boolean isLoggedIn() throws ParseException {
		if (this.expiryDate == null) {
			return false;
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
		Date currentDate = localDateFormat.parse(simpleDateFormat.format(new Date()));
		if (this.token != null && this.expiryDate != null && this.expiryDate.getTime() > currentDate.getTime()) {
			return true;
		} else {
			try {
				this.refreshToken();
			} catch (Exception err) {
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

	public Map<String, Object> update(Map<String, Object> fields) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("fields", fields);
		this.query(new UpdateQuery(parameters), true, false);
		return this.user;
	}

	public boolean delete(String password) throws Exception {
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("password", password);
		Map<String, ?> res = this.query(new DeleteQuery(parameters), true, false);
		this.token = "";
		this.user = Collections.emptyMap();
		this.expiryDate = new Date(0);
		this.cookieManager = new CookieManager();
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
		Map<String, ?> res = this.query(new EmailAvailableQuery(parameters), false, false);
		return (boolean) res.get("emailAvailable");
	}

	public boolean changePassword(String actualPassword, String newPassword) throws Exception {
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put("password", newPassword);
		fields.put("previousPassword", actualPassword);
		this.update(fields);
		return true;
	}

	public boolean sendVerificationEmail() throws Exception {
		Map<String, ?> res = this.query(new SendVerificationEmailQuery(), true, false);
		return (boolean) res.get("sendVerificationEmail");
	}

	public Map<String, Object> fetchUserOne(HashMap<String, Object> filter, String[] requestedFields) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("filter", filter);
		Map<String, ?> res = this.query(new UserOneQuery(parameter, requestedFields), false, false);
		return (Map<String, Object>) res.get("userOne");
	}

	public void fetchUserByIds(ArrayList<String> data, String[] requestedFields) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("ids", data);
		this.query(new UserByIdsQuery(parameter, requestedFields), false, false);
	}

	public Map[] fetchUserMany(HashMap<String, Object> filter, String[] requestedFields, int limit) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("filter", filter);
		parameter.put("limit", limit);
		Map<String, ?> res = this.query(new UserManyQuery(parameter, requestedFields), false, false);
		return (Map[]) res.get("userMany");
	}

	public Map[] fetchUserMany(HashMap<String, Object> filter, String[] requestedFields) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("filter", filter);
		Map<String, ?> res = this.query(new UserManyQuery(parameter, requestedFields), false, false);
		return (Map[]) res.get("userMany");
	}

	public int fetchUserCount(HashMap<String, Object> filter) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("filter", filter);
		return (int) this.query(new UserCountQuery(parameter), false, false).get("userCount");
	}

	public int fetchUserCount() throws Exception {
		return (int) this.query(new UserCountQuery(), false, false).get("userCount");
	}

	public Map<String, Object> fetchUserWithPagination(HashMap<String, Object> filter, String[] requestedFields,
			int page, int perPage) throws Exception {
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("filter", filter);
		parameter.put("page", page);
		parameter.put("perPage", perPage);
		return (Map<String, Object>) this.query(new UserPaginationQuery(parameter, requestedFields), false, false)
				.get("userPagination");
	}

	public String publicKey() throws Exception {
		Map<String, ?> res = this.query(new PublicKeyQuery(), false, false);
		return (String) res.get("publicKey");
	}

	private void decodeToken(String token) {
		byte[] decodedBytes = Base64.getDecoder().decode(token.split("[.]")[1]);
		String decodedtoken = new String(decodedBytes);
		user = new Gson().fromJson(decodedtoken, Map.class);
	}
}
