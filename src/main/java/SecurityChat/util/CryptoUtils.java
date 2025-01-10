package SecurityChat.util;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class CryptoUtils {
    public static byte[] encrypt(byte[] plaintext, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error encrypting message", e);
        }
    }

    public static byte[] decrypt(byte[] ciphertext, SecretKey secretKey) {
        try {
            byte[] iv = new byte[16];
            byte[] encryptedData = new byte[ciphertext.length - iv.length];
            System.arraycopy(ciphertext, 0, iv, 0, iv.length);
            System.arraycopy(ciphertext, iv.length, encryptedData, 0, encryptedData.length);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(encryptedData);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error decrypting message", e);
        }
    }

    public static byte[] computeMAC(byte[] data, SecretKey secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error computing MAC", e);
        }
    }

    public static boolean verifyMAC(byte[] data, byte[] mac, SecretKey secretKey) {
        byte[] computedMAC = computeMAC(data, secretKey);
        return MessageUtils.constantTimeEquals(mac, computedMAC);
    }
}