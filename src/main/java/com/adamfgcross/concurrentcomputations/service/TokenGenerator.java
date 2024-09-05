package com.adamfgcross.concurrentcomputations.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class TokenGenerator {

	private static final SecureRandom secureRandom = new SecureRandom(); // SecureRandom is preferred to Random
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); // Base64 encoder to encode as string

	public String generateToken() {
		byte[] randomBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes); 
	}
}
