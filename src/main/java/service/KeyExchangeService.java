package service;

import model.User;
import util.CryptoUtils;
import org.springframework.stereotype.Service;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeyExchangeService {
    private final Map<Long, KeyPair> userKeyPairs = new HashMap<>();

    public PublicKey getPublicKey(User user) {
        KeyPair keyPair = userKeyPairs.get(user.getId());
        if (keyPair == null) {
            keyPair = generateKeyPair();
            userKeyPairs.put(user.getId(), keyPair);
        }
        return keyPair.getPublic();
    }

    public SecretKey generateSharedSecret(User sender, User recipient) {
        KeyPair senderKeyPair = userKeyPairs.get(sender.getId());
        KeyPair recipientKeyPair = userKeyPairs.get(recipient.getId());

        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(senderKeyPair.getPrivate());
            keyAgreement.doPhase(recipientKeyPair.getPublic(), true);
            return keyAgreement.generateSecret("AES");
        } catch (Exception e) {
            throw new RuntimeException("Error generating shared secret", e);
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating key pair", e);
        }
    }
}

