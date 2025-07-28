@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.io.StringReader

class Parser(
    configuration: String?,
) {
    private val logTag = javaClass.simpleName
    private val configurationJson: JsonObject

    init {
        val gson = Gson()
        val reader =
            JsonReader(
                StringReader(configuration),
            )
        configurationJson = gson.fromJson(reader, JsonObject::class.java)
    }

    fun parseStringValue(vararg parameterNames: String): String = parseValue(*parameterNames) as String

    fun parseStringValues(vararg parameterNames: String): List<String> {
        val jsonValues: JsonArray = parseValue(*parameterNames) as JsonArray
        val values: MutableList<String> = ArrayList()
        for (i in 0 until jsonValues.size()) {
            values.add(jsonValues.get(i).asString)
        }
        return values
    }

    @Suppress("UNCHECKED_CAST")
    fun parseStringValuesToMap(vararg parameterNames: String): Map<String, String> =
        parseValue(*parameterNames) as Map<String, String>

    fun parseIntValue(vararg parameterNames: String): Int =
        try {
            (parseValue(*parameterNames) as String).toInt()
        } catch (nfe: NumberFormatException) {
            errorLog(logTag, "Unable to parse value", nfe)
            throw IllegalArgumentException("Unable to parse value")
        }

    private fun parseValue(vararg parameterNames: String): Any {
        var jsonObject: JsonObject = configurationJson
        for (i in 0 until parameterNames.size - 1) {
            jsonObject = jsonObject.getAsJsonObject(parameterNames[i])
        }
        val element: JsonElement =
            jsonObject.get(parameterNames[parameterNames.size - 1])
                ?: throw RuntimeException("Failed to parse parameter 'MISSING-VALUE' from configuration json")
        if (element is JsonArray) {
            return element.getAsJsonArray()
        } else if (element is JsonObject) {
            return Gson().fromJson<Map<*, *>>(element, MutableMap::class.java)
        }
        return element.asString
    }
}
