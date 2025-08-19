package api.poja.io.endpoint.rest.security.github;

import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.modes.ChaCha20Poly1305;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

public final class GithubSecretsEncryptor {

  private static final int PUBLIC_KEY_SIZE = 32;
  private static final int NONCE_SIZE = 12;

  private GithubSecretsEncryptor() {}

  /**
   * Chiffre un message en utilisant la clé publique du destinataire.
   *
   * @param message Le message en clair à chiffrer.
   * @param publicKeyBase64 La clé publique du repository, encodée en Base64.
   * @return Le message chiffré prêt à être envoyé à l'API GitHub, encodé en Base64.
   */
  public static String encrypt(final String message, final String publicKeyBase64) {
    final SecureRandom random = new SecureRandom();
    final byte[] recipientPublicKey = Base64.getDecoder().decode(publicKeyBase64);
    final byte[] messageBytes = message.getBytes();

    final X25519KeyPairGenerator keyPairGenerator = new X25519KeyPairGenerator();
    keyPairGenerator.init(new X25519KeyGenerationParameters(random));
    final AsymmetricCipherKeyPair ephemeralKeyPair = keyPairGenerator.generateKeyPair();
    final byte[] ephemeralPublicKey =
        ((X25519PublicKeyParameters) ephemeralKeyPair.getPublic()).getEncoded();
    final byte[] ephemeralPrivateKey =
        ((X25519PrivateKeyParameters) ephemeralKeyPair.getPrivate()).getEncoded();

    final byte[] sharedKey = new byte[PUBLIC_KEY_SIZE];
    final X25519PrivateKeyParameters privateKeyParams =
        new X25519PrivateKeyParameters(ephemeralPrivateKey, 0);
    privateKeyParams.generateSecret(
        new X25519PublicKeyParameters(recipientPublicKey, 0), sharedKey, 0);

    final ChaCha20Poly1305 chachaCipher = new ChaCha20Poly1305();
    final byte[] nonce = new byte[NONCE_SIZE];
    random.nextBytes(nonce);

    chachaCipher.init(true, new ParametersWithIV(new KeyParameter(sharedKey), nonce));

    final byte[] ciphertext = new byte[chachaCipher.getOutputSize(messageBytes.length)];
    int len = chachaCipher.processBytes(messageBytes, 0, messageBytes.length, ciphertext, 0);
    try {
      len += chachaCipher.doFinal(ciphertext, len);
    } catch (Exception e) {
      throw new RuntimeException("Encryption failed with ChaCha20-Poly1305", e);
    }

    final byte[] sealedMessage = new byte[PUBLIC_KEY_SIZE + NONCE_SIZE + len];
    System.arraycopy(ephemeralPublicKey, 0, sealedMessage, 0, PUBLIC_KEY_SIZE);
    System.arraycopy(nonce, 0, sealedMessage, PUBLIC_KEY_SIZE, NONCE_SIZE);
    System.arraycopy(ciphertext, 0, sealedMessage, PUBLIC_KEY_SIZE + NONCE_SIZE, len);

    return Base64.getEncoder().encodeToString(sealedMessage);
  }
}
