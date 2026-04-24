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

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.info

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ee.ria.DigiDoc.R

data class InfoComponentItem(
    @param:StringRes val name: Int = 0,
    @param:StringRes val licenseName: Int = 0,
    @param:StringRes val licenseUrl: Int = 0,
) {
    @Composable
    fun componentItems(): List<InfoComponentItem> =
        listOf(
            InfoComponentItem(
                name = R.string.main_about_libdigidocpp_title,
                licenseName = R.string.main_about_lgpl_2_1_license_title,
                licenseUrl = R.string.main_about_lgpl_2_1_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_support_preference_v7_fix_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_guava_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_square_okhttp_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_retrofit_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_bouncy_castle_title,
                licenseName = R.string.main_about_mit_license_title,
                licenseUrl = R.string.main_about_mit_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_dagger_title,
                licenseName = R.string.main_about_apache_2_license_title,
                licenseUrl = R.string.main_about_apache_2_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_junit_title,
                licenseName = R.string.main_about_eclipse_license_title,
                licenseUrl = R.string.main_about_eclipse_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_mockito_title,
                licenseName = R.string.main_about_mit_license_title,
                licenseUrl = R.string.main_about_mit_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_jackson_databind_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_jackson_databind_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_commons_io_title,
                licenseName = R.string.main_about_mit_license_title,
                licenseUrl = R.string.main_about_commons_io_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_commons_text_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_commons_io_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_telecom_charsets_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_telecom_charsets_license_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_core_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_junit_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_espresso_core_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_material_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_lifecycle_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_compose_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_ui_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_hilt_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_appcompat_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_test_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_arch_core_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_navigation_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_constraintlayout_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_preferencex_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_androidx_security_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_pdfbox_android_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_kotlinx_coroutines_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_android_material_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
            InfoComponentItem(
                name = R.string.main_about_gson_title,
                licenseName = R.string.main_about_apache_2_0_license_title,
                licenseUrl = R.string.main_about_apache_2_license_txt_url,
            ),
        )
}
