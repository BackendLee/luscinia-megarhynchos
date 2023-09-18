package com.lowlro.lusciniamegarhynchosapi.mapper

import com.lowlro.lusciniamegarhynchosapi.model.User
import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class UserMapper(
    val reactiveMongoTemplate: ReactiveMongoTemplate
) {

    private fun queryUser(criteriaDefinition: CriteriaDefinition): Mono<User> {
        return reactiveMongoTemplate.find(Query.query(criteriaDefinition), User::class.java, "user").toMono()
    }

    private fun saveUser(user: User): Mono<User> {
        return reactiveMongoTemplate.save(user, "user")
    }

    private fun updateUser(criteriaDefinition: CriteriaDefinition, key: String, value: Any): Mono<UpdateResult> {
        return reactiveMongoTemplate.updateFirst(Query.query(criteriaDefinition), Update.update(key, value), "user")
    }

    fun getUserByEmailAndPassword(email: String, password: String): Mono<User> {
        return queryUser(Criteria.where("mail").`is`(email).and("password").`is`(password))
    }

    fun getUserByEmail(email: String): Mono<User> {
        return queryUser(Criteria.where("mail").`is`(email))
    }

    fun setUser(user: User): Mono<User> {
        return saveUser(user)
    }

    fun getUserByCharacterName(characterName: String): Mono<User> {
        return queryUser(Criteria.where("characters.name").`is`(characterName))
    }

    fun setUserByUIDToCharacters(uid: String, characters: ArrayList<User.Character>): Mono<UpdateResult> {
        return updateUser(Criteria.where("_id").`is`(uid), "characters", characters)
    }

    fun getUserByUID(uid: String): Mono<User> {
        return queryUser(Criteria.where("_id").`is`(uid))
    }

}
