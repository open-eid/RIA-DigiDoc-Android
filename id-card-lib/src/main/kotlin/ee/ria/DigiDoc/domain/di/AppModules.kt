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

package ee.ria.DigiDoc.domain.di

import android.app.Application
import android.content.Context
import android.hardware.usb.UsbManager
import com.google.common.collect.ImmutableList
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.usb.AcsUsbSmartCardReader
import ee.ria.DigiDoc.smartcardreader.usb.IdentivUsbSmartCardReader
import ee.ria.DigiDoc.smartcardreader.usb.UsbSmartCardReader
import ee.ria.DigiDoc.smartcardreader.usb.UsbSmartCardReaderManager
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    @Singleton
    fun provideUsbSmartCardReaderManager(
        @ApplicationContext context: Context,
        usbManager: UsbManager,
        readers: ImmutableList<UsbSmartCardReader>,
    ): SmartCardReaderManager = UsbSmartCardReaderManager(context, usbManager, readers)

    @Provides
    @Singleton
    fun provideUsbManager(
        @ApplicationContext context: Context,
    ): UsbManager = (context.getSystemService(Context.USB_SERVICE) as UsbManager)

    @Provides
    fun provideUsbSmartCardReaderList(
        acsSmartCardReader: AcsUsbSmartCardReader,
        identivSmartCardReader: IdentivUsbSmartCardReader,
    ): ImmutableList<UsbSmartCardReader> = ImmutableList.of(acsSmartCardReader, identivSmartCardReader)

    @Provides
    @Singleton
    fun provideAcsUsbSmartCardReader(usbManager: UsbManager): AcsUsbSmartCardReader = AcsUsbSmartCardReader(usbManager)

    @Provides
    @Singleton
    fun provideIdentivUsbSmartCardReader(
        application: Application,
        usbManager: UsbManager,
    ): IdentivUsbSmartCardReader = IdentivUsbSmartCardReader(application, usbManager)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SmartCardReaderModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AcsReader

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class IdentivReader

    @Binds
    @Singleton
    @AcsReader
    abstract fun bindAcsUsbSmartCardReader(acsSmartCardReader: AcsUsbSmartCardReader): UsbSmartCardReader

    @Binds
    @Singleton
    @IdentivReader
    abstract fun bindIdentivUsbSmartCardReader(identivSmartCardReader: IdentivUsbSmartCardReader): UsbSmartCardReader
}
