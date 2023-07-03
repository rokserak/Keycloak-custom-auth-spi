package com.example.keycloak;

import com.example.keycloak.dto.User;
import com.example.keycloak.dto.UserResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;


public class CustomAuthenticator implements Authenticator {

    private final Logger log = LoggerFactory.getLogger(CustomAuthenticator.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private final KeycloakSession session;

    public CustomAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Method is used for user authentication. It makes a call to an external API that returns a jwt token if the user is authenticated
     * If the user is authenticated an authenticated user is set.
     * Whereas if the user is not authenticated, an error is set.
     * @param context
     */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.info("CUSTOMER PROVIDER authenticate");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String totp = formData.getFirst("totp");
        System.out.println("TOTP: " + totp);
        if (totp == null || totp.isEmpty()) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"invalid_grant\",\"error_description\": \"OTP missing\"}")
                    .type("application/json")
                    .build());
            return;
        }

        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        log.debug("CUSTOMER PROVIDER action");
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Set the required actions for the user after authentication
    }

    @Override
    public void close() {
        // Closes any open resources
    }
}
