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
        String username = formData.getFirst("username");
        String password = formData.getFirst("password");
        log.debug("AUTHENTICATE custom provider: " + username);

        User user = null;
        try {
            user = callExternalApi(username, password);
        } catch (IOException e) {
            log.error("Errore durante la chiamata all'API esterna", e);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
        }

        if (user != null) {
            try {
                UserModel userModel = context.getSession().users().getUserByUsername(context.getRealm(), user.getUsername());
                if (userModel == null) {
                    // create user if not exists
                    userModel = context.getSession().users().addUser(context.getRealm(), user.getUsername());
                }
                userModel.setEmail(user.getEmail());
                userModel.setFirstName(user.getFirstName());
                userModel.setLastName(user.getLastName());
                userModel.setEnabled(true);
                userModel.setSingleAttribute("extra-key", "extra-value");
                for (String role : user.getRoles()) {
                    userModel.grantRole(context.getRealm().getRole(role));
                }
                //userModel.grantRole(context.getRealm().getRole("user"));
                context.setUser(userModel);
            }
            catch (Exception e) {
                log.error("Authentication error", e);
                context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            }
            context.success();
        } else {
            // User not authenticated set unauthorized error
            context.failure(AuthenticationFlowError.INVALID_USER, Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You must be authenticated to access this resource.")
                    .build());
            return;
        }
        // It is also possible to use the challenge() method to request the user to provide further information to complete the authentication.
    }

    /**
     * Call to external API for authentication
     * @param username Username of the user
     * @param password Password of the user
     * @return User authenticated
     * @throws IOException
     */
    private User callExternalApi(String username, String password) throws IOException {
        CustomExternalApi customExternalApi = new CustomExternalApi();
        String token = customExternalApi.getTokenAuthenticateToExternalApi(username, password);
        if(token == null) {
            return null;
        }
        UserResponseDTO userResponseDTO = customExternalApi.getProfileToExternalApi(token);
        return new User(userResponseDTO.getEmail(), userResponseDTO.getEmail(), userResponseDTO.getName(), userResponseDTO.getSurname(), token, userResponseDTO.getRoles());
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
