package com.lowlro.lusciniamegarhynchosapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
@ConfigurationPropertiesScan("com.lowlro.lusciniamegarhynchosapi.config")
class LusciniaMegarhynchosApiApplication

fun main(args: Array<String>) {
	runApplication<LusciniaMegarhynchosApiApplication>(*args)
}
