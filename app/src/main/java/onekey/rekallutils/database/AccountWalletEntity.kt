package onekey.rekallutils.database

import org.litepal.crud.LitePalSupport

/**
 * <pre>

 * </pre>
 */
enum class AccountState(var state: Long) {
    LOGIN(1),
    LOGOUT(0),
}
data class AccountWalletEntity (

    var id: Long = -1,
    var address: String = "",
    var name: String = "",
    var password: String = "",
    var keystorePath: String = "",
    var mnemonicArrayJson: MutableList<String> = mutableListOf(),
    var accountState: Long = AccountState.LOGOUT.state,
    var BNBBalance: Double = -1.0,
    var ekaBalance: Double = -1.0,

    ): LitePalSupport() {

}