// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import java.lang.reflect.Type
import java.util.UUID

class Cdoc2ConfDeserializer : JsonDeserializer<Map<String, ConfigurationProvider.CDOC2Conf>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): Map<String, ConfigurationProvider.CDOC2Conf> {
        val jsonObject = json.asJsonObject

        return jsonObject.entrySet().associate { (key, value) ->
            val conf =
                context.deserialize<ConfigurationProvider.CDOC2Conf>(
                    value,
                    ConfigurationProvider.CDOC2Conf::class.java,
                )
            key to conf.copy(uuid = UUID.fromString(key))
        }
    }
}
