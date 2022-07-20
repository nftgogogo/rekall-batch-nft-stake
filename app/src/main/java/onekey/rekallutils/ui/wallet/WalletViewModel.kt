package onekey.rekallutils.ui.wallet

import onekey.rekallutils.R
import onekey.rekallutils.utils.ToastHelper
import onekey.rekallutils.utils.wallet.ETHWalletUtils
import androidx.lifecycle.*
import kotlinx.coroutines.*
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.constant.PRC_URL_KEY
import onekey.rekallutils.database.AccountState
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.database.nft.NFTItemDBUtils
import onekey.rekallutils.repository.RKRepository
import onekey.rekallutils.utils.MMKVUtils

class WalletViewModel() : BaseViewModel() {

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

    fun resetPRCURL(prc_url: String){
        MMKVUtils.save(PRC_URL_KEY,prc_url)
        RKRepository.get().reBuildHttpService()
    }

    fun removeAll() {
        loading.value = true
        viewModelScope.launch {
            val res = async(Dispatchers.IO) {
                try {
                    WalletDBUtils.getAll()?.forEach {
                        ETHWalletUtils.deleteWallet(it.id)
                        NFTItemDBUtils.deleteUser(it.address)
                        MMKVUtils.get().remove(PRC_URL_KEY)
                    }
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
                entity.value = WalletDBUtils.currentAccount()
                loading.value = false
            }
        }
    }
}

