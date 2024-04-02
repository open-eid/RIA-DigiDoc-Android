sealed class Screens(val route : String) {
    object Signature : Screens("signature_route")
    object Crypto : Screens("crypto_route")
    object eID : Screens("eid_route")
}