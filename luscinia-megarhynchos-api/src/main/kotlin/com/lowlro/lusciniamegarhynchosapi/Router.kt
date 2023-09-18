package com.lowlro.lusciniamegarhynchosapi

import com.lowlro.lusciniamegarhynchosapi.handler.CreateCharacterHandler
import com.lowlro.lusciniamegarhynchosapi.handler.UpdateCharacterHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class Router(
    val createCharacterHandler: CreateCharacterHandler,
    val updateCharacterHandler: UpdateCharacterHandler
) {
    @Bean
    fun extension(): RouterFunction<ServerResponse> {
        return router {
            path("/yggdrasil").nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    path("/extension").nest {
                        POST("/create/character") { createCharacterHandler.createCharacter(it) }
                        POST("/update/character/skin") { updateCharacterHandler.updateCharacter(it) }
                        POST("/update/character/cape") { updateCharacterHandler.updateCharacter2(it) }
                    }
                }
            }
        }
    }

}
