package com.lowlro.lusciniamegarhynchosapi

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class YggdrasilConfig : WebFluxConfigurer {
    override fun addCorsMappings(corsRegistry: CorsRegistry) {
        corsRegistry.addMapping("/**").allowCredentials(true).allowedOriginPatterns("*").allowedHeaders("*")
            .allowedMethods("*")
    }
}