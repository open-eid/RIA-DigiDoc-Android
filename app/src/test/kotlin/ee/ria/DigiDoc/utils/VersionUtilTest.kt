// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionUtilTest {
    @Test
    fun versionUtil_isLowerThan_returnTrueWhenBuildNumberIsLower() {
        assertTrue(VersionUtil.isLowerThan("3.2.0.100", "3.2.0.101"))
    }

    @Test
    fun versionUtil_isLowerThan_returnTrueWhenMinorIsLowerDespiteHigherBuildNumber() {
        assertTrue(VersionUtil.isLowerThan("3.1.9.999", "3.2.0.1"))
    }

    @Test
    fun versionUtil_isLowerThan_comparesSegmentsNumericallyNotLexically() {
        assertTrue(VersionUtil.isLowerThan("3.9.0.1", "3.10.0.1"))
        assertFalse(VersionUtil.isLowerThan("3.10.0.1", "3.9.0.1"))
    }

    @Test
    fun versionUtil_isLowerThan_returnFalseWhenEqual() {
        assertFalse(VersionUtil.isLowerThan("3.2.0.100", "3.2.0.100"))
    }

    @Test
    fun versionUtil_isLowerThan_returnFalseWhenHigher() {
        assertFalse(VersionUtil.isLowerThan("3.2.1.1", "3.2.0.500"))
    }

    @Test
    fun versionUtil_isLowerThan_treatsMissingSegmentsAsZero() {
        assertTrue(VersionUtil.isLowerThan("3.2", "3.2.0.1"))
        assertFalse(VersionUtil.isLowerThan("3.2.0.0", "3.2"))
        assertFalse(VersionUtil.isLowerThan("3.2.0.1", "3.2"))
    }

    @Test
    fun versionUtil_isLowerThan_returnFalseWhenOtherVersionIsNull() {
        assertFalse(VersionUtil.isLowerThan("3.2.0.100", null))
    }

    @Test
    fun versionUtil_isLowerThan_returnFalseWhenOtherVersionIsBlank() {
        assertFalse(VersionUtil.isLowerThan("3.2.0.100", " "))
    }

    @Test
    fun versionUtil_isLowerThan_returnFalseWhenOtherVersionIsNotNumeric() {
        assertFalse(VersionUtil.isLowerThan("3.2.0.100", "latest"))
    }

    @Test
    fun versionUtil_isLowerThan_ignoresTrailingNonNumericSuffix() {
        assertTrue(VersionUtil.isLowerThan("3.2.0.100", "3.3.0-beta"))
        assertFalse(VersionUtil.isLowerThan("3.3.0.1", "3.3.0-beta"))
    }

    @Test
    fun versionUtil_isLowerThan_trimsWhitespace() {
        assertTrue(VersionUtil.isLowerThan("3.2.0.100", " 3.2.0.101 "))
    }
}
