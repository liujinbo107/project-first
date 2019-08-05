package com.ljb.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * @author 刘进波
 * @create 2019-08-05 15:22
 */
public class MyGateWayFilter implements GatewayFilter, Ordered {

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=======这是===MyGateWayFilter");
        return  chain.filter(exchange);

    }

    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
