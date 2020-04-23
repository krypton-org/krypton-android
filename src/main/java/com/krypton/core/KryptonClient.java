package com.krypton.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;

import com.krypton.core.internal.queries.*;

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
        this.token ="";
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
        return "Bearer"+this.token;
    }

    public Map<String, Object> query(Query q, boolean isAuthTokenRequired) throws IOException, ParseException {
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

    private void saveExpiryDate(Data res, String mutationName) throws ParseException {
        Map<String, Map<String, Object>> data = res.data;
        Map<String, Object> login = data.get(mutationName);
        String dateString = (String) login.get("expiryDate");
        expiryDate = DATE_FORMAT.parse(dateString);
    }

    public Map<String, Object> query(Query q, boolean isAuthTokenRequired, boolean isRefreshed ) throws IOException, ParseException {
        URL url = new URL(endpoint);
        HttpURLConnection req = (HttpURLConnection) url.openConnection();
        req.setRequestMethod("POST");
        req.setRequestProperty("Content-Type", "application/json");
        req.setRequestProperty("Accept", "application/json");
        Data res = null;
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
        } catch(Exception err) {
            System.out.println(err);
        }
        if (res.errors != null && res.errors.size()> 0) {
            String errorType = res.errors.get(0).get("type");
            String message = res.errors.get(0).get("message");
            if (errorType == "UnauthorizedError" && !isRefreshed) {
                this.refreshToken();
                return this.query(q, isAuthTokenRequired, true);
            }
        }

        if (q instanceof LoginQuery) {
            this.saveToken(res, "login");
            this.saveExpiryDate(res, "login");
            this.saveCookies(req);
            this.setState(token);
        } else if (q instanceof UpdateQuery) {
            this.saveToken(res,"updateMe");
            this.saveExpiryDate(res, "updateMe");
            this.saveCookies(req);
        } else if (q instanceof RefreshQuery) {
            this.saveToken(res, "refreshToken");
            this.saveExpiryDate(res, "refreshToken");
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

    public void refreshToken() throws IOException, ParseException {
        this.query(new RefreshQuery(),false, true);
    }
    /*
     * user: {
     * 	email: "fdsfdsqfdsq@fdsfds.com",
     * 	password: "123456"
     *  age: 5,
     *  username: "toto"
     * }
     *
     * register(email, pass, fields = {age: 5
     * 									username : toto})
     * register(fields: {email:fdsfdsf username:jfdosfjdsfds age:5 password:fdsqfdsq})
     * register("fdsfdsfds", "fdsfdsfdsf", null)
     */

    public void register(String email, String password, Map<String, Object> otherFields) throws IOException, ParseException {
        if (otherFields==null) {
            otherFields = Collections.emptyMap();
        }
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        HashMap<String, Object> fields = new HashMap<String, Object>(otherFields);
        fields.put("email", email);
        fields.put("password", password);
        parameters.put("fields", fields);
        this.query(new RegisterQuery(parameters),false, false);
    }

    public void register(String email, String password) throws IOException, ParseException {
        Map<String, Object> empty = Collections.emptyMap();
        this.register(email, password, empty);
    }

    public void login(String email, String password) throws IOException, ParseException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", email);
        parameters.put("password", password);
        this.query(new LoginQuery(parameters),false,false);
    }

    public void delete(String password) throws IOException, ParseException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("password", password);
        this.query(new DeleteQuery(parameters),true,false);
    }

    public void recoverPassword(String email) throws IOException, ParseException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", email);
        this.query(new UpdateQuery(parameters),true,false);
    }

    public void emailAvailable(String email) throws IOException, ParseException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("email", email);
        this.query(new EmailAvailableQuery(parameters),false,false);
    }

    public void changePassword(String password, String previousPassword) throws IOException, ParseException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("password", password);
        parameters.put("PreviousPassword", previousPassword);
        this.query(new UpdateQuery(parameters),true ,false);
    }

    public void sendVerificationEmail() throws IOException, ParseException {
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
        user=new Gson().fromJson(Base64.getDecoder().decode(token.split("[.]")[1]).toString(), Map.class);
    }
}
