package com.hmall.gateway.filters;


import cn.hutool.core.text.AntPathMatcher;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFliter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher antPathMatcher;


    @Override
    public int getOrder() {
        return 0;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1.获取request
        ServerHttpRequest request = exchange.getRequest();
        // 2.判断是否需要做登录拦截
        if(isExclude( request.getPath().toString())){
            //放行
            return chain.filter(exchange);
            
        }
        // 3.获取Token
        String token = null;
        List<String> headers = request.getHeaders().get("authorization");
        if(headers!=null && !headers.isEmpty()){
            token = headers.get(0);
        }
        // 4.校验并解析Token
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token);
        }catch (Exception e){
            //拦截，设置状态响应码
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // TODO 5.传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        System.out.println("userId:"+userId);
        // 6.放行
        return chain.filter(swe);
    }

    private boolean isExclude(String Path) {
        for(String PathPattern : authProperties.getExcludePaths()){
            if(antPathMatcher.match(PathPattern,Path)){
                return true;
            }
        }
        return false;
    }
}
