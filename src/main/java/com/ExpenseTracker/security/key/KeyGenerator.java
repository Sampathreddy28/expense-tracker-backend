package com.ExpenseTracker.security.key;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

public class KeyGenerator {
	
	public static String generateJwtToken() {
		 SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);

	        // Encode the raw key bytes into a Base64 string for easy use in application.properties
	        String secretString = Base64.getEncoder().encodeToString(key.getEncoded());
	        System.out.println("jwtSecret key :-"+secretString);
	        return secretString;
	}
	
    public static void main(String[] args) {
        // Generate a cryptographically secure key specifically for HS512 (512 bits / 64 bytes)
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);

        // Encode the raw key bytes into a Base64 string for easy use in application.properties
        String secretString = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("🔥 New 512-bit JWT Secret Key (Base64 Encoded):");
        System.out.println(secretString);
        System.out.println("--------------------------------------------------------------------------------");
    }
}