package com.Placements.Ready.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class StringOrListDeserializer : JsonDeserializer<List<String>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<String> {
        return if (json.isJsonArray) {
            val list = mutableListOf<String>()
            json.asJsonArray.forEach { list.add(it.asString) }
            list
        } else if (json.isJsonPrimitive && json.asJsonPrimitive.isString) {
            listOf(json.asString)
        } else {
            emptyList()
        }
    }
}
