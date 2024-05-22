@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId

import androidx.lifecycle.LiveData
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.mid.dto.response.MobileIdServiceResponse
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting

interface MobileSignService {
    val response: LiveData<MobileIdServiceResponse?>
    val challenge: LiveData<String?>
    val status: LiveData<MobileCreateSignatureProcessStatus?>
    val errorState: LiveData<String?>
    val cancelled: LiveData<Boolean?>

    fun setCancelled(cancelled: Boolean?)

    suspend fun processMobileIdRequest(
        request: MobileCreateSignatureRequest?,
        roleDataRequest: RoleData?,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
        certificateCertBundle: ArrayList<String>?,
        accessTokenPath: String?,
        accessTokenPass: String?,
    )
}
