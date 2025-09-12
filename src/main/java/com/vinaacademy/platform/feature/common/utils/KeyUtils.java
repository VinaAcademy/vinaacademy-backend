package com.vinaacademy.platform.feature.common.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KeyUtils {
  public static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
  public static final String PUBLIC_KEY_PATH = "keys/public_key.pem";

  public static KeyPair loadOrCreateKeyPair() throws Exception {
    File privateKeyFile = new File(PRIVATE_KEY_PATH);
    File publicKeyFile = new File(PUBLIC_KEY_PATH);

    if (privateKeyFile.exists() && publicKeyFile.exists()) {
      // Load existing keys
      return new KeyPair(loadPublicKey(), loadPrivateKey());
    } else {
      // Create and save new keys
      KeyPair keyPair = generateRsaKeyPair();
      saveKeyAsPEM(PRIVATE_KEY_PATH, keyPair.getPrivate(), "PRIVATE KEY");
      saveKeyAsPEM(PUBLIC_KEY_PATH, keyPair.getPublic(), "PUBLIC KEY");
      return keyPair;
    }
  }

  private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
    keyPairGen.initialize(2048);
    return keyPairGen.generateKeyPair();
  }

  private static void saveKeyAsPEM(String path, Key key, String type) throws IOException {
    File file = new File(path);
    boolean dirsCreated =
        file.getParentFile().mkdirs();
    if (!dirsCreated && !file.getParentFile().exists()) {
      throw new IOException("Failed to create directories for path: " + path);
    }

    String base64 = Base64.getEncoder().encodeToString(key.getEncoded());
    try (FileWriter writer = new FileWriter(file)) {
      writer.write("-----BEGIN " + type + "-----\n");
      writer.write(base64.replaceAll("(.{64})", "$1\n"));
      writer.write("\n-----END " + type + "-----\n");
    }
  }

  private static PrivateKey loadPrivateKey() throws Exception {
    String keyPEM =
        new String(Files.readAllBytes(Paths.get(KeyUtils.PRIVATE_KEY_PATH)))
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

    byte[] decoded = Base64.getDecoder().decode(keyPEM);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
    return KeyFactory.getInstance("RSA").generatePrivate(spec);
  }

  private static PublicKey loadPublicKey() throws Exception {
    String keyPEM =
        new String(Files.readAllBytes(Paths.get(KeyUtils.PUBLIC_KEY_PATH)))
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

    byte[] decoded = Base64.getDecoder().decode(keyPEM);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
    return KeyFactory.getInstance("RSA").generatePublic(spec);
  }
}
