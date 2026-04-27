/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
