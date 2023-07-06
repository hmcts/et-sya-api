package uk.gov.hmcts.reform.et.syaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.reform.et.syaapi.models.CreateUser;
import uk.gov.hmcts.reform.et.syaapi.models.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
            Collections.singletonList(new Role("citizen"))
        );

        String body = new ObjectMapper().writeValueAsString(createUser);
        makePostRequest(baseIdamApiUrl + "/testing-support/accounts", body);

        return createUser;
    }

    private void makePostRequest(String uri, String body) throws IOException {
        HttpResponse createUserResponse = client.execute(post(uri)
                                                             .setEntity(new StringEntity(body, APPLICATION_JSON))
                                                             .build());

        assertEquals(CREATED.value(), createUserResponse.getStatusLine().getStatusCode());
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
}
