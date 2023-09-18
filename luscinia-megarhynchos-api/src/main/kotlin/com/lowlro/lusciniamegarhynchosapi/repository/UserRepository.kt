package com.lowlro.lusciniamegarhynchosapi.repository

import com.lowlro.lusciniamegarhynchosapi.model.User
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.UpdateDefinition
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

@Component
class UserRepository(
    val reactiveMongoTemplate: ReactiveMongoTemplate, val reactiveStringRedisTemplate: ReactiveStringRedisTemplate
) {
    private fun opsForValue(): ReactiveValueOperations<String, String> {
        return reactiveStringRedisTemplate.opsForValue()
    }

    fun get(key: Any): Mono<String> {
        return opsForValue().get(key)
    }

    fun get(key: String, start: Long, end: Long): Mono<String> {
        return opsForValue().get(key, start, end)
    }

    fun set(key: String, value: String): Mono<Boolean> {
        return opsForValue().set(key, value)
    }

    fun set(key: String, value: String, timeout: Duration): Mono<Boolean> {
        return opsForValue().set(key, value, timeout)
    }

    fun set(key: String, value: String, offset: Long): Mono<Long> {
        return opsForValue().set(key, value, offset)
    }

    private fun <T> find(
        query: Query, entityClass: Class<T>, collectionName: String
    ): Flux<T> {
        return reactiveMongoTemplate.find(query, entityClass, collectionName)
    }

    private fun updateFirst(query: Query, update: UpdateDefinition, entityClass: Class<*>): Mono<UpdateResult> {
        return reactiveMongoTemplate.updateFirst(query, update, entityClass)
    }

    private fun updateFirst(
        query: Query, update: UpdateDefinition, entityClass: Class<*>, collectionName: String
    ): Mono<UpdateResult> {
        return reactiveMongoTemplate.updateFirst(query, update, entityClass, collectionName)
    }

    private fun updateFirst(query: Query, update: UpdateDefinition, collectionName: String): Mono<UpdateResult> {
        return reactiveMongoTemplate.updateFirst(query, update, collectionName)
    }

    private fun findUserByCriteriaDefinition(criteriaDefinition: org.springframework.data.mongodb.core.query.CriteriaDefinition): Flux<User> {
        return find(Query.query(criteriaDefinition), User::class.java, "user")
    }

    private fun updateFirstByCriteriaDefinitionAndUpdateDefinition(
        criteriaDefinition: org.springframework.data.mongodb.core.query.CriteriaDefinition,
        updateDefinition: UpdateDefinition
    ): Mono<UpdateResult> {
        return updateFirst(Query.query(criteriaDefinition), updateDefinition, "user")
    }

    fun getUserWithWByUsernameAndPassword(w: String, username: String, password: String): Mono<User> {
        return findUserByCriteriaDefinition(Criteria.where(w).`is`(username).and("password").`is`(password)).toMono()
    }

    private fun updateFirstUpdateResultByCriteriaDefinitionAndUpdate(
        criteriaDefinition: org.springframework.data.mongodb.core.query.CriteriaDefinition, key: String, value: Any
    ): Mono<UpdateResult> {
        return updateFirstByCriteriaDefinitionAndUpdateDefinition(criteriaDefinition, Update.update(key, value))
    }

    fun setTokenById(_id: String, token: ArrayList<User.Token>): Mono<UpdateResult> {
        return updateFirstUpdateResultByCriteriaDefinitionAndUpdate(Criteria.where("_id").`is`(_id), "tokens", token)
    }

    /**
     * 查询数据库
     * sql: token.accessToken = accessToken
     * @param accessToken 访问令牌
     * @return 用户数据实体类
     */
    fun getUserByAccessToken(accessToken: String): Mono<User> {
        return findUserByCriteriaDefinition(Criteria.where("tokens.access").`is`(accessToken)).toMono()
    }

    /**
     * 查询数据库
     * sql1: characters.uuid = uuid
     * sql2: characters.name = name
     * @param uuid 角色标识符
     * @param name 角色名字
     * @return 用户数据实体类
     */
    fun getUserWithCharacterByUUIDAndName(uuid: String, name: String): Mono<User> {
        return findUserByCriteriaDefinition(
            Criteria.where("characters.uuid").`is`(uuid).and("characters.name").`is`(name)
        ).toMono()
    }

    /**
     * 查询数据库
     * sql: characters.uuid = uuid
     * @param uuid 角色标识符
     * @return 用户数据实体类
     */
    fun getUserWithCharacterByUUID(uuid: String): Mono<User> {
        return findUserByCriteriaDefinition(Criteria.where("characters.uuid").`is`(uuid)).toMono()
    }

    /**
     * 查询数据库
     * sql: characters.name = name
     * @param name 角色名字
     * @return 用户数据实体类
     */
    fun getUserWithCharacterByName(name: String): Mono<User> {
        return findUserByCriteriaDefinition(Criteria.where("characters.name").`is`(name)).toMono()
    }

    /**
     * 查询数据库
     * sql: mail = mail
     * @param mail 邮箱
     * @return 用户数据实体类
     */
    fun getUserByMail(mail: String): Mono<User> {
        return findUserByCriteriaDefinition(Criteria.where("mail").`is`(mail)).toMono()
    }

    /**
     * 查询数据库
     * sql1: mail = mail
     * sql2: password = password
     * @param mail 邮箱
     * @param password 密码
     * @return 用户数据实体类
     */
    fun getUseByMailAndPassword(mail: String, password: String): Mono<User> {
        return findUserByCriteriaDefinition(
            Criteria.where("mail").`is`(mail).and("password").`is`(password)
        ).toMono()
    }

    fun saveUser(user: User): Mono<User> {
        return reactiveMongoTemplate.save(user, "user")
    }
}