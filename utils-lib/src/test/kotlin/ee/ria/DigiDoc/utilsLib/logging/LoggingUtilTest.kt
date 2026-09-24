// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.logging

import android.content.Context
import ee.ria.DigiDoc.common.Constant.LIBDIGIDOCPP_LOG_FILE_NAME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.util.logging.Logger

@RunWith(MockitoJUnitRunner::class)
class LoggingUtilTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

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

    @Test
    fun loggingUtil_initialize_keepsExistingLogsWhenCalledAgain() {
        val filesDirectory = temporaryFolder.newFolder()
        val context = mock(Context::class.java)
        `when`(context.filesDir).thenReturn(filesDirectory)

        LoggingUtil.initialize(context, Logger.getLogger("loggingUtilTest"), true)

        val libdigidocppLog = File(File(filesDirectory, "logs"), LIBDIGIDOCPP_LOG_FILE_NAME)
        libdigidocppLog.writeText("libdigidocpp entry")

        LoggingUtil.initialize(context, Logger.getLogger("loggingUtilTest"), true)

        assertTrue(libdigidocppLog.exists())
    }

    @Test
    fun loggingUtil_resetLogs_deletesExistingLogs() {
        val logsDirectory = temporaryFolder.newFolder("logs")
        val appLog = File(logsDirectory, "01.01.2026.txt")
        appLog.writeText("app entry")

        LoggingUtil.resetLogs(logsDirectory)

        assertFalse(appLog.exists())
    }

    @Test
    fun loggingUtil_resetLogs_emptiesLibdigidocppLogInsteadOfDeletingIt() {
        val logsDirectory = temporaryFolder.newFolder("logs")
        val libdigidocppLog = File(logsDirectory, LIBDIGIDOCPP_LOG_FILE_NAME)
        libdigidocppLog.writeText("libdigidocpp entry")

        LoggingUtil.resetLogs(logsDirectory)

        assertTrue(libdigidocppLog.exists())
        assertEquals(0L, libdigidocppLog.length())
    }
}
