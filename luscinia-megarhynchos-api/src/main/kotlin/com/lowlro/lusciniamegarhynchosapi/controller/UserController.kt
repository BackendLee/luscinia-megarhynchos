package com.lowlro.lusciniamegarhynchosapi.controller

import com.lowlro.lusciniamegarhynchosapi.service.UserService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.io.Serializable

@RestController
@RequestMapping("/yggdrasil")
class UserController(
    val userService: UserService
) {
    @GetMapping("/")
    fun index(): Mono<LinkedHashMap<String, Any>> {
        return userService.index()
    }

    @PostMapping("/authserver/authenticate")
    fun authenticate(@RequestBody authenticateRequest: AuthenticateRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.authenticate(authenticateRequest)
    }

    @PostMapping("/authserver/refresh")
    fun refresh(@RequestBody refreshRequest: RefreshRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.refresh(refreshRequest)
    }

    @PostMapping("/authserver/validate")
    fun validate(@RequestBody validateRequest: ValidateRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.validate(validateRequest)
    }

    @PostMapping("/authserver/invalidate")
    fun invalidate(@RequestBody invalidateRequest: InvalidateRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.invalidate(invalidateRequest)
    }

    @PostMapping("/authserver/signout")
    fun signout(@RequestBody signoutRequest: SignoutRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.signout(signoutRequest)
    }

    @PostMapping("/sessionserver/session/minecraft/join")
    fun join(@RequestBody joinRequest: JoinRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.join(joinRequest)
    }

    @GetMapping("/sessionserver/session/minecraft/hasJoined")
    fun hasJoined(
        @RequestParam("username") username: String,
        @RequestParam("serverId") serverId: String,
        @RequestParam(value = "ip", name = "ip", required = false, defaultValue = "0.0.0.0") ip: String
    ): Mono<LinkedHashMap<String, Any>> {
        return userService.hasJoined(username, serverId, ip)
    }

    @GetMapping("/sessionserver/session/minecraft/profile/{uuid}")
    fun minecraftProfile(
        @PathVariable(value = "uuid", name = "uuid", required = true) uuid: String,
        @RequestParam(value = "unsigned", name = "unsigned", required = false, defaultValue = "true") unsigned: String
    ): Mono<LinkedHashMap<String, Any>> {
        return userService.minecraftProfile(uuid, unsigned)
    }

    @PostMapping("/api/profiles/minecraft")
    fun minecraft(@RequestBody requestBody: ArrayList<String>): Mono<out ArrayList<out Serializable>> {
        return userService.minecraft(requestBody)
    }

    @PostMapping("/extension/api/user/register/captcha")
    fun extensionApiUserRegisterCaptcha(@RequestBody extensionApiUserRegisterCaptchaRequest: ExtensionApiUserRegisterCaptchaRequest): Mono<LinkedHashMap<String, Any>> {
        return userService.extensionApiUserRegisterCaptcha(extensionApiUserRegisterCaptchaRequest)
    }

    @PostMapping("/extension/api/user/register")
    fun extensionApiUserRegister(@RequestBody extensionApiUserRegisterRequest: ExtensionApiUserRegisterRequest): Mono<out Any> {
        return userService.extensionApiUserRegister(extensionApiUserRegisterRequest)
    }

    @PostMapping("/extension/api/user/login")
    fun extensionApiUserLogin(@RequestBody extensionApiUserLoginRequest: ExtensionApiUserLoginRequest): Mono<out Any> {
        return userService.extensionApiUserLogin(extensionApiUserLoginRequest)
    }

    class ExtensionApiUserLoginRequest {
        var email = ""
        var password = ""
    }

    class ExtensionApiUserRegisterCaptchaRequest {
        var email = ""
    }

    class ExtensionApiUserRegisterRequest {
        var email = ""
        var password = ""
        var captcha = ""
    }

    class AuthenticateRequest {

        var username = ""
        var password = ""
        var clientToken = ""
        var requestUser = false
        var agent = Agent()

        class Agent {

            var name = ""
            var version = ""

        }

    }

    class CreateCharacterRequest {
        var uid = ""
        var accessToken = ""
        var name = ""
    }

    class InvalidateRequest {

        var accessToken = ""
        var clientToken = ""

    }

    class JoinRequest {

        var accessToken = ""
        var selectedProfile = ""
        var serverId = ""

    }

    class RefreshRequest {

        var accessToken = ""
        var clientToken = ""
        var requestUser = false
        var selectedProfile = SelectedProfile()

        class SelectedProfile {

            var id = ""
            var name = ""

        }

    }

    class SignoutRequest {
        var username = ""
        var password = ""
    }

    class UpdateCharacter2Request {
        var uid = ""
        var accessToken = ""
        var texturesCape = ""
    }

    class UpdateCharacterRequest {
        var uid = ""
        var accessToken = ""
        var model = ""
        var texturesSkin = ""
    }

    class ValidateRequest {

        var accessToken = ""
        var clientToken = ""

    }
}