package com.swd392.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@Slf4j
public class GoogleTokenVerifierService {

  private final GoogleIdTokenVerifier verifier;

  public GoogleTokenVerifierService(
      @Value("${app.google.web-client-id}") String webClientId,
      @Value("${app.google.android-client-id}") String androidClientId) {

    // Accept tokens from both Web and Android client IDs
    this.verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(Arrays.asList(webClientId, androidClientId))
        .build();

    log.info("GoogleTokenVerifierService initialized with Web and Android client IDs");
  }

  /**
   * Verify a Google ID token and return the payload if valid.
   *
   * @param idTokenString the raw ID token string from the client
   * @return GoogleIdToken.Payload containing user info, or null if invalid
   */
  public GoogleIdToken.Payload verify(String idTokenString) {
    try {
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken != null) {
        GoogleIdToken.Payload payload = idToken.getPayload();
        log.info("Google ID token verified successfully for email: {}", payload.getEmail());
        return payload;
      } else {
        log.warn("Google ID token verification returned null - token is invalid or expired");
        return null;
      }
    } catch (Exception e) {
      log.error("Error verifying Google ID token: {}", e.getMessage(), e);
      return null;
    }
  }
}
