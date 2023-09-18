package com.lowlro.lusciniamegarhynchosapi.handler

import com.lowlro.lusciniamegarhynchosapi.controller.UserController
import com.lowlro.lusciniamegarhynchosapi.mapper.RedisMapper
import com.lowlro.lusciniamegarhynchosapi.mapper.UserMapper
import com.lowlro.lusciniamegarhynchosapi.model.User
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.util.*
import java.util.UUID.randomUUID

@Component
class CreateCharacterHandler(
    val reactiveMongoTemplate: ReactiveMongoTemplate, val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) {

    val userMapper: UserMapper = UserMapper(reactiveMongoTemplate)
    val redisMapper: RedisMapper = RedisMapper(reactiveStringRedisTemplate)

    fun createCharacter(serverRequest: ServerRequest): Mono<ServerResponse> {
        return serverRequest.bodyToMono<UserController.CreateCharacterRequest>().flatMap { createCharacterRequest ->
            val uid = createCharacterRequest.uid
            val accessToken = createCharacterRequest.accessToken
            val name = createCharacterRequest.name
            if (uid.isEmpty() || accessToken.isEmpty() || name.isEmpty()) {
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
                                val temp2 = userMapper.getUserByCharacterName(name)
                                temp2.hasElement().flatMap { temp2IsNotEmpty ->
                                    if (temp2IsNotEmpty) {
                                        badRequest().bodyValue("无效名称")
                                    } else {
                                        val character = User.Character()
                                        character.uuid =
                                            UUID.nameUUIDFromBytes(("OfflinePlayer:$name").toByteArray()).toString()
                                                .replace("-", "").replace("-", "").replace("-", "").replace("-", "")
                                        character.name = name
                                        userMapper.setUserByUIDToCharacters(uid, arrayListOf(character))
                                            .flatMap { isOk ->
                                                if (isOk.modifiedCount != 1.toLong()) {
                                                    badRequest().bodyValue("创建失败")
                                                } else {
                                                    val temp3 = userMapper.getUserByUID(uid)
                                                    temp3.hasElement().flatMap { temp3IsNotEmpty ->
                                                        if (!(temp3IsNotEmpty)) {
                                                            badRequest().bodyValue("查询失败")
                                                        } else {
                                                            temp3.flatMap { user ->
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
    }

}
