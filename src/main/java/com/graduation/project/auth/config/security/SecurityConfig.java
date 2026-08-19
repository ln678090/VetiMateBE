package com.graduation.project.auth.config.security;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.auth.exception.CustomAccessDeniedHandlerOauth2;
import com.graduation.project.auth.exception.CustomAuthenticationEntryPointOauth2;
import com.graduation.project.auth.keys.RsaKeyProperties;
import com.graduation.project.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final RsaKeyProperties rsaKeys;

  @Value("${app.cors.allowed-origins}")
  private List<String> allowedOrigins;

  /*
   * @Bean
   * public SecurityFilterChain securityFilterChain(
   * HttpSecurity http,
   * CustomAuthenticationEntryPointOauth2 entryPointOauth2,
   * CustomAccessDeniedHandlerOauth2 accessDeniedHandlerOauth2)
   * throws Exception {
   * return http.csrf(AbstractHttpConfigurer::disable)
   * .cors(cors -> cors.configurationSource(corsConfigurationSource()))
   * // .addFilterBefore(rateLimitingFilter,
   * // UsernamePasswordAuthenticationFilter.class)
   * .authorizeHttpRequests(
   * auth ->
   * auth.requestMatchers("/api/auth/**")
   * .permitAll()
   * .requestMatchers("/api/auth/test")
   * .permitAll()
   * .requestMatchers(HttpMethod.GET, "/api/catalog/**")
   * .permitAll()
   * .requestMatchers(HttpMethod.GET, "/api/products/**")
   * .permitAll()
   * .requestMatchers("/api/tracking/action")
   * .permitAll()
   * .requestMatchers("/", "/index.html", "/dist/**", "/assets/**")
   * .permitAll()
   * .anyRequest()
   * .authenticated())
   * .oauth2ResourceServer(
   * oauth2 ->
   * oauth2
   * .jwt(Customizer.withDefaults())
   * .authenticationEntryPoint(entryPointOauth2)
   * .accessDeniedHandler(accessDeniedHandlerOauth2))
   * .sessionManagement(
   * session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   * .build();
   * }
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CustomAuthenticationEntryPointOauth2 entryPointOauth2,
      CustomAccessDeniedHandlerOauth2 accessDeniedHandlerOauth2) throws Exception {

    return http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            .requestMatchers("/api/tracking/action").permitAll()
            .requestMatchers("/", "/index.html", "/dist/**", "/assets/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            .authenticationEntryPoint(entryPointOauth2)
            .accessDeniedHandler(accessDeniedHandlerOauth2))
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  @Bean
  public org.springframework.core.convert.converter.Converter<org.springframework.security.oauth2.jwt.Jwt, org.springframework.security.authentication.AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return jwt -> {
      // Extract roles from the "roles" claim (space-separated string)
      String rolesStr = jwt.getClaimAsString("roles");
      java.util.Collection<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
      if (rolesStr != null && !rolesStr.isBlank()) {
        for (String role : rolesStr.split("\\s+")) {
          authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
        }
      }

      // Build CustomUserDetails from JWT claims
      CustomUserDetails userDetails = new CustomUserDetails(
          java.util.UUID.fromString(jwt.getSubject()),
          jwt.getClaimAsString("email"),
          "", // password not needed for JWT auth
          jwt.getClaimAsString("fullName"),
          jwt.getClaimAsString("username"),
          true,
          authorities
      );

      return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
          userDetails, jwt, authorities
      );
    };
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(allowedOrigins);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(
        List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
    configuration.setAllowCredentials(Boolean.TRUE);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return email -> userRepository
        .findByEmail(email)
        .map(CustomUserDetails::fromUser)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
  }
}
