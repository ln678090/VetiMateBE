package com.graduation.project.auth.config.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

  private final RedisTemplate<String, String> redisTemplate;

  private static final int AUTH_MAX_REQUESTS = 10;
  private static final int REFRESH_MAX_REQUESTS = 20;
  private static final int GENERAL_MAX_REQUESTS = 150;
  private static final int EXPIRATION_SECONDS = 60;

  private static final String LUA_SCRIPT =
      "local current = redis.call('INCR', KEYS[1]); "
          + "local ttl = redis.call('TTL', KEYS[1]); "
          + "if ttl == -1 then "
          + "   redis.call('EXPIRE', KEYS[1], ARGV[1]); "
          + "end; "
          + "return current;";

  private final DefaultRedisScript<Long> redisScript = buildScript();

  private static DefaultRedisScript<Long> buildScript() {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText(LUA_SCRIPT);
    script.setResultType(Long.class);
    return script;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = request.getHeader("X-Forwarded-For");
    if (clientIp == null || clientIp.isEmpty()) {
      clientIp = request.getRemoteAddr();
    }

    String uri = request.getRequestURI();

    // Xác định tầng và giới hạn
    String tier;
    String redisKey;
    int maxRequests;

    if (uri.startsWith("/api/auth/login") || uri.startsWith("/api/auth/register")) {
      // Tầng 1: Đăng nhập / Đăng ký -> nghiêm nhất
      tier = "AUTH";
      redisKey = "connecthub:rl:auth:" + clientIp;
      maxRequests = AUTH_MAX_REQUESTS;

    } else if (uri.startsWith("/api/auth/refresh")) {
      // Tầng 2: Refresh token (Axios tự gọi ngầm) -> thoải mái hơn
      tier = "REFRESH";
      redisKey = "connecthub:rl:refresh:" + clientIp;
      maxRequests = REFRESH_MAX_REQUESTS;

    } else {
      // Tầng 3: Mọi API còn lại -> bảo vệ Render khỏi sập
      tier = "GENERAL";
      redisKey = "connecthub:rl:general:" + clientIp;
      maxRequests = GENERAL_MAX_REQUESTS;
    }

    List<String> keys = Collections.singletonList(redisKey);
    Long requestCount =
        redisTemplate.execute(redisScript, keys, String.valueOf(EXPIRATION_SECONDS));

    if (requestCount != null && requestCount > maxRequests) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json;charset=UTF-8");

      String message =
          switch (tier) {
            case "AUTH" -> "Bạn thử đăng nhập quá nhiều lần. Vui lòng chờ 1 phút.";
            case "REFRESH" -> "Phiên làm việc bất thường. Vui lòng đăng nhập lại.";
            default -> "Bạn thao tác quá nhanh. Vui lòng chờ 1 phút.";
          };

      response.getWriter().write("{\"status\": 429, \"message\": \"" + message + "\"}");
      //            log.warn("[RateLimit][{}] Blocked IP: {} | Count: {}/{} | URI: {}",
      //                    tier, clientIp, requestCount, maxRequests, uri);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
