package com.loyaltyplatform.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses Render/Neon-style database URLs (postgresql://user:pass@host/db)
 * and splits them into the separate spring.datasource.* properties that
 * the PostgreSQL JDBC driver requires. Runs before any Spring beans are created.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = resolveRawUrl(environment);
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        try {
            // Normalize to a form that java.net.URI can parse
            String parseableUrl = rawUrl;
            if (parseableUrl.startsWith("jdbc:")) {
                parseableUrl = parseableUrl.substring(5);
            }
            if (parseableUrl.startsWith("postgres://")) {
                parseableUrl = "postgresql" + parseableUrl.substring("postgres".length());
            }

            URI uri = URI.create(parseableUrl);
            String host = uri.getHost();
            if (host == null) {
                return;
            }

            // Build clean JDBC URL — no user:password in the URL
            StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
            jdbcUrl.append(host);
            if (uri.getPort() > 0) {
                jdbcUrl.append(":").append(uri.getPort());
            }
            if (uri.getPath() != null) {
                jdbcUrl.append(uri.getPath());
            }
            if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
                jdbcUrl.append("?").append(uri.getRawQuery());
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", jdbcUrl.toString());

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                int colonIdx = userInfo.indexOf(':');
                if (colonIdx >= 0) {
                    properties.put("spring.datasource.username", userInfo.substring(0, colonIdx));
                    properties.put("spring.datasource.password", userInfo.substring(colonIdx + 1));
                } else {
                    properties.put("spring.datasource.username", userInfo);
                }
            }

            // addFirst = highest priority, overrides application.yml
            environment.getPropertySources().addFirst(
                    new MapPropertySource("neon-database-url", properties)
            );

        } catch (Exception ignored) {
            // If parsing fails, fall through and let Spring Boot handle it normally
        }
    }

    private String resolveRawUrl(ConfigurableEnvironment env) {
        // 1. Prefer DATABASE_URL (Render native env var)
        String url = env.getProperty("DATABASE_URL");
        if (url != null && !url.isBlank()) {
            return url;
        }

        // 2. Check SPRING_DATASOURCE_URL — handle if it's a raw postgres:// URL
        url = env.getProperty("SPRING_DATASOURCE_URL");
        if (url == null || url.isBlank()) {
            return null;
        }

        // raw libpq-style URL — needs full parsing
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            return url;
        }

        // JDBC URL with embedded credentials (jdbc:postgresql://user:pass@host/db)
        if (url.startsWith("jdbc:postgresql://") && url.contains("@")) {
            return url;
        }

        return null; // already a clean JDBC URL, no action needed
    }
}
