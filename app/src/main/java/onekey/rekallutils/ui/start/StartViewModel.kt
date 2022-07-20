package net.rekall.ui.start

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.rekall.R
import net.rekall.base.BaseViewModel
import net.rekall.constant.PRC_URL_KEY
import net.rekall.database.AccountState
import net.rekall.database.WalletDBUtils
import net.rekall.repository.RKRepository
import net.rekall.utils.MMKVUtils
import net.rekall.utils.ToastHelper
import net.rekall.utils.wallet.ETHWalletUtils

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