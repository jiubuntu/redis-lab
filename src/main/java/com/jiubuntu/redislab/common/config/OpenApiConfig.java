package com.jiubuntu.redislab.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Redis Lab API")
                        .description("Redis 학습 내용을 정리한 예제 API 모음")
                        .version("v0.0.1"));
    }
}
