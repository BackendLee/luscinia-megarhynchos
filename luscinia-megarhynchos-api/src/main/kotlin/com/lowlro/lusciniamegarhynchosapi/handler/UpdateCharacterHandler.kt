package com.lowlro.lusciniamegarhynchosapi.handler

import com.lowlro.lusciniamegarhynchosapi.controller.UserController
import com.lowlro.lusciniamegarhynchosapi.mapper.RedisMapper
import com.lowlro.lusciniamegarhynchosapi.mapper.UserMapper
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.util.UUID.randomUUID

@Component
class UpdateCharacterHandler(
    val reactiveMongoTemplate: ReactiveMongoTemplate, val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) {

    val userMapper: UserMapper = UserMapper(reactiveMongoTemplate)
    val redisMapper: RedisMapper = RedisMapper(reactiveStringRedisTemplate)

    fun updateCharacter(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<UserController.UpdateCharacterRequest>().flatMap { updateCharacterRequest ->
            val uid = updateCharacterRequest.uid
            val accessToken = updateCharacterRequest.accessToken
            val model = updateCharacterRequest.model
            val texturesSkin = updateCharacterRequest.texturesSkin
            if (uid.isEmpty() || accessToken.isEmpty() || model.isEmpty() || texturesSkin.isEmpty()) {
                badRequest().bodyValue("非法请求")
            } else {
                val temp = redisMapper.getRedisByUID(uid)
                temp.hasElement().flatMap { tempIsNotEmpty ->
                    if (!(tempIsNotEmpty)) {
                        badRequest().bodyValue("无效用户")
                    } else {
                        temp.flatMap { token ->
                            if (token != accessToken) {
                                badRequest().bodyValue("无效令牌")
                            } else {
                                var modelType = true
                                if (model == "alex") {
                                    modelType = false
                                } else if (model == "steve") {
                                    modelType = false
                                }
                                if (modelType) {
                                    badRequest().bodyValue("无效模型")
                                } else {
                                    val temp2 = userMapper.getUserByUID(uid)
                                    temp2.hasElement().flatMap { temp2IsNotEmpty ->
                                        if (!(temp2IsNotEmpty)) {
                                            badRequest().bodyValue("查询失败")
                                        } else {
                                            temp2.flatMap { user ->
                                                val character = user.characters[0]
                                                character.model = model
                                                character.texture.skin = texturesSkin
                                                userMapper.setUserByUIDToCharacters(uid, arrayListOf(character))
                                                    .flatMap { isOk ->
                                                        if (isOk.modifiedCount != 1.toLong()) {
                                                            badRequest().bodyValue("请不要重复更换相同的皮肤(或者是更换皮肤失败?)")
                                                        } else {
                                                            val newToken = randomUUID().toString().replace("-", "")
                                                            redisMapper.setRedisByUIDAndAccessTokenInTimeOut(
                                                                user._id, newToken, 30
                                                            ).flatMap { isOk2 ->
                                                                if (!(isOk2)) {
                                                                    badRequest().bodyValue("令牌更新失败")
                                                                } else {
                                                                    user.password = newToken
                                                                    ok().bodyValue(user)
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateCharacter2(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<UserController.UpdateCharacter2Request>().flatMap { updateCharacterRequest ->
            val uid = updateCharacterRequest.uid
            val accessToken = updateCharacterRequest.accessToken
            val texturesCape = updateCharacterRequest.texturesCape
            if (uid.isEmpty() || accessToken.isEmpty() || texturesCape.isEmpty()) {
                badRequest().bodyValue("非法请求")
            } else {
                val temp = redisMapper.getRedisByUID(uid)
                temp.hasElement().flatMap { tempIsNotEmpty ->
                    if (!(tempIsNotEmpty)) {
                        badRequest().bodyValue("无效用户")
                    } else {
                        temp.flatMap { token ->
                            if (token != accessToken) {
                                badRequest().bodyValue("无效令牌")
                            } else {
                                val temp2 = userMapper.getUserByUID(uid)
                                temp2.hasElement().flatMap { temp2IsNotEmpty ->
                                    if (!(temp2IsNotEmpty)) {
                                        badRequest().bodyValue("查询失败")
                                    } else {
                                        temp2.flatMap { user ->
                                            val character = user.characters[0]
                                            character.texture.cape = texturesCape
                                            userMapper.setUserByUIDToCharacters(uid, arrayListOf(character))
                                                .flatMap { isOk ->
                                                    if (isOk.modifiedCount != 1.toLong()) {
                                                        badRequest().bodyValue("请不要重复更换相同的披风(或者是更换披风失败?)")
                                                    } else {
                                                        val newToken = randomUUID().toString().replace("-", "")
                                                        redisMapper.setRedisByUIDAndAccessTokenInTimeOut(
                                                            user._id, newToken, 30
                                                        ).flatMap { isOk2 ->
                                                            if (!(isOk2)) {
                                                                badRequest().bodyValue("令牌更新失败")
                                                            } else {
                                                                user.password = newToken
                                                                ok().bodyValue(user)
                                                            }
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
