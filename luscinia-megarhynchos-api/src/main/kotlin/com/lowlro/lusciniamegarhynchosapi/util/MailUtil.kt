package com.lowlro.lusciniamegarhynchosapi.util

import com.lowlro.lusciniamegarhynchosapi.config.Mail
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Component

@Component
class MailUtil(
    val mail: Mail
) {
    fun send(setTo: String, captcha: String): Boolean {
        val javaMailSender = JavaMailSenderImpl()
        javaMailSender.host = mail.host
        javaMailSender.port = mail.port
        javaMailSender.username = mail.username
        javaMailSender.password = mail.password
        javaMailSender.protocol = mail.protocol
        javaMailSender.defaultEncoding = mail.defaultEncoding
        javaMailSender.javaMailProperties.setProperty("mail.smtp.ssl.enable", "true")
        javaMailSender.javaMailProperties.setProperty("mail.smtp.auth", "true")
        javaMailSender.javaMailProperties.setProperty("mail.smtp.starttls.enable", "true")
        javaMailSender.javaMailProperties.setProperty("mail.smtp.starttls.required", "true")
        val simpleMailMessage = SimpleMailMessage()
        simpleMailMessage.from = "${mail.from}<${mail.username}>"
        simpleMailMessage.setTo(setTo)
        simpleMailMessage.subject = mail.subject
        simpleMailMessage.text = mail.text.replace("%s", captcha)
        try {
            javaMailSender.send(simpleMailMessage)
        } catch (exception: Exception) {
            return false
        }
        return true
    }
}