package dddan.paper_summary.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // 기본 CacheManager (24시간 유지)
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                "parsedMeta", "sectionList", "sectionText"
        );
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(500)
        );
        return manager;
    }

    // 짧은 TTL CacheManager (6시간 유지)
    @Bean("shortLivedCacheManager")
    public CacheManager shortLivedCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("mediaList");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .maximumSize(300)
        );
        return manager;
    }
}
