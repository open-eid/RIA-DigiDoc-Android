// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.notifications

sealed class ContainerNotificationType {
    data object XadesFile : ContainerNotificationType()

    data object CadesFile : ContainerNotificationType()

    data object DdocFile : ContainerNotificationType()

    data object EmptyFile : ContainerNotificationType()

    data class UnknownSignatures(
        val count: Int,
    ) : ContainerNotificationType()

    data class InvalidSignatures(
        val count: Int,
    ) : ContainerNotificationType()

    data class UnknownTimestamps(
        val count: Int,
    ) : ContainerNotificationType()

    data class InvalidTimestamps(
        val count: Int,
    ) : ContainerNotificationType()
}
