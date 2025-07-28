@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.notifications

sealed class ContainerNotificationType {
    data object XadesFile : ContainerNotificationType()

    data object CadesFile : ContainerNotificationType()

    data class UnknownSignatures(
        val count: Int,
    ) : ContainerNotificationType()

    data class InvalidSignatures(
        val count: Int,
    ) : ContainerNotificationType()
}
