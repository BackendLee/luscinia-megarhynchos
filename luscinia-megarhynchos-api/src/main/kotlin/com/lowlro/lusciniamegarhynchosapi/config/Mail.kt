package com.lowlro.lusciniamegarhynchosapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "mail")
data class Mail(
    val host: String = "localhost",
    val port: Int = 465,
    val username: String = "",
    val password: String = "",
    val protocol: String = "smtps",
    val defaultEncoding: String = "utf-8",
    val from: String = "luscinia-megarhynchos 客服",
    val subject: String = "您的 luscinia-megarhynchos 帐户：来自未授权的访问",
    val text: String = "验证码：%s"
)