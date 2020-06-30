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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.krypton.core.data.AuthData;
import com.krypton.core.data.DeleteData;
import com.krypton.core.data.EmailAvailableData;
import com.krypton.core.data.GenericData;
import com.krypton.core.data.LoginData;
import com.krypton.core.data.PublicKeyData;
import com.krypton.core.data.QueryData;
import com.krypton.core.data.RefreshData;
import com.krypton.core.data.RegisterData;
import com.krypton.core.data.SendPasswordRecoveryData;
import com.krypton.core.data.SendVerificationEmailData;
import com.krypton.core.data.UpdateData;
import com.krypton.core.data.User;
import com.krypton.core.data.UserByIdsData;
import com.krypton.core.data.UserCountData;
import com.krypton.core.data.UserManyData;
import com.krypton.core.data.UserOneData;
import com.krypton.core.data.UserPaginationData;
import com.krypton.core.data.UserPaginationData.Pagination;
import com.krypton.core.exceptions.AlreadyLoggedInException;
import com.krypton.core.exceptions.EmailAlreadyConfirmedException;
import com.krypton.core.exceptions.EmailAlreadyExistsException;
import com.krypton.core.exceptions.EmailNotSentException;
import com.krypton.core.exceptions.GraphQLException;
import com.krypton.core.exceptions.KryptonException;
import com.krypton.core.exceptions.UnauthorizedException;
import com.krypton.core.exceptions.UpdatePasswordTooLateException;
import com.krypton.core.exceptions.UserNotFoundException;
import com.krypton.core.exceptions.UserValidationException;
import com.krypton.core.exceptions.UsernameAlreadyExistsException;
import com.krypton.core.exceptions.WrongPasswordException;
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

public class KryptonClient {
	private String endpoint;
	private Date expiryDate;
	private String token;
	private int minTimeToLive;
	private User user;
	private static final String COOKIES_HEADER = "Set-Cookie";
	private CookieManager cookieManager;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final int DEFAULT_MIN_TIME_TO_LIVE = 30 * 1000;
	private final ExecutorService executor = Executors.newWorkStealingPool();

	public KryptonClient(String endpoint, int minTimeToLive) {
		this.minTimeToLive = minTimeToLive;
		this.endpoint = endpoint;
		this.token = "";
		this.user = null;
		this.expiryDate = new Date(0);
		this.cookieManager = new CookieManager();
	}

	public KryptonClient(String endpoint) {
		this(endpoint, DEFAULT_MIN_TIME_TO_LIVE);
	}

	public Object getUser() {
		return this.user;
	}

	public Future<String> getToken() throws Exception {
		return executor.submit(new Callable<String>() {
			public String call() throws Exception {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				Date currentDate = localDateFormat.parse(simpleDateFormat.format(new Date()));
				if (KryptonClient.this.token != null && KryptonClient.this.expiryDate != null
						&& KryptonClient.this.expiryDate.getTime() < currentDate.getTime() + minTimeToLive) {
					KryptonClient.this.refreshToken().get();
				}
				return KryptonClient.this.token;

			}
		});
	}

	public Date getExpiryDate() {
		return this.expiryDate;
	}

	public Future<String> getAuthorizationHeader() throws Exception {
		return executor.submit(new Callable<String>() {
			public String call() throws Exception {
				return "Bearer " + KryptonClient.this.getToken().get();
			}
		});
	}

