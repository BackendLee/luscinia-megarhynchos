package com.lowlro.lusciniamegarhynchosapi.controller.advice

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class UserControllerAdvice {
    /**
     * "请求速率过快，请稍后再试。"
     */
    class UserRateException : RuntimeException()

    /**
     * "在程序尝试操作数据库 Redis 时发生了错误"
     */
    class UserRedisException : RuntimeException()

    /**
     * "该用户名与密码未能在数据库中找到对应的记录"
     */
    class UserMongoException : RuntimeException()

    /**
     * "在程序尝试操作数据库 MongoDB 时发生了错误,具体操作是: 尝试更新用户令牌"
     */
    class UserMongoTokenException : RuntimeException()

    /**
     * "非法请求，值 accessToken 为空。"
     */
    class RequestAccessTokenException : RuntimeException()

    /**
     * "访问令牌无效"
     */
    class UserAccessTokenException : RuntimeException()

    /**
     * "客户端令牌无效"
     */
    class UserClientTokenException : RuntimeException()

    /**
     * "访问令牌已经分配了一个配置文件。"
     */
    class UserAccessTokenHasProfileException : RuntimeException()

    /**
     * "访问令牌尝试绑定一个不属于自己的角色"
     */
    class UserAccessTokenBindNotHaveException : RuntimeException()

    /**
     * "204 NO_CONTENT"
     */
    class NOCONTENT : RuntimeException()

    /**
     * "ip 地址不一致,请检查你是否使用了代理。"
     */
    class UserIPException : RuntimeException()

    /**
     * "邮箱地址不能为空"
     */
    class EmailNullException : RuntimeException()

    /**
     * "邮箱地址不符合规则"
     */
    class EmailDenyRuleException : RuntimeException()

    /**
     * "此邮箱地址已被注册"
     */
    class EmailRegisteredException : RuntimeException()

    /**
     * "邮件发送失败"
     */
    class EmailSendException : RuntimeException()

    @ExceptionHandler(EmailSendException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun emailSendException(): LinkedHashMap<String, Any> {
        return resp("邮件发送失败")
    }

    @ExceptionHandler(EmailRegisteredException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun emailRegisteredException(): LinkedHashMap<String, Any> {
        return resp("此邮箱地址已被注册")
    }

    @ExceptionHandler(EmailDenyRuleException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun emailDenyRuleException(): LinkedHashMap<String, Any> {
        return resp("邮箱地址不符合规则")
    }

    @ExceptionHandler(EmailNullException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun emailNullException(): LinkedHashMap<String, Any> {
        return resp("邮箱地址不能为空")
    }

    @ExceptionHandler(UserIPException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userIPException(): LinkedHashMap<String, Any> {
        return resp("ip 地址不一致,请检查你是否使用了代理。")
    }

    @ExceptionHandler(NOCONTENT::class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun nOCONTENT(): LinkedHashMap<String, Any> {
        return respNOCONTENT()
    }

    @ExceptionHandler(UserAccessTokenBindNotHaveException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userAccessTokenBindNotHaveException(): LinkedHashMap<String, Any> {
        return resp("访问令牌尝试绑定一个不属于自己的角色。")
    }

    @ExceptionHandler(UserAccessTokenHasProfileException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun userAccessTokenHasProfileException(): LinkedHashMap<String, Any> {
        return respBADREQUEST("访问令牌已经分配了一个配置文件。")
    }

    @ExceptionHandler(UserClientTokenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userClientTokenException(): LinkedHashMap<String, Any> {
        return resp("客户端令牌无效")
    }

    @ExceptionHandler(UserAccessTokenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userAccessTokenException(): LinkedHashMap<String, Any> {
        return resp("访问令牌无效")
    }

    @ExceptionHandler(UserRateException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userRateException(): LinkedHashMap<String, Any> {
        return resp("请求速率过快，请稍后再试。")
    }

    @ExceptionHandler(UserRedisException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userRedisException(): LinkedHashMap<String, Any> {
        return resp("在程序尝试操作数据库 Redis 时发生了错误")
    }

    @ExceptionHandler(UserMongoException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userMongoException(): LinkedHashMap<String, Any> {
        return resp("该用户名与密码未能在数据库中找到对应的记录")
    }

    @ExceptionHandler(UserMongoTokenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userMongoTokenException(): LinkedHashMap<String, Any> {
        return resp("在程序尝试操作数据库 MongoDB 时发生了错误,具体操作是: 尝试更新用户令牌")
    }

    @ExceptionHandler(RequestAccessTokenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun requestAccessTokenException(): LinkedHashMap<String, Any> {
        return resp("非法请求，值 accessToken 为空。")
    }

    fun resp(errorMessage: String): LinkedHashMap<String, Any> {
        val data = linkedMapOf<String, Any>()
        data["error"] = "ForbiddenOperationException"
        data["errorMessage"] = errorMessage
        return data
    }

    fun respBADREQUEST(errorMessage: String): LinkedHashMap<String, Any> {
        val data = linkedMapOf<String, Any>()
        data["error"] = "IllegalArgumentException"
        data["errorMessage"] = errorMessage
        return data
    }

    fun respNOCONTENT(): LinkedHashMap<String, Any> {
        return linkedMapOf<String, Any>()
    }
}