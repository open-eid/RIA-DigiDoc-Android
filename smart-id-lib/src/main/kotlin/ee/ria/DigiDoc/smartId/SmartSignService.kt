@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId

import android.content.Context
import androidx.lifecycle.LiveData
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.sid.dto.request.SmartCreateSignatureRequest
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SmartIDServiceResponse

interface SmartSignService {
    val response: LiveData<SmartIDServiceResponse?>
    val challenge: LiveData<String?>
    val status: LiveData<SessionStatusResponseProcessStatus?>
    val errorState: LiveData<String?>
    val cancelled: LiveData<Boolean?>
    val selectDevice: LiveData<Boolean?>

    fun setCancelled(cancelled: Boolean?)

    fun resetValues()

    suspend fun processSmartIdRequest(
        context: Context,
        request: SmartCreateSignatureRequest?,
        roleDataRequest: RoleData?,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
        certificateCertBundle: ArrayList<String>?,
        accessTokenPath: String?,
        accessTokenPass: String?,
    )
}
