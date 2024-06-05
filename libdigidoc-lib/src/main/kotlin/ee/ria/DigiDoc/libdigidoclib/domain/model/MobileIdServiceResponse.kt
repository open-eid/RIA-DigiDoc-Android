@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.libdigidocpp.Container

data class MobileIdServiceResponse(
    var status: MobileCreateSignatureProcessStatus? = null,
    var container: Container? = null,
    var signature: String? = null,
)
