package onekey.rekallutils.ui.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.constant.PRC_URL_KEY
import onekey.rekallutils.database.AccountState
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.repository.RKRepository
import onekey.rekallutils.utils.MMKVUtils
import onekey.rekallutils.utils.ToastHelper
import onekey.rekallutils.utils.wallet.ETHWalletUtils

class StartViewModel : BaseViewModel() {

    val loading = MutableLiveData(false)

    fun privateKeyImport(key: String, pwd: String,prc_url:String) {
        if (key.isBlank()) {
            return
        }
        if (pwd.isBlank()) {
            return
        }
        loading.value = true
        viewModelScope.launch {
            val res = async(Dispatchers.IO) {

                try {
                    var res = ETHWalletUtils.loadWalletByPrivateKey(key, null, pwd)
                    res.accountState = AccountState.LOGIN.state
                    WalletDBUtils.saveAccountWallet(res)
                    MMKVUtils.save(PRC_URL_KEY,prc_url)
                    RKRepository.get().reBuildHttpService()
                    res = WalletDBUtils.currentAccount()
                    getBalance()
                    res
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        ToastHelper.showMessage(R.string.import_priavet_key_action)
                        loading.value = false
                    }
                    null
                }
            }
            withContext(Dispatchers.Main) {
                val await = res.await() ?: return@withContext
                entity.value = await
                loading.value = false
            }
        }
    }
}