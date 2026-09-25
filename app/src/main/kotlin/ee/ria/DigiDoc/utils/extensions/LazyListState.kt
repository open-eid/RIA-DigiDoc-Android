// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.extensions

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.reachedBottom(): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem != null &&
        lastVisibleItem.index != 0 &&
        this.layoutInfo.totalItemsCount > 5 &&
        lastVisibleItem.index == this.layoutInfo.totalItemsCount - 1
}
