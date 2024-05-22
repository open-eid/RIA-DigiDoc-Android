@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import ee.ria.libdigidocpp.Container

class MobileIdServiceResponse {
    var status: MobileCreateSignatureProcessStatus? = null
    var container: Container? = null
    var signature: String? = null
}
