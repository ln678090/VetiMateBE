package com.graduation.project.auth.keys;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.context.properties.ConfigurationProperties;

// Class này tự động lấy đường dẫn file từ application.yml và map thành Object Key
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {}
