@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Date

class AddresseeTest {
    @Test
    fun addressee_lockLabel_defaultIsNull() {
        val addressee = addressee()
        assertNull(addressee.lockLabel)
    }

    @Test
    fun addressee_lockType_defaultIsNull() {
        val addressee = addressee()
        assertNull(addressee.lockType)
    }

    @Test
    fun addressee_lockLabel_storedCorrectly() {
        val raw = "data:,v=1&label=MyKey&type=pw"
        val addressee = addressee(lockLabel = raw)
        assertEquals(raw, addressee.lockLabel)
    }

    @Test
    fun addressee_lockType_storedCorrectly() {
        val addressee = addressee(lockType = "PASSWORD")
        assertEquals("PASSWORD", addressee.lockType)
    }

    @Test
    fun addressee_passwordRecipient_lockLabelAndTypeSet() {
        val addressee =
            Addressee(
                data = ByteArray(0),
                identifier = "MyKey",
                serialNumber = null,
                givenName = null,
                surname = null,
                certType = CertType.PasswordType,
                validTo = null,
                concatKDFAlgorithmURI = null,
                lockLabel = "data:,v=1&label=MyKey&type=pw",
                lockType = "PASSWORD",
            )
        assertEquals("data:,v=1&label=MyKey&type=pw", addressee.lockLabel)
        assertEquals("PASSWORD", addressee.lockType)
        assertEquals(CertType.PasswordType, addressee.certType)
        assertEquals("MyKey", addressee.identifier)
    }

    @Test
    fun addressee_otherFields_unaffectedByNewFields() {
        val addressee = addressee(lockLabel = "label", lockType = "TYPE")
        assertEquals("47101010033", addressee.identifier)
        assertEquals("Test", addressee.givenName)
        assertEquals("User", addressee.surname)
        assertEquals(CertType.IDCardType, addressee.certType)
        assertNull(addressee.validTo)
    }

    @Test
    fun addressee_isSerializable() {
        val mockAddressee =
            Addressee(
                data = byteArrayOf(1, 2, 3, 4),
                identifier = "47101010033",
                serialNumber = "10060701",
                givenName = "Test",
                surname = "User",
                certType = CertType.ESealType,
                validTo = Date(1_700_000_000_000L),
                concatKDFAlgorithmURI = "",
                lockLabel = "data:,v=1&label=MyKey&type=pw",
                lockType = "PASSWORD",
            )

        val bytes =
            ByteArrayOutputStream().use { buffer ->
                ObjectOutputStream(buffer).use { it.writeObject(mockAddressee) }
                buffer.toByteArray()
            }
        val getAddressee =
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Addressee }

