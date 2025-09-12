package com.quiztournament.quiz_backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting configuration using filters
 * Compatible with Spring Boot 3
 */
@Configuration
public class RateLimitFilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter());
        registrationBean.addUrlPatterns("/api/auth/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

    /**
     * Simple rate limiting filter
     */
    public static class RateLimitFilter implements Filter {
        
        private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
        private final int MAX_REQUESTS_PER_MINUTE = 10;
        private ScheduledExecutorService scheduler;

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            scheduler = Executors.newScheduledThreadPool(1);
            // Reset counters every minute
            scheduler.scheduleAtFixedRate(() -> {
                requestCounts.clear();
            }, 1, 1, TimeUnit.MINUTES);
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String clientIP = getClientIP(httpRequest);
            AtomicInteger count = requestCounts.computeIfAbsent(clientIP, k -> new AtomicInteger(0));
            
            if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                httpResponse.setStatus(429);
                httpResponse.setHeader("Retry-After", "60");
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return;
            }
            
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }
        
        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            
            return request.getRemoteAddr();
        }
    }
}