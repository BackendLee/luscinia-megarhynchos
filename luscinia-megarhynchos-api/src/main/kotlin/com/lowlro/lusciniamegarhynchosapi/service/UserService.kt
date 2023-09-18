package com.lowlro.lusciniamegarhynchosapi.service

import com.google.gson.Gson
import com.lowlro.lusciniamegarhynchosapi.config.Mail
import com.lowlro.lusciniamegarhynchosapi.config.Yggdrasil
import com.lowlro.lusciniamegarhynchosapi.controller.UserController
import com.lowlro.lusciniamegarhynchosapi.controller.advice.UserControllerAdvice
import com.lowlro.lusciniamegarhynchosapi.model.User
import com.lowlro.lusciniamegarhynchosapi.repository.UserRepository
import com.lowlro.lusciniamegarhynchosapi.util.DigitalSignatureUtil
import com.lowlro.lusciniamegarhynchosapi.util.MailUtil
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.random.Random

@Component
class UserService(
    val userRepository: UserRepository, val yggdrasil: Yggdrasil, val mail: Mail
) {
    fun index(): Mono<LinkedHashMap<String, Any>> {
        val meta = linkedMapOf<String, Any>()
        meta["implementationVersion"] = "0.0.1-SNAPSHOT"
        meta["serverName"] = yggdrasil.core.serverName
        meta["implementationName"] = yggdrasil.core.serverName
        meta["feature.non_email_login"] = yggdrasil.core.loginWithCharacterName
        val body = linkedMapOf<String, Any>()
        body["skinDomains"] = yggdrasil.core.skinDomains
        body["signaturePublickey"] =
            "-----BEGIN PUBLIC KEY-----${yggdrasil.signatureKey.public}-----END PUBLIC KEY-----"
        body["meta"] = meta
        return Mono.just(body)
    }

    fun authenticate(authenticateRequest: UserController.AuthenticateRequest): Mono<LinkedHashMap<String, Any>> {
        return userRepository.get(authenticateRequest.username).hasElement().flatMap { rate ->
            if (rate) {
                Mono.error(UserControllerAdvice.UserRateException())
            } else {
                userRepository.set(
                    authenticateRequest.username,
                    authenticateRequest.clientToken,
                    Duration.ofMillis(yggdrasil.rateLimit.limitDuration)
                ).flatMap { redis ->
                    if (!redis) {
                        Mono.error(UserControllerAdvice.UserRedisException())
                    } else {
                        val w = if (authenticateRequest.username.indexOf("@") <= 0) {
                            if (yggdrasil.core.loginWithCharacterName) {
                                "characters.name"
                            } else {
                                "mail"
                            }
                        } else {
                            "mail"
                        }
                        val data = userRepository.getUserWithWByUsernameAndPassword(
                            w, authenticateRequest.username, authenticateRequest.password
                        )
                        data.hasElement().flatMap { boolean ->
                            if (!boolean) {
                                Mono.error(UserControllerAdvice.UserMongoException())
                            } else {
                                data.flatMap { user ->

                                    val newToken = User.Token()
                                    newToken.access = UUID.randomUUID().toString().replace("-", "")
                                    newToken.client = authenticateRequest.clientToken.ifEmpty {
                                        UUID.randomUUID().toString().replace("-", "")
                                    }
                                    val response = linkedMapOf<String, Any>()
                                    response["accessToken"] = newToken.access
                                    response["clientToken"] = newToken.client

                                    val ap = arrayListOf(linkedMapOf<String, String>())
                                    ap.clear()
                                    user.characters.forEach { c ->
                                        val p = linkedMapOf<String, String>()
                                        if (c.uuid.isNotEmpty() && c.name.isNotEmpty()) {
                                            p["id"] = c.uuid
                                            p["name"] = c.name
                                            ap.add(p)
                                        }
                                    }

                                    response["availableProfiles"] = ap

                                    val sp = linkedMapOf<String, String>()
                                    if (authenticateRequest.username.indexOf("@") == -1) {
                                        user.characters.forEach { c ->
                                            if (c.name == authenticateRequest.username) {
                                                sp["id"] = c.uuid
                                                sp["name"] = c.name
                                            }
                                        }
                                    } else {
                                        if (user.characters.size == 1 && user.characters[0].uuid.isNotEmpty() && user.characters[0].name.isNotEmpty()) {
                                            sp["id"] = user.characters[0].uuid
                                            sp["name"] = user.characters[0].name
                                        }
                                    }

                                    if (sp.isNotEmpty()) {

                                        val bc = User.Token.BoundCharacter()
                                        bc.uuid = sp["id"].toString()
                                        bc.name = sp["name"].toString()

                                        newToken.boundCharacter = bc
                                        response["selectedProfile"] = sp
                                    }
                                    if (authenticateRequest.requestUser) {

                                        val ur = linkedMapOf<String, Any>()
                                        val prop = arrayListOf(linkedMapOf<String, String>())
                                        prop.clear()
                                        user.properties.forEach { p ->
                                            val d = linkedMapOf<String, String>()
                                            if (p.name.isNotEmpty() && p.value.isNotEmpty()) {
                                                d["name"] = p.name
                                                d["value"] = p.value
                                                prop.add(d)
                                            }
                                        }
                                        ur["id"] = user._id
                                        ur["properties"] = prop

                                        response["user"] = ur
                                    }
                                    newToken.createAt = Date().getFormatTime("yyyyMMddHHmmss")

                                    val oldToken = user.tokens
                                    if (oldToken.size < 1) {
                                        oldToken.add(newToken)
                                    } else {
                                        oldToken.removeAt(0)
                                        oldToken.add(newToken)
                                    }

                                    userRepository.setTokenById(
                                        user._id, oldToken
                                    ).flatMap { hasElement4 ->
                                        if (hasElement4.modifiedCount != 1.toLong()) {
                                            Mono.error(UserControllerAdvice.UserMongoTokenException())
                                        } else {
                                            Mono.just(response)
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

    fun refresh(refreshRequest: UserController.RefreshRequest): Mono<LinkedHashMap<String, Any>> {
        return if (refreshRequest.accessToken == "") {
            Mono.error(UserControllerAdvice.RequestAccessTokenException())
        } else {
            val userData = userRepository.getUserByAccessToken(refreshRequest.accessToken)
            userData.hasElement().flatMap { userDataIsNotEmpty ->
                if (!userDataIsNotEmpty) {
                    Mono.error(UserControllerAdvice.UserAccessTokenException())
                } else {
                    userData.flatMap { user ->
                        val oldToken = oldToken(user.tokens, refreshRequest.accessToken)
                        if (refreshRequest.clientToken != "" && refreshRequest.clientToken != oldToken.client) {
                            Mono.error(UserControllerAdvice.UserClientTokenException())
                        } else {
                            val newToken = User.Token()
                            newToken.access = UUID.randomUUID().toString().replace("-", "")
                            if (refreshRequest.selectedProfile.id == "" && refreshRequest.selectedProfile.name == "") {
                                newToken.boundCharacter = oldToken.boundCharacter
                                if (refreshRequest.clientToken == "") {
                                    newToken.client = oldToken.client
                                } else {
                                    newToken.client = refreshRequest.clientToken
                                }
                                newToken.createAt = Date().getFormatTime("yyyyMMddHHmmss")
                                val response = linkedMapOf<String, Any>()
                                response.clear()
                                response["accessToken"] = newToken.access
                                response["clientToken"] = newToken.client
                                if (newToken.boundCharacter.uuid != "" && newToken.boundCharacter.name != "") {
                                    response["selectedProfile"] = responseSelectedProfile(newToken.boundCharacter)
                                }
                                if (refreshRequest.requestUser) {
                                    response["user"] = user(user.properties, user._id)
                                }
                                val token = user.tokens
                                if (token.size < 1) {
                                    token.add(newToken)
                                } else {
                                    token.removeAt(0)
                                    token.add(newToken)
                                }
                                userRepository.setTokenById(user._id, token).flatMap { success ->
                                    if (success.modifiedCount != 1.toLong()) {
                                        Mono.error(UserControllerAdvice.UserMongoTokenException())
                                    } else {
                                        Mono.just(response)
                                    }
                                }
                            } else {
                                val characterData = userRepository.getUserWithCharacterByUUIDAndName(
                                    refreshRequest.selectedProfile.id, refreshRequest.selectedProfile.name
                                )
                                characterData.hasElement().flatMap { characterDataIsNotEmpty ->
                                    if (!characterDataIsNotEmpty) {
                                        Mono.error(UserControllerAdvice.UserAccessTokenHasProfileException())
                                    } else {
                                        characterData.flatMap { character ->
                                            if (oldToken.boundCharacter.uuid != "" && oldToken.boundCharacter.name != "") {
                                                Mono.error(UserControllerAdvice.UserAccessTokenHasProfileException())
                                            } else if (user._id != character._id) {
                                                Mono.error(UserControllerAdvice.UserAccessTokenBindNotHaveException())
                                            } else {
                                                val boundCharacter = User.Token.BoundCharacter()
                                                boundCharacter.uuid = refreshRequest.selectedProfile.id
                                                boundCharacter.name = refreshRequest.selectedProfile.name
                                                newToken.boundCharacter = boundCharacter
                                                if (refreshRequest.clientToken == "") {
                                                    newToken.client = oldToken.client
                                                } else {
                                                    newToken.client = refreshRequest.clientToken
                                                }
                                                newToken.createAt = Date().getFormatTime("yyyyMMddHHmmss")
                                                val response = linkedMapOf<String, Any>()
                                                response.clear()
                                                response["accessToken"] = newToken.access
                                                response["clientToken"] = newToken.client
                                                if (newToken.boundCharacter.uuid != "" && newToken.boundCharacter.name != "") {
                                                    response["selectedProfile"] =
                                                        responseSelectedProfile(newToken.boundCharacter)
                                                }
                                                if (refreshRequest.requestUser) {
                                                    response["user"] = user(user.properties, user._id)
                                                }
                                                val token = user.tokens
                                                if (token.size < 1) {
                                                    token.add(newToken)
                                                } else {
                                                    token.removeAt(0)
                                                    token.add(newToken)
                                                }
                                                userRepository.setTokenById(user._id, token).flatMap { success ->
                                                    if (success.modifiedCount != 1.toLong()) {
                                                        Mono.error(UserControllerAdvice.UserMongoTokenException())
                                                    } else {
                                                        Mono.just(response)
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

    fun validate(validateRequest: UserController.ValidateRequest): Mono<LinkedHashMap<String, Any>> {
        return if (validateRequest.accessToken == "") {
            Mono.error(UserControllerAdvice.RequestAccessTokenException())
        } else {
            val userData = userRepository.getUserByAccessToken(validateRequest.accessToken)
            userData.hasElement().flatMap { userDataIsNotEmpty ->
                if (!userDataIsNotEmpty) {
                    Mono.error(UserControllerAdvice.UserAccessTokenException())
                } else {
                    userData.flatMap { user ->
                        val oldToken = oldToken(user.tokens, validateRequest.accessToken)
                        if (validateRequest.clientToken != "" && validateRequest.clientToken != oldToken.client) {
                            Mono.error(UserControllerAdvice.UserClientTokenException())
                        } else {
                            Mono.error(UserControllerAdvice.NOCONTENT())
                        }
                    }
                }
            }
        }
    }

    fun invalidate(invalidateRequest: UserController.InvalidateRequest): Mono<LinkedHashMap<String, Any>> {
        return if (invalidateRequest.accessToken == "") {
            Mono.error(UserControllerAdvice.RequestAccessTokenException())
        } else {
            val userData = userRepository.getUserByAccessToken(invalidateRequest.accessToken)
            userData.hasElement().flatMap { userDataIsNotEmpty ->
                if (!userDataIsNotEmpty) {
                    Mono.error(UserControllerAdvice.NOCONTENT())
                } else {
                    userData.flatMap { user ->
                        val oldToken = oldToken(user.tokens, invalidateRequest.accessToken)
                        val newToken = User.Token()
                        newToken.access = UUID.randomUUID().toString().replace("-", "")
                        newToken.boundCharacter = oldToken.boundCharacter
                        if (invalidateRequest.clientToken == "") {
                            newToken.client = oldToken.client
                        } else {
                            newToken.client = invalidateRequest.clientToken
                        }
                        newToken.createAt = Date().getFormatTime("yyyyMMddHHmmss")
                        val response = linkedMapOf<String, Any>()
                        response.clear()
                        response["accessToken"] = newToken.access
                        response["clientToken"] = newToken.client
                        if (newToken.boundCharacter.uuid != "" && newToken.boundCharacter.name != "") {
                            response["selectedProfile"] = responseSelectedProfile(newToken.boundCharacter)
                        }
                        val token = user.tokens
                        if (token.size < 1) {
                            token.add(newToken)
                        } else {
                            token.removeAt(0)
                            token.add(newToken)
                        }
                        userRepository.setTokenById(user._id, token).flatMap { success ->
                            if (success.modifiedCount != 1.toLong()) {
                                Mono.error(UserControllerAdvice.NOCONTENT())
                            } else {
                                Mono.error(UserControllerAdvice.NOCONTENT())
                            }
                        }
                    }
                }
            }
        }
    }

    fun signout(signoutRequest: UserController.SignoutRequest): Mono<LinkedHashMap<String, Any>> {
        return userRepository.get(signoutRequest.username).hasElement().flatMap { hasElement1 ->
            if (hasElement1) {
                Mono.error(UserControllerAdvice.UserRateException())
            } else {
                userRepository.set(
                    signoutRequest.username,
                    signoutRequest.username,
                    Duration.ofMillis(yggdrasil.rateLimit.limitDuration)
                ).flatMap { hasElement2 ->
                    if (!(hasElement2)) {
                        Mono.error(UserControllerAdvice.UserRedisException())
                    } else {
                        val w = if (signoutRequest.username.indexOf("@") <= 0) {
                            if (yggdrasil.core.loginWithCharacterName) {
                                "characters.name"
                            } else {
                                "mail"
                            }
                        } else {
                            "mail"
                        }

                        val mongodb = userRepository.getUserWithWByUsernameAndPassword(
                            w, signoutRequest.username, signoutRequest.password
                        )
                        mongodb.hasElement().flatMap { hasElement3 ->
                            if (!(hasElement3)) {
                                Mono.error(UserControllerAdvice.UserMongoException())
                            } else {
                                mongodb.flatMap { userMongoDB ->
                                    userRepository.setTokenById(
                                        userMongoDB._id, token(
                                            arrayListOf(
                                                User.Token()
                                            ), User.Token()
                                        )
                                    ).flatMap { hasElement4 ->
                                        if (hasElement4.modifiedCount != 1.toLong()) {
                                            Mono.error(UserControllerAdvice.NOCONTENT())
                                        } else {
                                            Mono.error(UserControllerAdvice.NOCONTENT())
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

    fun join(joinRequest: UserController.JoinRequest): Mono<LinkedHashMap<String, Any>> {
        val query = userRepository.getUserByAccessToken(joinRequest.accessToken)
        return query.hasElement().flatMap { hasElement1 ->
            if (!(hasElement1)) {
                Mono.error(UserControllerAdvice.UserAccessTokenException())
            } else {
                query.flatMap { user ->
                    if (!(joinGetToken(user.tokens, joinRequest.accessToken, joinRequest.selectedProfile))) {
                        Mono.error(UserControllerAdvice.UserAccessTokenBindNotHaveException())
                    } else {
                        joinSetRequest(
                            joinRequest.serverId, joinRequest.accessToken, "0.0.0.0"
                        ).flatMap { hasElement2 ->
                            if (!(hasElement2)) {
                                Mono.error(UserControllerAdvice.UserRedisException())
                            } else {
                                Mono.error(UserControllerAdvice.NOCONTENT())
                            }
                        }
                    }
                }
            }
        }
    }

    fun hasJoined(username: String, serverId: String, ip: String): Mono<LinkedHashMap<String, Any>> {
        val redis = userRepository.get(serverId)
        return redis.hasElement().flatMap { hasElement1 ->
            if (!(hasElement1)) {
                Mono.error(UserControllerAdvice.NOCONTENT())
            } else {
                redis.flatMap { r ->
                    val data = r.replace("[", "").replace("]", "").replace("\"", "").split(",")
                    val accessToken = data[0]
                    val ipAddress = data[1]
                    if (ip.isNotEmpty() && ip != ipAddress) {
                        Mono.error(UserControllerAdvice.UserIPException())
                    } else {
                        val mongodb = userRepository.getUserByAccessToken(accessToken)
                        mongodb.hasElement().flatMap { hasElement1 ->
                            if (!(hasElement1)) {
                                Mono.error(UserControllerAdvice.UserAccessTokenException())
                            } else {
                                mongodb.flatMap { user ->
                                    var flag = true
                                    var c = User.Character()
                                    user.characters.forEach { character ->
                                        if (character.name == username) {
                                            flag = false
                                            c = character
                                        }
                                    }
                                    if (flag) {
                                        Mono.error(UserControllerAdvice.NOCONTENT())
                                    } else {
                                        val uploadableTextures = linkedMapOf<String, Any>()
                                        uploadableTextures["name"] = "uploadableTextures"
                                        var value =
                                            "?".replace("\"", "").replace("[", "").replace("]", "").replace(" ", "")
                                        uploadableTextures["value"] = value
                                        uploadableTextures["signature"] = DigitalSignatureUtil(yggdrasil).sign(value)

                                        val textures = linkedMapOf<String, Any>()
                                        textures["name"] = "textures"
                                        value = textures(c)
                                        textures["value"] = value
                                        textures["signature"] = DigitalSignatureUtil(yggdrasil).sign(value)

                                        val response = linkedMapOf<String, Any>()
                                        response["id"] = c.uuid
                                        response["name"] = c.name
                                        response["properties"] = arrayListOf(textures, uploadableTextures)

                                        Mono.just(response)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun minecraftProfile(uuid: String, unsigned: String): Mono<LinkedHashMap<String, Any>> {
        val mongodb = userRepository.getUserWithCharacterByUUID(uuid)
        return mongodb.hasElement().flatMap { hasElement1 ->
            if (!(hasElement1)) {
                Mono.error(UserControllerAdvice.NOCONTENT())
            } else {
                mongodb.flatMap { user ->
                    val response = linkedMapOf<String, Any>()
                    user.characters.forEach { character ->
                        if (character.uuid == uuid) {
                            val uploadableTextures = linkedMapOf<String, Any>()
                            uploadableTextures["name"] = "uploadableTextures"
                            var value = "?".replace("\"", "").replace("[", "").replace("]", "").replace(" ", "")
                            uploadableTextures["value"] = value
                            if (unsigned == "false") {
                                uploadableTextures["signature"] = DigitalSignatureUtil(yggdrasil).sign(value)
                            }
                            val textures = linkedMapOf<String, Any>()
                            textures["name"] = "textures"
                            value = textures(character)
                            textures["value"] = value
                            if (unsigned == "false") {
                                textures["signature"] = DigitalSignatureUtil(yggdrasil).sign(value)
                            }
                            response["id"] = character.uuid
                            response["name"] = character.name
                            response["properties"] = arrayListOf(textures, uploadableTextures)
                        }
                    }
                    Mono.just(response)
                }
            }
        }
    }

    fun minecraft(requestBody: ArrayList<String>): Mono<out ArrayList<out Serializable>> {
        val response = arrayListOf(linkedMapOf<String, String>())
        response.clear()
        if (requestBody.size == 1) {
            val data = userRepository.getUserWithCharacterByName(requestBody[0])
            return data.hasElement().flatMap { hasElement1 ->
                if (!(hasElement1)) {
                    Mono.just(arrayListOf<String>())
                } else {
                    data.flatMap { userMongoDB1 ->
                        response.add(minecraftCharacter(userMongoDB1, requestBody[0]))
                        Mono.just(response)
                    }
                }
            }
        } else if (requestBody.size == 2) {
            if (requestBody[0] == requestBody[1]) {
                val data = userRepository.getUserWithCharacterByName(requestBody[0])
                return data.hasElement().flatMap { hasElement1 ->
                    if (!(hasElement1)) {
                        Mono.just(arrayListOf<String>())
                    } else {
                        data.flatMap { userMongoDB1 ->
                            response.add(minecraftCharacter(userMongoDB1, requestBody[0]))
                            Mono.just(response)
                        }
                    }
                }
            } else {
                val data1 = userRepository.getUserWithCharacterByName(requestBody[0])
                val data2 = userRepository.getUserWithCharacterByName(requestBody[1])
                return data1.hasElement().flatMap { hasElement1 ->
                    if (!(hasElement1)) {
                        data2.hasElement().flatMap { hasElement2 ->
                            if (!(hasElement2)) {
                                Mono.just(arrayListOf<String>())
                            } else {
                                data2.flatMap { userMongoDB2 ->
                                    response.add(minecraftCharacter(userMongoDB2, requestBody[1]))
                                    Mono.just(response)
                                }
                            }
                        }
                    } else {
                        data1.flatMap { userMongoDB1 ->
                            response.add(minecraftCharacter(userMongoDB1, requestBody[0]))
                            data2.hasElement().flatMap { hasElement2 ->
                                if (!(hasElement2)) {
                                    Mono.just(response)
                                } else {
                                    data2.flatMap { userMongoDB2 ->
                                        response.add(minecraftCharacter(userMongoDB2, requestBody[1]))
                                        Mono.just(response)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (requestBody.size == 3) {
            val data1 = userRepository.getUserWithCharacterByName(requestBody[0])
            val data2 = userRepository.getUserWithCharacterByName(requestBody[1])
            val data3 = userRepository.getUserWithCharacterByName(requestBody[2])
            return data1.hasElement().flatMap { hasElement1 ->
                if (!(hasElement1)) {
                    data2.hasElement().flatMap { hasElement2 ->
                        if (!(hasElement2)) {
                            data3.hasElement().flatMap { hasElement3 ->
                                if (!(hasElement3)) {
                                    Mono.just(arrayListOf<String>())
                                } else {
                                    data3.flatMap { userMongoDB3 ->
                                        response.add(minecraftCharacter(userMongoDB3, requestBody[2]))
                                        Mono.just(response)
                                    }
                                }
                            }
                        } else {
                            data2.flatMap { userMongoDB2 ->
                                response.add(minecraftCharacter(userMongoDB2, requestBody[1]))
                                data3.hasElement().flatMap { hasElement3 ->
                                    if (!(hasElement3)) {
                                        Mono.just(response)
                                    } else {
                                        data3.flatMap { userMongoDB3 ->
                                            response.add(minecraftCharacter(userMongoDB3, requestBody[2]))
                                            Mono.just(response)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    data1.flatMap { userMongoDB1 ->
                        response.add(minecraftCharacter(userMongoDB1, requestBody[0]))
                        data2.hasElement().flatMap { hasElement2 ->
                            if (!(hasElement2)) {
                                data3.hasElement().flatMap { hasElement3 ->
                                    if (!(hasElement3)) {
                                        Mono.just(arrayListOf<String>())
                                    } else {
                                        data3.flatMap { userMongoDB3 ->
                                            response.add(minecraftCharacter(userMongoDB3, requestBody[2]))
                                            Mono.just(response)
                                        }
                                    }
                                }
                            } else {
                                data2.flatMap { userMongoDB2 ->
                                    response.add(minecraftCharacter(userMongoDB2, requestBody[1]))
                                    data3.hasElement().flatMap { hasElement3 ->
                                        if (!(hasElement3)) {
                                            Mono.just(response)
                                        } else {
                                            data3.flatMap { userMongoDB3 ->
                                                response.add(minecraftCharacter(userMongoDB3, requestBody[2]))
                                                Mono.just(response)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            return Mono.just(arrayListOf<String>())
        }
    }

    private fun minecraftCharacter(user: User, name: String): LinkedHashMap<String, String> {
        user.characters.forEach { character ->
            if (character.name == name) {
                val c = linkedMapOf<String, String>()
                c["id"] = character.uuid
                c["name"] = character.name
                return c
            }
        }
        return linkedMapOf()
    }

    private fun textures(character: User.Character): String {
        val metadata = linkedMapOf<String, Any>()
        if (character.model == "steve") {
            metadata["model"] = "default"
        }
        if (character.model == "alex") {
            metadata["model"] = "slim"
        }
        val skin = linkedMapOf<String, Any>()
        skin["url"] = character.texture.skin
        skin["metadata"] = metadata
        val cape = linkedMapOf<String, Any>()
        cape["url"] = character.texture.cape
        val textures = linkedMapOf<String, Any>()
        if (character.texture.skin != "") {
            textures["SKIN"] = skin
        }
        if (character.texture.cape != "") {
            textures["CAPE"] = cape
        }
        val response = linkedMapOf<String, Any>()
        response["timestamp"] = Instant.now().toEpochMilli()
        response["profileId"] = character.uuid
        response["profileName"] = character.name
        response["textures"] = textures
        return Base64.getEncoder().encodeToString(Gson().toJson(response).toString().toByteArray())
    }

    private fun joinSetRequest(serverId: String, accessToken: String, ip: String): Mono<Boolean> {
        return userRepository.set(serverId, "[\"$accessToken\",\"$ip\"]", Duration.ofMinutes(3))
    }

    private fun joinGetToken(token: ArrayList<User.Token>, accessToken: String, selectedProfile: String): Boolean {
        token.forEach { t ->
            if (t.access == accessToken && t.boundCharacter.uuid == selectedProfile) {
                return true
            }
        }
        return false
    }

    private fun token(oldToken: ArrayList<User.Token>, newToken: User.Token): ArrayList<User.Token> {
        if (oldToken.size < 1) {
            oldToken.add(newToken)
        } else {
            oldToken.removeAt(0)
            oldToken.add(newToken)
        }
        return oldToken
    }

    private fun oldToken(token: ArrayList<User.Token>, accessToken: String): User.Token {
        token.forEach { t ->
            if (t.access == accessToken) {
                return t
            }
        }
        return User.Token()
    }

    private fun responseSelectedProfile(boundCharacter: User.Token.BoundCharacter): LinkedHashMap<String, Any> {
        val response = linkedMapOf<String, Any>()
        response["id"] = boundCharacter.uuid
        response["name"] = boundCharacter.name
        return response
    }

    private fun user(properties: ArrayList<User.Propertie>, id: String): LinkedHashMap<String, Any> {
        val user = linkedMapOf<String, Any>()
        val prop = arrayListOf(linkedMapOf<String, String>())
        prop.clear()
        properties.forEach { p ->
            val data = linkedMapOf<String, String>()
            if (p.name.isNotEmpty() && p.value.isNotEmpty()) {
                data["name"] = p.name
                data["value"] = p.value
                prop.add(data)
            }
        }
        user["id"] = id
        user["properties"] = prop
        return user
    }

    private fun Date.getFormatTime(format: String = ""): String {
        val ft: String = format
        val sdf = if (ft.isNotEmpty()) SimpleDateFormat(ft)
        else SimpleDateFormat("yyyyMMddHHmmss")
        return sdf.format(this)
    }

    fun extensionApiUserRegisterCaptcha(extensionApiUserRegisterCaptchaRequest: UserController.ExtensionApiUserRegisterCaptchaRequest): Mono<LinkedHashMap<String, Any>> {
        val response = linkedMapOf<String, Any>()
        return if (extensionApiUserRegisterCaptchaRequest.email.isEmpty()) {
            response["message"] = "邮箱地址不能为空！"
            Mono.just(response)
        } else {
            if (!("[a-zA-Z0-9._-]+@[a-zA-Z0-9-]+\\.[a-zA-Z.]{2,18}".toRegex()
                    .containsMatchIn(input = extensionApiUserRegisterCaptchaRequest.email))
            ) {
                response["message"] = "邮箱地址不符合要求！"
                Mono.just(response)
            } else {
                userRepository.get("rate:${extensionApiUserRegisterCaptchaRequest.email}").hasElement()
                    .flatMap { rate ->
                        if (rate) {
                            response["message"] = "请等待一分钟后再试。"
                            Mono.just(response)
                        } else {
                            userRepository.set(
                                "rate:${extensionApiUserRegisterCaptchaRequest.email}",
                                extensionApiUserRegisterCaptchaRequest.email,
                                Duration.ofMinutes(1)
                            ).flatMap { redis ->
                                if (!redis) {
                                    response["message"] = "缓存服务失败，请联系管理员。"
                                    Mono.just(response)
                                } else {
                                    val user =
                                        userRepository.getUserByMail(extensionApiUserRegisterCaptchaRequest.email)
                                    user.hasElement().flatMap { haveData ->
                                        if (haveData) {
                                            response["message"] = "邮件地址已经被注册了，请尝试更换新一个邮箱。"
                                            Mono.just(response)
                                        } else {
                                            val characterSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                            val random = Random(System.nanoTime())
                                            val password = StringBuilder()
                                            for (i in 0 until 6) {
                                                password.append(characterSet[random.nextInt(characterSet.length)])
                                            }
                                            val captcha = password.toString()
                                            userRepository.set(
                                                "captcha:${extensionApiUserRegisterCaptchaRequest.email}",
                                                captcha,
                                                Duration.ofMinutes(5)
                                            ).flatMap { setRedis ->
                                                if (!(setRedis)) {
                                                    response["message"] = "缓存服务失败，请联系管理员。"
                                                    Mono.just(response)
                                                } else {
                                                    val smtp = MailUtil(mail).send(
                                                        extensionApiUserRegisterCaptchaRequest.email, captcha
                                                    )
                                                    if (!(smtp)) {
                                                        response["message"] = "邮件发送失败，请联系管理员。"
                                                        Mono.just(response)
                                                    } else {
                                                        response["message"] = "邮件发送成功，请注意查收。"
                                                        Mono.just(response)
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

    fun extensionApiUserRegister(extensionApiUserRegisterRequest: UserController.ExtensionApiUserRegisterRequest): Mono<out Any> {
        val response = linkedMapOf<String, Any>()
        return if (extensionApiUserRegisterRequest.email.isEmpty() || extensionApiUserRegisterRequest.password.isEmpty() || extensionApiUserRegisterRequest.captcha.isEmpty()) {
            response["message"] = "非法请求！"
            Mono.just(response)
        } else {
            if (!("[a-zA-Z0-9._-]+@[a-zA-Z0-9-]+\\.[a-zA-Z.]{2,18}".toRegex()
                    .containsMatchIn(input = extensionApiUserRegisterRequest.email))
            ) {
                response["message"] = "邮箱地址不符合要求！"
                Mono.just(response)
            } else {
                userRepository.get("rate:${extensionApiUserRegisterRequest.email}").hasElement().flatMap { rate ->
                    if (rate) {
                        response["message"] = "请求过快！"
                        Mono.just(response)
                    } else {
                        userRepository.set(
                            "rate:${extensionApiUserRegisterRequest.email}",
                            extensionApiUserRegisterRequest.email,
                            Duration.ofMillis(yggdrasil.rateLimit.limitDuration)
                        ).flatMap { redis ->
                            if (!redis) {
                                response["message"] = "缓存服务失败，请联系管理员。"
                                Mono.just(response)
                            } else {
                                val captchaRedis =
                                    userRepository.get("captcha:${extensionApiUserRegisterRequest.email}")
                                captchaRedis.hasElement().flatMap { captchaHave ->
                                    if (!(captchaHave)) {
                                        response["message"] = "验证码不存在，请重新获取。"
                                        Mono.just(response)
                                    } else {
                                        captchaRedis.flatMap { captcha ->
                                            if (extensionApiUserRegisterRequest.captcha != captcha) {
                                                response["message"] = "验证码不正确，请重新尝试。"
                                                Mono.just(response)
                                            } else {
                                                val userData =
                                                    userRepository.getUserByMail(extensionApiUserRegisterRequest.email)
                                                userData.hasElement().flatMap { userIsNotEmpty ->
                                                    if (userIsNotEmpty) {
                                                        response["message"] = "该邮箱已被注册，请尝试更换一个新的邮箱。"
                                                        Mono.just(response)
                                                    } else {
                                                        val user = User()
                                                        user._id = UUID.randomUUID().toString().replace("-", "")
                                                        user.mail = extensionApiUserRegisterRequest.email
                                                        user.password = extensionApiUserRegisterRequest.password
                                                        val temp2 = userRepository.saveUser(user)
                                                        temp2.hasElement().flatMap { temp2IsNotEmpty ->
                                                            if (!(temp2IsNotEmpty)) {
                                                                response["message"] = "数据服务失败，请联系管理员。"
                                                                Mono.just(response)
                                                            } else {
                                                                getUser(temp2)
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

    fun extensionApiUserLogin(extensionApiUserLoginRequest: UserController.ExtensionApiUserLoginRequest): Mono<out Any> {
        val response = linkedMapOf<String, Any>()
        return if (extensionApiUserLoginRequest.email.isEmpty() || extensionApiUserLoginRequest.password.isEmpty()) {
            response["message"] = "非法请求！"
            Mono.just(response)
        } else {
            userRepository.get("rate:${extensionApiUserLoginRequest.email}").hasElement().flatMap { rate ->
                if (rate) {
                    response["message"] = "请求过快！"
                    Mono.just(response)
                } else {
                    userRepository.set(
                        "rate:${extensionApiUserLoginRequest.email}",
                        extensionApiUserLoginRequest.email,
                        Duration.ofMillis(yggdrasil.rateLimit.limitDuration)
                    ).flatMap { redis ->
                        if (!redis) {
                            response["message"] = "缓存服务失败，请联系管理员。"
                            Mono.just(response)
                        } else {
                            val temp = userRepository.getUseByMailAndPassword(
                                extensionApiUserLoginRequest.email, extensionApiUserLoginRequest.password
                            )
                            temp.hasElement().flatMap { tempIsNotEmpty ->
                                if (!(tempIsNotEmpty)) {
                                    response["message"] = "账号或密码错误"
                                    Mono.just(response)
                                } else {
                                    getUser(temp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUser(temp: Mono<User>): Mono<Any> {
        val response = linkedMapOf<String, Any>()
        return temp.flatMap { user ->
            val newToken = UUID.randomUUID().toString().replace("-", "")
            userRepository.set(user._id, newToken, Duration.ofDays(30)).flatMap { isOk ->
                if (!(isOk)) {
                    response["message"] = "缓存服务失败，请联系管理员。"
                    Mono.just(response)
                } else {
                    user.password = newToken
                    Mono.just(user)
                }
            }
        }
    }
}