package com.graduation.project.auth.config.security;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.auth.exception.CustomAccessDeniedHandlerOauth2;
import com.graduation.project.auth.exception.CustomAuthenticationEntryPointOauth2;
import com.graduation.project.auth.keys.RsaKeyProperties;
import com.graduation.project.user.repository.UserRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
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
      CustomAccessDeniedHandlerOauth2 accessDeniedHandlerOauth2)
      throws Exception {

    return http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/clinic/services/**").permitAll()
            .requestMatchers("/api/tracking/action").permitAll()
            .requestMatchers("/", "/index.html", "/dist/**", "/assets/**").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(
                jwtAuthenticationConverter())))
        // .oauth2ResourceServer(oauth2 -> oauth2
        // .jwt(Customizer.withDefaults())
        // .authenticationEntryPoint(entryPointOauth2)
        // .accessDeniedHandler(accessDeniedHandlerOauth2))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
  }

  //
  // @Bean
  // public JwtAuthenticationConverter jwtAuthenticationConverter() {
  // JwtGrantedAuthoritiesConverter authoritiesConverter = new
  // JwtGrantedAuthoritiesConverter();// JWT hiện có: "roles":
  // // "ROLE_DOCTOR"
  // authoritiesConverter.setAuthoritiesClaimName("roles");// Không thêm prefix vì
  // giá trị đã có ROLE_
  // authoritiesConverter.setAuthorityPrefix("");
  // JwtAuthenticationConverter authenticationConverter = new
  // JwtAuthenticationConverter();
  // authenticationConverter.setPrincipalClaimName(JwtClaimNames.SUB);
  // authenticationConverter.setJwtGrantedAuthoritiesConverter(
  // authoritiesConverter);
  // return authenticationConverter;
  // }
  @Bean
  public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
    return jwt -> {
      Object rolesClaim = jwt.getClaim("roles");

      List<String> roles =
          switch (rolesClaim) {
            case String role ->
                Arrays.stream(role.split("[,\\s]+")).filter(value -> !value.isBlank()).toList();

            case Collection<?> roleCollection ->
                roleCollection.stream()
                    .map(String::valueOf)
                    .filter(value -> !value.isBlank())
                    .toList();

            case null -> List.of();

            default -> List.of(String.valueOf(rolesClaim));
          };

      Collection<GrantedAuthority> authorities =
          roles.stream()
              .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
              .map(SimpleGrantedAuthority::new)
              .map(GrantedAuthority.class::cast)
              .toList();

      // Chỉ dùng tạm để xác minh, sau đó xóa.
      // System.out.println(
      // "JWT subject=" + jwt.getSubject()
      // + ", rolesClaim=" + rolesClaim
      // + ", authorities=" + authorities);
      log.info(
          "JWT_AUTH_DEBUG | subject=[{}] | rolesClaim=[{}] | authorities=[{}] | END",
          jwt.getSubject(),
          rolesClaim,
          authorities);
      return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
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
    return email ->
        userRepository
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
