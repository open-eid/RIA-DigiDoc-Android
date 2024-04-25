@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

interface SignatureInterface {
    val claimedSigningTime: String
    val trustedSigningTime: String
    val signatureMethod: String
    val dataToSign: ByteArray?
    val policy: String
    val spUri: String
    val profile: String
    val city: String
    val stateOrProvince: String
    val postalCode: String
    val countryName: String
    val signerRoles: List<String>
    val ocspProducedAt: String
    val timeStampTime: String
    val archiveTimeStampTime: String
    val streetAddress: String
    val signedBy: String
    val messageImprint: ByteArray
    val signingCertificateDer: ByteArray
    val ocspCertificateDer: ByteArray
    val timeStampCertificateDer: ByteArray
    val archiveTimeStampCertificateDer: ByteArray

    val validator: ValidatorInterface
}
