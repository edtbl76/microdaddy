package org.tbl.microdaddy.authz.jose;

import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static java.util.UUID.randomUUID;
import static org.tbl.microdaddy.authz.jose.KeyGeneratorUtils.generateRsaKey;

public class Jwks {

    private Jwks() { }

    public static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(randomUUID().toString())
                .build();
    }

}
