// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.snackbar

enum class SnackbarType { SUCCESS, ERROR }

data class SnackBarMessage(
    val text: String,
    val type: SnackbarType = SnackbarType.ERROR,
)
