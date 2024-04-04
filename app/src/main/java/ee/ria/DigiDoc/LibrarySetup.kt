@file:Suppress("PackageName")

package ee.ria.DigiDoc

import android.content.Context
import android.widget.Toast
import ee.ria.DigiDoc.init.Initialization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LibrarySetup {
    suspend fun setupLibraries(context: Context) {
        try {
            Initialization.init(context)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    R.string.libdigidocpp_initialization_failed,
                    Toast.LENGTH_LONG,
                )
                    .show()
            }
        }
    }
}
