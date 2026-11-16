package com.cineticket.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory fixed-window rate limiter for the auth endpoints most exposed to abuse:
 * login (brute-force / credential stuffing) and register (mass account creation).
 * Keyed by client IP + path. Not distributed - fine for a single instance; a
 * multi-instance deployment behind a load balancer would need a shared store (Redis).
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record Limit(int maxRequests, long windowMillis) {
    }

    private static final Map<String, Limit> LIMITS = Map.of(
        "/api/auth/login", new Limit(10, 60_000),
        "/api/auth/register", new Limit(5, 300_000)
    );

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static class Bucket {
        private volatile long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger(0);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Limit limit = LIMITS.get(request.getRequestURI());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRequestURI() + "|" + clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket());

        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart > limit.windowMillis()) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }

            if (bucket.count.incrementAndGet() > limit.maxRequests()) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"message\":\"Too many requests, please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
