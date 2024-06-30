package com.teamsparta.jobtopia.domain.post.controller
import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): LocalDateTime {
        return LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}