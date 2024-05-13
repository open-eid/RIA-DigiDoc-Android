@file:Suppress("PackageName")

package ee.ria.DigiDoc.common

object Constant {
    const val RESTRICTED_FILENAME_CHARACTERS_AS_STRING = "@%:^?[]\\'\"”’{}#&`\\\\~«»/´"
    const val RTL_CHARACTERS_AS_STRING = "" + '\u200E' + '\u200F' + '\u202E' + '\u202A' + '\u202B'
    const val RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING =
        RESTRICTED_FILENAME_CHARACTERS_AS_STRING + RTL_CHARACTERS_AS_STRING
    const val DEFAULT_FILENAME = "newFile"
    const val ALLOWED_URL_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_,.:/%;+=@?&!()"

    const val DATA_FILE_DIR = "%s-data-files"
    const val DIR_SIGNATURE_CONTAINERS = "signed_containers"
    const val DIR_EXTERNALLY_OPENED_FILES = "external_files"
    const val DEFAULT_CONTAINER_EXTENSION = "asice"
    const val CONTAINER_MIME_TYPE = "application/octet-stream"
    const val DEFAULT_MIME_TYPE = "text/plain"
    val ASICS_CONTAINER_EXTENSIONS: Set<String> = setOf("asics", "scs")

    val CONTAINER_EXTENSIONS: Set<String> =
        setOf("asice", "sce", "adoc", "bdoc", "ddoc", "edoc")
            .plus(ASICS_CONTAINER_EXTENSIONS)

    val NON_LEGACY_CONTAINER_EXTENSIONS: Set<String> =
        setOf("asice", "sce", "bdoc")

    const val PDF_EXTENSION = "pdf"
}
