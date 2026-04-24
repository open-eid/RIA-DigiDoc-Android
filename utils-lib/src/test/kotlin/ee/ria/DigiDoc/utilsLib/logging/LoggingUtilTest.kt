/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
