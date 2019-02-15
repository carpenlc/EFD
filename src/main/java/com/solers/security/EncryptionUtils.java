/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class EncryptionUtils {
    
    private static final int IV_SIZE = 16;
    private static final int SALT_SIZE = 8;
    private static final int KEY_ITERATIONS = 2000;
    private static final int KEY_LENGTH = 256;
    private static final String KEY_TYPE = "AES";
    private static final String KDF_ALGORITHM = "PBKDF2WithSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte [] SALT = ArrayUtils.subarray(System.class.getName().getBytes(), 0 , SALT_SIZE);
    private static final String MAC_ALGORITHM = "HmacSHA256";
    
    /**
     * Note that the keys are generated with 256 bytes.  This requires the 
     * "strong" JCE encryption jars
     * 
     * @return AES key
     */
    public static Key generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_TYPE);
            keyGen.init(KEY_LENGTH);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AES-256 algorithm not available");
        }
    }
    
    public static String toHex(byte [] data) {
        return new String(Hex.encodeHex(data));
    }
    
    public static byte [] fromHex(String hex) {
        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (DecoderException ex) {
            throw new RuntimeException("Input is not hex", ex);
        }
    }
    
    public static String encryptAsHex(char [] password, byte [] plaintext) {
        return toHex(encrypt(password, plaintext));
    }
    
    public static String decryptFromHex(char [] password, String hexCipherText) {
        return new String(decrypt(password, fromHex(hexCipherText)));
    }
    
    public static byte[] encrypt(char [] password, byte [] plaintext) {
        try {
            return crypt(plaintext, password, Cipher.ENCRYPT_MODE);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("Could not encrypt message", ex);
        }
    }
    
    public static byte[] decrypt(char [] password, byte [] ciphertext) {
        try {
            return crypt(ciphertext, password, Cipher.DECRYPT_MODE);
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException("Could not decrypt message", ex);
        }
    }
    
    /**
     * Seals a serializable object given a password. The password is used for creating a cipher.
     * 
     * @param obj to be sealed
     * @param password used for sealing the object
     * @return a sealed object
     * @throws IllegalBlockSizeException if there is a problem with using the cipher
     * @throws IOException if there is a problem serializing the object
     * @throws GeneralSecurityException if there is a problem getting a cipher
     */
    public static SealedObject seal(Serializable obj, char[] password) 
        throws IllegalBlockSizeException, IOException, GeneralSecurityException {
        return new SealedObject(obj, getCipher(password, Cipher.ENCRYPT_MODE));
    }
    
    /**
     * Unseals a sealed object given the same password that was used to seal it.
     * The password is used for creating a cipher.
     * 
     * @param sealed - object to be unsealed
     * @param password - same password that was used to seal the object
     * @return an unsealed object.  Caller will have to cast the object appropriately
     * @throws IllegalBlockSizeException if there is a problem with using the cipher
     * @throws BadPaddingException if there is a problem with using the cipher
     * @throws IOException if errors occur during deserialization
     * @throws ClassNotFoundException if errors occur during deserialization
     * @throws ClassCastException if T is not of a valid type
     * @throws GeneralSecurityException if there is a problem getting a cipher
     */
    @SuppressWarnings("unchecked")
    public static <T> T unseal(SealedObject sealed, char[] password) 
        throws IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException, GeneralSecurityException {
        return (T)sealed.getObject(getCipher(password, Cipher.DECRYPT_MODE));
    }
    
    /**
     * This is itself a method because caller will need to use a new MAC key if the message id has 
     * reached the MAX_VALUE and need to be reset.  Caching of key is left to the caller.
     * 
     * @return a valid MAC secret key based on the default MAC algorithm
     * @throws NoSuchAlgorithmException if the algorithm needed is not supported
     */
    public static SecretKey getMacKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(MAC_ALGORITHM);
        return generator.generateKey();
    }
    
    /**
     * Caller should call getMacKey() to get a valid secret key to be passed in as the secret key.  
     * Caller may need to cache the same key in order to validate the hash generated by 
     * the MAC function.
     * 
     * Caller can use the helper method getObjectArray() to convert an object
     * to byte[] before passing into Mac.doFinal(byte[]).
     * 
     * @param key - generated from calling getMacKey()
     * @return an initialized MAC object
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static Mac getMac(SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(key);
        return mac;
    }
    
    /**
     * This method returns a byte[] representation of an object.  This is included 
     * in this util because quite a few methods requires params of type byte[].
     * 
     * @param object to be converted to a byte[]
     * @return a byte[] representation of the object
     * @throws IOException if there is a problem converting an object to byte[]
     */
    public static byte[] getObjectArray(Object object) throws IOException {
        ObjectOutput out = null;
        ByteArrayOutputStream bos = null;
        byte[] result = null;
        
        bos = new ByteArrayOutputStream() ;
        out = new ObjectOutputStream(bos);
        out.writeObject(object);
        out.close();
        result = bos.toByteArray();
        bos.close();

        return result;
    }
    
    /**
     * This method returns a string representation of an array of bytes.
     * 
     * @param bytes to be converted to a string
     * @return a String representation of the array of bytes
     */
    public static String getStringFromBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        
        for (byte a : bytes) {
            sb.append(String.format("%02X", a));
        }
        
        return sb.toString();
    }
    
    private static byte [] crypt(byte [] message, char [] password, int mode) throws GeneralSecurityException {
        Cipher cipher = getCipher(password, mode);
        return cipher.doFinal(message);
    }
    
    private static SecretKey generateKey(char [] password) throws GeneralSecurityException {
        SecretKeyFactory pbeSkf = SecretKeyFactory.getInstance(KDF_ALGORITHM);
        PBEKeySpec ks = new PBEKeySpec(password, SALT, KEY_ITERATIONS, KEY_LENGTH);
        try {
            SecretKey pbeKey = pbeSkf.generateSecret(ks);
            KeySpec aesKs = new SecretKeySpec(pbeKey.getEncoded(), KEY_TYPE);
            SecretKeyFactory aesSkf = SecretKeyFactory.getInstance(KEY_TYPE);
            return aesSkf.generateSecret(aesKs);
        } finally {
            ks.clearPassword();
        }
    }
    
    private static IvParameterSpec deriveIv(SecretKey key) {
        byte [] data = key.getEncoded();
        byte [] iv = new byte [IV_SIZE];
        int half = IV_SIZE / 2;
        System.arraycopy(data, data.length-half, iv, 0, half);
        System.arraycopy(iv, 0, iv, half, half);
        
        return new IvParameterSpec(iv);
    }

    /**
     * Returns a cipher given a password and cipher mode
     * 
     * @param password for generating a key
     * @param mode for the cipher - ENCRYPT, DECRYPT, etc.
     * @return an initialized cipher of the given mode
     * @throws GeneralSecurityException if there is a problem creating the cipher
     */
    private static Cipher getCipher(char[] password, int mode) throws GeneralSecurityException {
        SecretKey key = generateKey(password);
        IvParameterSpec iv = deriveIv(key);
        
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(mode, key, iv);

        return cipher;
    }
}
