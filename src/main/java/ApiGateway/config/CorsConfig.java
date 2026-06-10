package ApiGateway.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final int MAX_REQUESTS = 30;
    private static final long WINDOW_MS = 60_000;
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String ip = request.getRemoteAddr();
                long now = System.currentTimeMillis();

                windowStart.putIfAbsent(ip, now);
                requestCounts.putIfAbsent(ip, new AtomicInteger(0));

                if (now - windowStart.get(ip) > WINDOW_MS) {
                    windowStart.put(ip, now);
                    requestCounts.get(ip).set(0);
                }

                int count = requestCounts.get(ip).incrementAndGet();
                if (count > MAX_REQUESTS) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Too Many Requests\", \"mensaje\": \"Demasiadas peticiones. Intenta en 1 minuto.\"}");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}