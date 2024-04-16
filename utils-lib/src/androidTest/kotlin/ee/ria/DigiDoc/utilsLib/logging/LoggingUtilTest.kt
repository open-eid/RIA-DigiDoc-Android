@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.logging

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoggingUtilTest {
    @Mock
    private lateinit var mockLogger: Logging

    @Test
    fun loggingUtil_errorLog_logsErrorMessageWithThrowable() {
        val tag = "TestTag"
        val message = "Test error message"
        val throwable = NullPointerException("Test Exception")

        mockLogger.errorLog(tag, message, throwable)

        verify(mockLogger, times(1)).errorLog(tag, message, throwable)
    }

    @Test
    fun loggingUtil_debugLog_logsDebugMessageWithThrowable() {
        val tag = "TestTag"
        val message = "Test debug message"
        val throwable = NullPointerException("Test Exception")

        mockLogger.debugLog(tag, message, throwable)

        verify(mockLogger, times(1)).debugLog(tag, message, throwable)
    }

    @Test
    fun loggingUtil_errorLog_logsErrorMessageWithoutThrowable() {
        val tag = "TestTag"
        val message = "Test error message"

        mockLogger.errorLog(tag, message)

        verify(mockLogger, times(1)).errorLog(tag, message, null)
    }

    @Test
    fun loggingUtil_debugLog_logsDebugMessageWithoutThrowable() {
        val tag = "TestTag"
        val message = "Test debug message"

        mockLogger.debugLog(tag, message)

        verify(mockLogger, times(1)).debugLog(tag, message, null)
    }
}
