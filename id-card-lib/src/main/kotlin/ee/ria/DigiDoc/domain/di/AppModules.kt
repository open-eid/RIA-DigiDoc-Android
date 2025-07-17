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