        assertEquals(mockAddressee, getAddressee)
        assertArrayEquals(mockAddressee.data, getAddressee.data)
    }

    @Test
    fun addressee_lockIndex_defaultIsNull() {
        val addressee = addressee()
        assertNull(addressee.lockIndex)
    }

    @Test
    fun addressee_lockIndex_storedCorrectly() {
        val addressee = addressee(lockIndex = 2)
        assertEquals(2, addressee.lockIndex)
    }

    @Test
    fun addressee_lockIndex_survivesCopy() {
        val stamped = addressee().copy(keyLabel = "MyKey", lockIndex = 1)
        assertEquals(1, stamped.lockIndex)
        assertEquals("MyKey", stamped.keyLabel)
    }

    @Test
    fun addressee_lockIndex_survivesSerialization() {
        val original = addressee(lockLabel = "data:,v=1&label=MyKey&type=pw", lockIndex = 3)

        val bytes =
            ByteArrayOutputStream().use { byteStream ->
                ObjectOutputStream(byteStream).use { it.writeObject(original) }
                byteStream.toByteArray()
            }
        val restored =
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as Addressee }

        assertEquals(3, restored.lockIndex)
    }

    @Test
    fun addressee_fromLabelInfo_usesRawLabelAsCnWhenLabelIsNotMachineReadable() {
        val addressee =
            Addressee.fromLabelInfo(
                info = emptyMap(),
                rawLabel = "TESTSURNAME,TESTGIVENNAME,47101010033",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("TESTSURNAME", addressee.surname)
        assertEquals("TESTGIVENNAME", addressee.givenName)
        assertEquals("47101010033", addressee.identifier)
    }

    @Test
    fun addressee_fromLabelInfo_usesWholeRawLabelAsIdentifierWhenItHasNoNameParts() {
        val addressee =
            Addressee.fromLabelInfo(
                info = emptyMap(),
                rawLabel = "TESTORG",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("TESTORG", addressee.identifier)
        assertNull(addressee.surname)
        assertNull(addressee.givenName)
    }

    @Test
    fun addressee_fromLabelInfo_prefersCnOverRawLabel() {
        val addressee =
            Addressee.fromLabelInfo(
                info = mapOf("cn" to "TESTSURNAME,TESTGIVENNAME,47101010033"),
                rawLabel = "data:v=1&type=ID-card&cn=TESTSURNAME%2CTESTGIVENNAME%2C47101010033",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("47101010033", addressee.identifier)
    }

    @Test
    fun addressee_fromLabelInfo_fallsBackToLabelKeyWhenThereIsNoCn() {
        val addressee =
            Addressee.fromLabelInfo(
                info = mapOf("label" to "MyKey"),
                rawLabel = "data:,v=1&label=MyKey&type=pw",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("MyKey", addressee.identifier)
    }

    @Test
    fun addressee_fromLabelInfo_keepsTwoPartCnWholeInsteadOfThrowing() {
        val addressee =
            Addressee.fromLabelInfo(
                info = mapOf("cn" to "TESTSURNAME,TESTGIVENNAME"),
                rawLabel = "",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("TESTSURNAME,TESTGIVENNAME", addressee.identifier)
        assertNull(addressee.surname)
        assertNull(addressee.givenName)
    }

    @Test
    fun addressee_fromLabelInfo_mapsCertTypeFromLabelType() {
        val info = mapOf("cn" to "TESTSURNAME,TESTGIVENNAME,47101010033", "type" to "Digi-ID")
        val addressee = Addressee.fromLabelInfo(info, "", ByteArray(0), "")

        assertEquals(CertType.DigiIDType, addressee.certType)
    }

    @Test
    fun addressee_fromLabelInfo_certTypeIsUnknownWhenLabelDeclaresNoType() {
        val addressee =
            Addressee.fromLabelInfo(
                info = emptyMap(),
                rawLabel = "TESTORG",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals(CertType.UnknownType, addressee.certType)
    }

    @Test
    fun addressee_fromLabelInfo_readsServerExpirationAndSerialNumber() {
        val info =
            mapOf(
                "cn" to "TESTORG",
                "serial_number" to "12345678",
                "server_exp" to "2000000000",
            )
        val addressee = Addressee.fromLabelInfo(info, "", ByteArray(0), "")

        assertEquals("12345678", addressee.serialNumber)
        assertEquals(Date(2000000000L * 1000), addressee.validTo)
    }

    @Test
    fun addressee_fromLabelInfo_resolvesSymmetricKeyLabelInsteadOfShowingTheRawDataUri() {
        val addressee =
            Addressee.fromLabelInfo(
                info = mapOf("label" to "Shared key", "type" to "key"),
                rawLabel = "data:,v=1&type=key&label=Shared%20key",
                pub = ByteArray(0),
                concatKDFAlgorithmURI = "",
            )

        assertEquals("Shared key", addressee.identifier)
        assertEquals(CertType.UnknownType, addressee.certType)
    }

    @Test
    fun addressee_fromCN_keepsTwoPartCnWholeInsteadOfThrowing() {
        val addressee =
            Addressee.fromCN(
                cn = "TESTSURNAME,TESTGIVENNAME",
                sn = "",
                certType = CertType.UnknownType,
                validTo = null,
                data = ByteArray(0),
            )

        assertEquals("TESTSURNAME,TESTGIVENNAME", addressee.identifier)
        assertNull(addressee.surname)
        assertNull(addressee.givenName)
    }

    private fun addressee(
        lockLabel: String? = null,
        lockType: String? = null,
        lockIndex: Int? = null,
    ) = Addressee(
        data = ByteArray(0),
        identifier = "47101010033",
        serialNumber = null,
        givenName = "Test",
        surname = "User",
        certType = CertType.IDCardType,
        validTo = null,
        concatKDFAlgorithmURI = null,
        lockLabel = lockLabel,
        lockType = lockType,
        lockIndex = lockIndex,
    )
}
