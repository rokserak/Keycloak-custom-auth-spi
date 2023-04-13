package com.example.keycloak;

import com.example.keycloak.dto.AuthRequestDTO;
import com.example.keycloak.dto.AuthResponseDTO;
import com.example.keycloak.dto.UserResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomExternalApi {
    public static final String API_URL = "http://192.168.1.62:8082";
    private final Logger log = LoggerFactory.getLogger(CustomExternalApi.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Method used for user authentication. A call is made to an external API for authentication.
     * @param username username of the user
     * @param password password of the user
     * @return Token of the user
     * @throws IOException
     */
    public String getTokenAuthenticateToExternalApi(String username, String password) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //Define url for the request
        HttpPost httpPost = new HttpPost(API_URL + "/auth/login");

        //Define json object to send
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(new AuthRequestDTO(username, password));

        //Define content type and attach json object to the request
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        //Execute request
        CloseableHttpResponse response = httpClient.execute(httpPost);

        //Read response
        String responseJson = EntityUtils.toString(response.getEntity());

        //convert json into my authentification response object
        AuthResponseDTO x = objectMapper.readValue(responseJson.toString(), AuthResponseDTO.class);

        response.close();
        httpClient.close();
        return x.getToken();
    }

    /**
     * Method used to get user profile. A call is made to an external API for user profile.
     * @param token token of the user
     * @return UserResponseDTO Object containing user profile
     * @throws IOException
     */
    public UserResponseDTO getProfileToExternalApi(String token) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //Define url for the request
        HttpGet httpGet = new HttpGet(API_URL + "/auth/profile");
        //Add token to the request
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "bearer " + token);

        //Execute request
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet, HttpClientContext.create());

        //Read response
        HttpEntity entity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);

        //check the response status
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            //log error
            log.error(String.format("Failed to POST login: %s %s",  httpResponse.getStatusLine().getStatusCode(), responseString));
            httpResponse.close();
            httpClient.close();
            return null;
        }else{
            //convert json into my response object
            UserResponseDTO x = objectMapper.readValue(responseString, UserResponseDTO.class);

            httpResponse.close();
            httpClient.close();
            return x;
        }

    }
}
