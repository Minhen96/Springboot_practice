package com.example.mhpractice.common.security;

import java.time.Instant;
import java.util.Date;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String base64Secret;

    @Value("${app.jwt.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.jwt.encryption-key}")
    private String base64EncryptionKey;

    @Autowired
    private EncryptService encryptService;

    // Converted keys (initialized in @PostConstruct)
    private byte[] secretKeyBytes;
    private SecretKey encryptionKey;

    /**
     * Convert Base64 strings to proper key objects after Spring injects values
     */
    @PostConstruct // Run after constructor (Once after injection)
    public void init() {
        // Convert JWT signing secret (Base64 to byte array)
        this.secretKeyBytes = Base64.getDecoder().decode(base64Secret);

        // Validate key length (HMAC-SHA256 needs at least 32 bytes)
        if (secretKeyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 256 bits (32 bytes)");
        }

        // Convert AES encryption key
        byte[] encryptionKeyBytes = Base64.getDecoder().decode(base64EncryptionKey);
        this.encryptionKey = new SecretKeySpec(encryptionKeyBytes, "AES");
    }

    public String generateAccessToken(String email) {
        return generateToken(email, "ACCESS");
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, "REFRESH");
    }

    public String generateToken(String email, String tokenType) {
        try {

            // Current time
            Instant now = Instant.now();
            Instant expiration = now.plusMillis(accessTokenExpirationMs);

            // JWT claims
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("type", tokenType)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .build();

            // Create JWT object (draft, dont have signature)
            SignedJWT draftJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

            // Sign JWT (now it is in header.claim.signature)
            draftJWT.sign(new MACSigner(secretKeyBytes));

            // Serialize JWT (compact form)
            String compactJwtToken = draftJWT.serialize();

            // Encrypt JWT token (cant read)
            return encryptService.encryptUrlSafe(compactJwtToken, encryptionKey);

        } catch (JOSEException ex) {
            throw new RuntimeException("Failed to generate token.");
        }
    }

    public boolean validateToken(String encodedToken) {
        try {

            // Decrypt the unreadable jwt token
            String compactToken = encryptService.decryptUrlSafe(encodedToken, encryptionKey);
            log.debug("Decrypted token successfully");

            // Parse the compact jwt token
            SignedJWT parsedJwt = SignedJWT.parse(compactToken);
            log.debug("Parsed JWT successfully");

            // Verify the signature
            JWSVerifier verifier = new MACVerifier(secretKeyBytes);
            if (!parsedJwt.verify(verifier)) {
                log.debug("Signature verification failed");
                return false;
            }
            log.debug("Signature verified");

            // Check expiration
            Date expiration = parsedJwt.getJWTClaimsSet().getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                log.debug("Token expired. Expiration: {}, Now: {}", expiration, new Date());
                return false;
            }
            log.debug("Token not expired");

            // Check token type (ACCESS vs REFRESH)
            // String realTokenType = parsedJwt.getJWTClaimsSet().getStringClaim("type");
            // if (tokenType != null && !tokenType.equals(realTokenType)) {
            // log.warn("Invalid token type. Expected: {}, Got: {}", realTokenType,
            // tokenType);
            // return false;
            // }

            return true;

        } catch (Exception e) {
            log.debug("Token validation exception: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String encodedToken) {
        try {
            String decryptedJwt = encryptService.decryptUrlSafe(encodedToken, encryptionKey);

            SignedJWT parsedJwt = SignedJWT.parse(decryptedJwt);

            return parsedJwt.getJWTClaimsSet().getSubject();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get email from token.");
        }
    }
}
