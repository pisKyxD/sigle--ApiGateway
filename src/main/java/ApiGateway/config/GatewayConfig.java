package ApiGateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> coreRoutes() {
        return GatewayRouterFunctions.route("core-routes")
            .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http())
            .route(RequestPredicates.path("/api/establecimientos/**"), HandlerFunctions.http())
            .route(RequestPredicates.path("/api/notificaciones/**"), HandlerFunctions.http())
            .route(RequestPredicates.path("/api/dashboard/**"), HandlerFunctions.http())
            .filter(LoadBalancerFilterFunctions.lb("coreservice"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> listasRoutes() {
        return GatewayRouterFunctions.route("listas-routes")
            .route(RequestPredicates.path("/api/listas/**"), HandlerFunctions.http())
            .filter(LoadBalancerFilterFunctions.lb("listasservice"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> citasRoutes() {
        return GatewayRouterFunctions.route("citas-routes")
            .route(RequestPredicates.path("/api/citas/**"), HandlerFunctions.http())
            .filter(LoadBalancerFilterFunctions.lb("citasservice"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> pacientesRoutes() {
        return GatewayRouterFunctions.route("pacientes-routes")
            .route(RequestPredicates.path("/api/pacientes/**"), HandlerFunctions.http())
            .filter(LoadBalancerFilterFunctions.lb("pacientesservice"))
            .build();
    }
}