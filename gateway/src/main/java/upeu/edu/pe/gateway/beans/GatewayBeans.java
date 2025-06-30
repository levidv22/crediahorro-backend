package upeu.edu.pe.gateway.beans;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import upeu.edu.pe.gateway.filters.AuthFilter;

@Configuration
public class GatewayBeans {
    private final AuthFilter authFilter;

    public GatewayBeans(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }
    @Bean
    @Profile(value = "eureka-off")
    public RouteLocator routeLocatorEurekaOff (RouteLocatorBuilder builder){
        return builder
                .routes()
                .route(route -> route
                        .path("/admin-service/*")
                        .uri("http://localhost:8081")
                )
                .route(route -> route
                        .path("/report-ms/*")
                        .uri("http://localhost:8082")
                )
                .route(route -> route
                        .path("/report-grafico/*")
                        .uri("http://localhost:8082")
                )
                .build();
    }

    @Bean
    @Profile(value = "eureka-on")
    public RouteLocator routeLocatorEurekaOn (RouteLocatorBuilder builder){
        return builder
                .routes()
                .route(route -> route
                        .path("/admin-service/**")
                        .uri("lb://admin-service")  //load balance = lb = balanceo de carga
                )
                .route(route -> route
                        .path("/report-ms/**")
                        .uri("lb://report-ms")  //load balance = lb = balanceo de carga
                )
                .route(route -> route
                        .path("/report-grafico/**")
                        .uri("lb://report-grafico")  //load balance = lb = balanceo de carga
                )
                .build();
    }

    @Bean
    @Profile(value = "oauth2")
    public RouteLocator routeLocatorOauth2(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route(route -> route
                        .path("/admin-service/**")
                        .filters(filter -> filter.filter(this.authFilter))
                        .uri("lb://admin-service")  //load balance = lb = balanceo de carga
                )
                .route(route -> route
                        .path("/report-ms/**")
                        .filters(filter -> filter.filter(this.authFilter))
                        .uri("lb://report-ms")
                )
                .route(route -> route
                        .path("/report-grafico/**")
                        .filters(filter -> filter.filter(this.authFilter))
                        .uri("lb://report-grafico")
                )
                .route(route -> route
                        .path("/auth-service/auth/**")
                        .uri("lb://auth-service")
                )
                .build();
    }
}
