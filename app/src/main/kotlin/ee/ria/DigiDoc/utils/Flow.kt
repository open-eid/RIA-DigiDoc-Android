@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <T> Flow<List<T>>.flattenToList() = flatMapConcat { it.asFlow() }.toList()
