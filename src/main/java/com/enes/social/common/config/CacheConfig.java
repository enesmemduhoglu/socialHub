package com.enes.social.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

/**
 * Redis tabanlı cache yapılandırması. Şimdilik tek cache var: kullanıcının takip
 * ettiği kişilerin id kümesi ({@link #FOLLOWEE_IDS_CACHE}) — feed sorgusunda kullanılır,
 * follow/unfollow'da geçersiz kılınır.
 *
 * <p>Değerler JDK serileştirmesiyle saklanır (varsayılan); TTL 10 dakika.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Kullanıcı id -> takip edilenlerin (kendisi dahil) id listesi. */
    public static final String FOLLOWEE_IDS_CACHE = "followeeIds";

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();
    }
}