	private QueryData query(Query q, boolean isAuthTokenRequired) throws Exception {
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

	private QueryData query(Query q, boolean isAuthTokenRequired, boolean isRefreshed) throws Exception {
		URL url = new URL(endpoint);
		HttpURLConnection req = (HttpURLConnection) url.openConnection();
		req.setRequestMethod("POST");
		req.setRequestProperty("Content-Type", "application/json");
		req.setRequestProperty("Accept", "application/json");
		QueryData res = null;
		if (isAuthTokenRequired) {
			req.setRequestProperty("Authorization", KryptonClient.this.getAuthorizationHeader().get());
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
		}

		if (res.getErrors() != null && res.getErrors().size() > 0) {
			String errorType = res.getErrors().get(0).get("type");
			String message = res.getErrors().get(0).get("message");
			if (errorType == "UnauthorizedError" && !isRefreshed) {
				KryptonClient.this.refreshToken();
				return KryptonClient.this.query(q, isAuthTokenRequired, true);
			} else {
				throw errorStringToException(errorType, message);
			}
		}
		if (res instanceof AuthData) {
			KryptonClient.this.token = ((AuthData) res).getToken();
			String expiryDatesrt = ((AuthData) res).getExpiryDate();
			KryptonClient.this.expiryDate = DATE_FORMAT.parse(expiryDatesrt);
			KryptonClient.this.saveCookies(req);
			KryptonClient.this.decodeToken(token);
		}
		req.disconnect();
		return res;

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

		} else if (q instanceof UserByIdsQuery) {
			res = new Gson().fromJson(response.toString(), UserByIdsData.class);

		} else {
			res = new Gson().fromJson(response.toString(), GenericData.class);
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

	public Future<Void> refreshToken() throws Exception {
		return executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				KryptonClient.this.query(new RefreshQuery(), false, true);
				return null;
			}
		});
	}

	public Future<Boolean> isLoggedIn() throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				if (KryptonClient.this.expiryDate == null) {
					return false;
				}
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
				Date currentDate = localDateFormat.parse(simpleDateFormat.format(new Date()));
				if (KryptonClient.this.token != null && KryptonClient.this.expiryDate != null
						&& KryptonClient.this.expiryDate.getTime() > currentDate.getTime()) {
					return true;
				} else {
					try {
						KryptonClient.this.refreshToken().get();
					} catch (Exception err) {
						return false;
					}
					return true;
				}
			}
		});
	}

	public Future<Void> register(String email, String password, Map<String, Object> otherFields) throws Exception {
		Map<String, Object> otherFieldsNotNull;
		if (otherFields == null) {
			otherFieldsNotNull = Collections.emptyMap();
		} else {
			otherFieldsNotNull = otherFields;
		}
		return executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				HashMap<String, Object> fields = new HashMap<String, Object>(otherFieldsNotNull);
				fields.put("email", email);
				fields.put("password", password);
				parameters.put("fields", fields);
				KryptonClient.this.query(new RegisterQuery(parameters), false, false);
				return null;
			}
		});
	}

	public Future<Void> register(String email, String password) throws Exception {
		Map<String, Object> empty = Collections.emptyMap();
		return executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				return KryptonClient.this.register(email, password, empty).get();
			}
		});
	}

	public Future<User> login(String email, String password) throws Exception {
		return executor.submit(new Callable<User>() {
			public User call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("email", email);
				parameters.put("password", password);
				KryptonClient.this.query(new LoginQuery(parameters), false, false);
				return KryptonClient.this.user;
			}
		});
	}

	public Future<User> update(Map<String, Object> fields) throws Exception {
		return executor.submit(new Callable<User>() {
			public User call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("fields", fields);
				KryptonClient.this.query(new UpdateQuery(parameters), true, false);
				return KryptonClient.this.user;
			}
		});
	}

	public Future<Boolean> delete(String password) throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("password", password);
				DeleteData res = (DeleteData) KryptonClient.this.query(new DeleteQuery(parameters), true, false);
				KryptonClient.this.token = "";
				KryptonClient.this.user = null;
				KryptonClient.this.expiryDate = new Date(0);
				KryptonClient.this.cookieManager = new CookieManager();
				return (boolean) res.getData().get("deleteMe");
			}
		});
	}

	public Future<Boolean> recoverPassword(String email) throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("email", email);
				SendPasswordRecoveryData res = (SendPasswordRecoveryData) KryptonClient.this
						.query(new SendPasswordRecoveryQuery(parameters), false, false);
				return res.getData().get("sendPasswordRecoveryEmail");
			}
		});
	}

	public Future<Boolean> isEmailAvailable(String email) throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HashMap<String, Object> parameters = new HashMap<String, Object>();
				parameters.put("email", email);
				EmailAvailableData res = (EmailAvailableData) KryptonClient.this
						.query(new EmailAvailableQuery(parameters), false, false);
				return res.getData().get("emailAvailable");
			}
		});
	}

	public Future<Boolean> changePassword(String actualPassword, String newPassword) throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				HashMap<String, Object> fields = new HashMap<String, Object>();
				fields.put("password", newPassword);
				fields.put("previousPassword", actualPassword);
				KryptonClient.this.update(fields);
				return true;
			}
		});

	}

	public Future<Boolean> sendVerificationEmail() throws Exception {
		return executor.submit(new Callable<Boolean>() {
			public Boolean call() throws Exception {
				SendVerificationEmailData res = (SendVerificationEmailData) KryptonClient.this
						.query(new SendVerificationEmailQuery(), true, false);
				return res.getData().get("sendVerificationEmail");
			}
		});

	}

	public Future<Map<String, Object>> fetchUserOne(HashMap<String, Object> filter, String[] requestedFields)
			throws Exception {
		return executor.submit(new Callable<Map<String, Object>>() {
			public Map<String, Object> call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("filter", filter);
				UserOneData res = (UserOneData) KryptonClient.this.query(new UserOneQuery(parameter, requestedFields),
						false, false);
				return res.getData().get("userOne");
			}
		});

	}

	public Future<List<Map<String, Object>>> fetchUserByIds(ArrayList<String> ids, String[] requestedFields)
			throws Exception {
		return executor.submit(new Callable<List<Map<String, Object>>>() {
			public List<Map<String, Object>> call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				String[] idArray = ids.toArray(new String[ids.size()]);
				parameter.put("ids", idArray);
				UserByIdsData res = (UserByIdsData) KryptonClient.this
						.query(new UserByIdsQuery(parameter, requestedFields), false, false);
				return res.getData().get("userByIds");
			}
		});

	}

	public Future<List<Map<String, Object>>> fetchUserMany(HashMap<String, Object> filter, String[] requestedFields,
			int limit) throws Exception {
		return executor.submit(new Callable<List<Map<String, Object>>>() {
			public List<Map<String, Object>> call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("filter", filter);
				parameter.put("limit", limit);
				UserManyData res = (UserManyData) KryptonClient.this
						.query(new UserManyQuery(parameter, requestedFields), false, false);
				return res.getData().get("userMany");
			}
		});

	}

	public Future<List<Map<String, Object>>> fetchUserMany(HashMap<String, Object> filter, String[] requestedFields)
			throws Exception {
		return executor.submit(new Callable<List<Map<String, Object>>>() {
			public List<Map<String, Object>> call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("filter", filter);
				UserManyData res = (UserManyData) KryptonClient.this
						.query(new UserManyQuery(parameter, requestedFields), false, false);
				return res.getData().get("userMany");
			}
		});

	}

	public Future<Integer> fetchUserCount(HashMap<String, Object> filter) throws Exception {
		return executor.submit(new Callable<Integer>() {
			public Integer call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("filter", filter);
				UserCountData res = (UserCountData) KryptonClient.this.query(new UserCountQuery(parameter), false,
						false);
				return res.getData().get("userCount");
			}
		});

	}

	public Future<Integer> fetchUserCount() throws Exception {
		return executor.submit(new Callable<Integer>() {
			public Integer call() throws Exception {
				UserCountData res = (UserCountData) KryptonClient.this.query(new UserCountQuery(), false, false);
				return res.getData().get("userCount");
			}
		});

	}

	public Future<Pagination> fetchUserWithPagination(HashMap<String, Object> filter, String[] requestedFields,
			int page, int perPage) throws Exception {
		return executor.submit(new Callable<Pagination>() {
			public Pagination call() throws Exception {
				HashMap<String, Object> parameter = new HashMap<String, Object>();
				parameter.put("filter", filter);
				parameter.put("page", page);
				parameter.put("perPage", perPage);
				UserPaginationData res = (UserPaginationData) KryptonClient.this
						.query(new UserPaginationQuery(parameter, requestedFields), false, false);
				return res.getData().get("userPagination");
			}
		});

	}

	public Future<String> publicKey() throws Exception {
		return executor.submit(new Callable<String>() {
			public String call() throws Exception {
				PublicKeyData res = (PublicKeyData) KryptonClient.this.query(new PublicKeyQuery(), false, false);
				return res.getData().get("publicKey");
			}
		});

	}

	private void decodeToken(String token) {

		byte[] decodedBytes = Base64.getDecoder().decode(token.split("[.]")[1]);
		String decodedtoken = new String(decodedBytes);
		TypeToken<Map<String, Object>> map = new TypeToken<Map<String, Object>>() {
		};
		Map<String, Object> result = new Gson().fromJson(decodedtoken, map.getType());
		this.user = User.convertMapToUser(result);
	}
}
