package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.Mac;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Objects;

abstract class MediaInputStream extends InputStream {
    static final int BUFFER_LENGTH = 8192;
    static final int MAC_LENGTH = 10;
    static final int EXPANDED_SIZE = 112;
    static final int KEY_LENGTH = 32;
    static final int IV_LENGTH = 16;

    final InputStream rawInputStream;
    MediaInputStream(InputStream rawInputStream) {
        this.rawInputStream = Objects.requireNonNull(rawInputStream, "rawInputStream must not be null");
    }

    byte[] deriveMediaKeyData(byte[] mediaKey, String mediaKeyName) throws WhatsAppMediaException {
        try {
            var hkdf = KDF.getInstance("HKDF-SHA256");
            var params = HKDFParameterSpec.ofExtract()
                    .addIKM(new SecretKeySpec(mediaKey, "AES"))
                    .thenExpand(mediaKeyName.getBytes(), EXPANDED_SIZE);
            return hkdf.deriveData(params);
        }catch (GeneralSecurityException e) {
            throw new WhatsAppMediaException("Cannot derive media key data", e);
        }
    }

    MessageDigest newHash() throws WhatsAppMediaException {
        try {
            return MessageDigest.getInstance("SHA-256");
        }catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new hash", exception);
        }
    }

    Cipher newCipher(int mode, SecretKeySpec key, IvParameterSpec iv) throws WhatsAppMediaException {
        try {
            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, key, iv);
            return cipher;
        }catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new cipher", exception);
        }
    }

    Mac newMac(SecretKeySpec key) throws WhatsAppMediaException {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac;
        }catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new mac", exception);
        }
    }

    @Override
    public void close() throws IOException {
        rawInputStream.close();
    }
}
