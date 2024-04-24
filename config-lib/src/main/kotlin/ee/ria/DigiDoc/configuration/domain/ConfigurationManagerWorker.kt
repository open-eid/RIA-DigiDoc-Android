@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import ee.ria.DigiDoc.configuration.ConfigurationManager
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationViewModel
import ee.ria.DigiDoc.configuration.utils.Constant.FORCE_LOAD_CENTRAL_CONFIGURATION
import ee.ria.DigiDoc.configuration.utils.Constant.LAST_CONFIGURATION_UPDATE
import java.util.Date

class ConfigurationManagerWorker(
    private val configurationManager: ConfigurationManager,
    private val configurationViewModel: ConfigurationViewModel,
    context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {
    private fun getConfiguration(): ConfigurationProvider {
        return if (inputData.getBoolean(FORCE_LOAD_CENTRAL_CONFIGURATION, false)) {
            configurationManager.forceLoadCentralConfiguration()
        } else {
            configurationManager.configuration
        }
    }

    companion object {
        fun enqueueWorker(context: Context) {
            val data =
                Data.Builder()
                    .putBoolean(FORCE_LOAD_CENTRAL_CONFIGURATION, false)
                    .putLong(LAST_CONFIGURATION_UPDATE, 0)
                    .build()

            val request =
                OneTimeWorkRequest.Builder(ConfigurationManagerWorker::class.java)
                    .setInputData(data)
                    .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }

    override fun doWork(): Result {
        val configurationProvider = getConfiguration()
        val lastConfigurationUpdateEpoch =
            inputData.getLong(LAST_CONFIGURATION_UPDATE, 0)
        val confUpdateDate = configurationProvider.configurationUpdateDate
        if (lastConfigurationUpdateEpoch == 0L || confUpdateDate != null &&
            confUpdateDate.after(
                Date(lastConfigurationUpdateEpoch),
            )
        ) {
            configurationViewModel.workerResult.postValue(configurationProvider)
        }
        return Result.success()
    }
}
