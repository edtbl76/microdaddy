package org.tbl.microdaddy.authz.jose;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.nimbusds.jose.jwk.Curve.forECParameterSpec;
import static java.util.UUID.randomUUID;
import static org.tbl.microdaddy.authz.jose.KeyGeneratorUtils.generateEcKey;
import static org.tbl.microdaddy.authz.jose.KeyGeneratorUtils.generateRsaKey;
import static org.tbl.microdaddy.authz.jose.KeyGeneratorUtils.generateSecretKey;

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

    public static ECKey generateEc() {
        KeyPair keyPair = generateEcKey();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        Curve curve = forECParameterSpec(publicKey.getParams());

        return new ECKey.Builder(curve, publicKey)
                .privateKey(privateKey)
                .keyID(randomUUID().toString())
                .build();
    }

    public static OctetSequenceKey generateSecret() {
        SecretKey secretKey = generateSecretKey();

        return new OctetSequenceKey.Builder(secretKey)
                .keyID(randomUUID().toString())
                .build();
    }
}
