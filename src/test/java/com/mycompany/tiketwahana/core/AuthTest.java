package com.mycompany.tiketwahana.core;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AuthTest {
    @Test void hashesAndVerifiesWithoutStoringPlaintext() {
        String hash = Auth.hash("correct horse battery");
        assertTrue(Auth.isHashed(hash));
        assertNotEquals("correct horse battery", hash);
        assertTrue(Auth.verify("correct horse battery", hash));
        assertFalse(Auth.verify("wrong", hash));
    }
    @Test void saltsProduceDifferentValues() {
        assertNotEquals(Auth.hash("same password"), Auth.hash("same password"));
    }
    @Test void policyRejectsTemporaryPasswords() {
        assertThrows(AuthException.class, () -> Auth.checkPolicy("admin"));
        assertThrows(AuthException.class, () -> Auth.checkPolicy("12345678"));
        assertDoesNotThrow(() -> Auth.checkPolicy("tiket-kuat-2026"));
    }
}
