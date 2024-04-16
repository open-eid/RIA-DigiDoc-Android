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
}
