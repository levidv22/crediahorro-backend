package upeu.edu.pe.report_ms.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;


public class LoadBalancerConfiguration {
    private final Logger log = LoggerFactory.getLogger(LoadBalancerConfiguration.class);

    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier(ConfigurableApplicationContext context) {
    log.info("configuring load balancer");
    return ServiceInstanceListSupplier
            .builder()
            .withBlockingDiscoveryClient()
           // .withSameInstancePreference()
            .build(context);
    }

}
