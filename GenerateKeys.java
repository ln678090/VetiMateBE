import java.security.*;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        
        String pub = "-----BEGIN PUBLIC KEY-----\\n" + 
                     Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()).replaceAll("(.{64})", "$1\\\\n") + 
                     "\\n-----END PUBLIC KEY-----";
                     
        String priv = "-----BEGIN PRIVATE KEY-----\\n" + 
                      Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()).replaceAll("(.{64})", "$1\\\\n") + 
                      "\\n-----END PRIVATE KEY-----";
                      
        System.out.println("PUB===" + pub);
        System.out.println("PRIV===" + priv);
    }
}
