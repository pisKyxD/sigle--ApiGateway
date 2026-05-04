package ApiGateway.config;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    private static final String CORE_SERVICE = "https://sigle-core-service.onrender.com";
    private static final String LISTAS_SERVICE = "https://sigle-listas-service.onrender.com";
    private static final String CITAS_SERVICE = "https://api-citasservice.onrender.com";
    private static final String PACIENTES_SERVICE = "https://sigle-pacientes-service.onrender.com";

    @Bean
    public RouterFunction<ServerResponse> coreRoutes() {
        return GatewayRouterFunctions.route("core-routes")
            .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http(CORE_SERVICE))
            .route(RequestPredicates.path("/api/establecimientos/**"), HandlerFunctions.http(CORE_SERVICE))
            .route(RequestPredicates.path("/api/notificaciones/**"), HandlerFunctions.http(CORE_SERVICE))
            .route(RequestPredicates.path("/api/dashboard/**"), HandlerFunctions.http(CORE_SERVICE))
            .route(RequestPredicates.path("/api/listas/**"), HandlerFunctions.http(LISTAS_SERVICE))
            .route(RequestPredicates.path("/api/citas/**"), HandlerFunctions.http(CITAS_SERVICE))
            .route(RequestPredicates.path("/api/pacientes/**"), HandlerFunctions.http(PACIENTES_SERVICE))
            .build();
    }
}