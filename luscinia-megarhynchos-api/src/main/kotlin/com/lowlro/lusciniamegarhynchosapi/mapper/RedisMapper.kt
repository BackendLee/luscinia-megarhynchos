package com.lowlro.lusciniamegarhynchosapi.mapper

import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import reactor.core.publisher.Mono
import java.time.Duration

class RedisMapper(
    val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) {

    private fun setRedis(key: String, value: String, timeout: Long): Mono<Boolean> {
        return reactiveStringRedisTemplate.opsForValue().set(key, value, Duration.ofDays(timeout))
    }

    private fun getRedis(key: String): Mono<String> {
        return reactiveStringRedisTemplate.opsForValue().get(key)
    }

    fun setRedisByUIDAndAccessTokenInTimeOut(uid: String, accessToken: String, timeout: Long): Mono<Boolean> {
        return setRedis(uid, accessToken, timeout)
    }

    fun getRedisByUID(uid: String): Mono<String> {
        return getRedis(uid)
    }

}
