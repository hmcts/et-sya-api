package uk.gov.hmcts.reform.et.syaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.reform.et.syaapi.model.CreateUser;
import uk.gov.hmcts.reform.et.syaapi.model.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
public class IdamTestApiRequests {
    private final HttpClient client;
    private final String baseIdamApiUrl;
    private static final String USER_PASSWORD = "Apassword123";

    public IdamTestApiRequests(HttpClient client, String baseIdamApiUrl) {
        this.client = client;
        this.baseIdamApiUrl = baseIdamApiUrl;
    }

    public CreateUser createUser(String email) throws IOException {
        CreateUser createUser = new CreateUser(
            email,
            "ATestForename",
            "ATestSurname",
            USER_PASSWORD,
            List.of(new Role("citizen"), new Role("caseworker-employment-api"))
        );

        String body = new ObjectMapper().writeValueAsString(createUser);
        makePostRequest(baseIdamApiUrl + "/testing-support/accounts", body);

        return createUser;
    }

    private void makePostRequest(String uri, String body) throws IOException {
        HttpResponse createUserResponse = client.execute(post(uri)
                                                             .setEntity(new StringEntity(body, APPLICATION_JSON))
                                                             .build());

        int statusCode = createUserResponse.getStatusLine().getStatusCode();

        assertTrue(statusCode == CREATED.value() || statusCode == OK.value());
    }

    public String getAccessToken(String email) throws IOException {
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("username", email));
        formparams.add(new BasicNameValuePair("password", USER_PASSWORD));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpResponse loginResponse = client.execute(post(baseIdamApiUrl + "/loginUser")
                                                        .setHeader("Content-type", APPLICATION_FORM_URLENCODED_VALUE)
                                                        .setEntity(entity)
                                                        .build());
        assertEquals(OK.value(), loginResponse.getStatusLine().getStatusCode());

        String tokens = EntityUtils.toString(loginResponse.getEntity());
        JSONObject jsonObject;
        String accessToken = null;
        try {
            jsonObject = new JSONObject(tokens);
            accessToken = jsonObject.get("access_token").toString();
        } catch (JSONException e) {
            log.error("Failed to get access token from loginResponse, error: ", e);
        }
        assertNotNull(accessToken);
        return "Bearer " + accessToken;
    }

    /**
     * Get Access Token when testing locally - uses standard login journey which requires xui app to be running.
     */
    public String getLocalAccessToken() throws IOException {
        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = instance.execute(new HttpGet("http://localhost:3455/auth/login"));

        List<String> cookies =
            Arrays.stream(response.getHeaders("Set-Cookie")).map(o -> o.getValue().substring(0,
                o.getValue().indexOf(";"))).collect(Collectors.toList());

        cookies.add("seen_cookie_message=yes");
        cookies.add("cookies_policy={ \"essential\": true, \"analytics\": false, \"apm\": false }");
        cookies.add("cookies_preferences_set=false");

        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("username", "et.dev@hmcts.net"));
        formparams.add(new BasicNameValuePair("password", "Pa55word11"));
        formparams.add(new BasicNameValuePair("save", "Sign in"));
        formparams.add(new BasicNameValuePair("selfRegistrationEnabled", "true"));
        formparams.add(new BasicNameValuePair("azureLoginEnabled", "true"));
        formparams.add(new BasicNameValuePair("mojLoginEnabled", "true"));
        formparams.add(new BasicNameValuePair("_csrf", "idklol"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

        HttpResponse loginResponse = client.execute(post(response.getHeaders("Location")[0].getValue())
                                                        .setHeader("Content-type", APPLICATION_FORM_URLENCODED_VALUE)
                                                        .setHeader("Cookie", String.join("; ", cookies))
                                                        .setEntity(entity)
                                                        .build());

        Header[] locations = loginResponse.getHeaders("Location");

        Arrays.stream(loginResponse.getHeaders("Set-Cookie")).forEach(o -> cookies.add(o.getValue()));
        String cookieStr = String.join("; ", cookies);
        HttpGet httpGet = new HttpGet(locations[0].getValue());
        httpGet.setHeader("Cookie", cookieStr);
        HttpResponse callbackResponse = instance.execute(httpGet);

        String auth = Arrays.stream(callbackResponse.getHeaders("Set-Cookie")).filter(o -> o.getValue().startsWith(
            "__auth__")).findFirst().get().getValue();

        return "Bearer " + auth.substring(9, auth.indexOf(';'));
    }

}
