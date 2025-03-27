@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

enum class CertType {
    UnknownType,
    IDCardType,
    DigiIDType,
    EResidentType,
    MobileIDType,
    SmartIDType,
    ESealType,
}

object OID {
    const val ID_CARD_POLICY_PREFIX = "1.3.6.1.4.1.10015.1.1"
    const val ALTERNATE_ID_CARD_POLICY = "1.3.6.1.4.1.51361.1.1.1"

    const val DIGI_ID_POLICY_PREFIX = "1.3.6.1.4.1.10015.1.2"
    const val ALTERNATE_DIGI_ID_POLICY1 = "1.3.6.1.4.1.51361.1.1"
    const val ALTERNATE_DIGI_ID_POLICY2 = "1.3.6.1.4.1.51455.1.1"

    const val MOBILE_ID_POLICY_PREFIX = "1.3.6.1.4.1.10015.1.3"
    const val ALTERNATE_MOBILE_ID_POLICY = "1.3.6.1.4.1.10015.11.1"

    const val ESEAL_POLICY_PREFIX1 = "1.3.6.1.4.1.10015.7.3"
    const val ESEAL_POLICY_PREFIX2 = "1.3.6.1.4.1.10015.7.1"
    const val ESEAL_POLICY_PREFIX3 = "1.3.6.1.4.1.10015.2.1"
}
