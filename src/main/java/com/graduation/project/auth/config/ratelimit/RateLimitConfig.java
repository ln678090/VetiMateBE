package com.graduation.project.auth.config.ratelimit;

// @Configuration
// @EnableCaching
public class RateLimitConfig {

  public static final String CACHE_NAME = "rate-limit-buckets";

  // Đọc tự động từ spring.data.redis.host trong application.yml
  // @Value("${spring.data.redis.host:localhost}")
  // private String redisHost;

  // // Đọc tự động từ spring.data.redis.port trong application.yml
  // @Value("${spring.data.redis.port:6379}")
  // private String redisPort;

  // @Bean
  // public Config redissonConfig() {
  //     Config config = new Config();

  //     // Nối chuỗi động tạo ra định dạng "redis://host:port"
  //     String redisAddress = String.format("redis://%s:%s", redisHost, redisPort);

  //     config.useSingleServer().setAddress(redisAddress);

  //     /*
  //     Nếu sau này Redis trên Production có mật khẩu, bạn chỉ cần thêm:
  //     @Value("${spring.data.redis.password:}") private String redisPassword;
  //     Sau đó check:
  //     if (redisPassword != null && !redisPassword.isEmpty()) {
  //         config.useSingleServer().setPassword(redisPassword);
  //     }
  //     */

  //     return config;
  // }

  // @Bean
  // public CacheManager jCacheManager(Config redissonConfig) {
  //     CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();

  //     javax.cache.configuration.Configuration<Object, Object> jcacheConfig =
  //             RedissonConfiguration.fromConfig(redissonConfig);

  //     if (cacheManager.getCache(CACHE_NAME) == null) {
  //         cacheManager.createCache(CACHE_NAME, jcacheConfig);
  //     }
  //     return cacheManager;
  // }
}
