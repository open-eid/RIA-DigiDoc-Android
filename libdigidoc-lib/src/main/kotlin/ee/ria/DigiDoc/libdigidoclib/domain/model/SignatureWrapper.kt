@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.Signature

class SignatureWrapper(signature: Signature) : SignatureInterface {
    override val id: String = signature.id()
    override val claimedSigningTime: String = signature.claimedSigningTime()
    override val trustedSigningTime: String = signature.trustedSigningTime()
    override val signatureMethod: String = signature.signatureMethod()
    override val dataToSign: ByteArray? =
        try {
            signature.dataToSign()
        } catch (e: Exception) {
            null
        }
    override val policy: String = signature.policy()
    override val spUri: String = signature.SPUri()
    override val profile: String = signature.profile()
    override val city: String = signature.city()
    override val stateOrProvince: String = signature.stateOrProvince()
    override val postalCode: String = signature.postalCode()
    override val countryName: String = signature.countryName()
    override val signerRoles: List<String> = signature.signerRoles()
    override val ocspProducedAt: String = signature.OCSPProducedAt()
    override val timeStampTime: String = signature.TimeStampTime()
    override val archiveTimeStampTime: String = signature.ArchiveTimeStampTime()
    override val streetAddress: String = signature.streetAddress()
    override val signedBy: String = signature.signedBy()
    override val messageImprint: ByteArray = signature.messageImprint()
    override val signingCertificateDer: ByteArray = signature.signingCertificateDer()
    override val ocspCertificateDer: ByteArray = signature.OCSPCertificateDer()
    override val timeStampCertificateDer: ByteArray = signature.TimeStampCertificateDer()
    override val archiveTimeStampCertificateDer: ByteArray = signature.ArchiveTimeStampCertificateDer()

    override val validator: ValidatorInterface = ValidatorWrapper(Signature.Validator(signature))
}
