package com.lowlro.lusciniamegarhynchosapi.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class User {
    @Id
    var _id = ""
    var mail = ""
    var password = ""
    var characters = arrayListOf(Character())
    var tokens = arrayListOf(Token())
    var properties = arrayListOf(Propertie())

    class Character {
        var uuid = ""
        var name = ""
        var model = ""
        var texture = Texture()

        class Texture {
            var skin = ""
            var cape = ""
        }
    }

    class Token {
        var access = ""
        var boundCharacter = BoundCharacter()
        var client = ""
        var createAt = ""

        class BoundCharacter {
            var uuid = ""
            var name = ""
        }
    }

    class Propertie {
        var name = ""
        var value = ""
    }
}