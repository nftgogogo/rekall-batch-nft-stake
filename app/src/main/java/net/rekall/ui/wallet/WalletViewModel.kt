package net.rekall.ui.wallet

import android.util.Log
import net.rekall.R
import net.rekall.utils.ToastHelper
import net.rekall.utils.wallet.ETHWalletUtils
import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.rekall.base.BaseViewModel
import net.rekall.constant.PRC_URL_KEY
import net.rekall.database.AccountState
import net.rekall.database.WalletDBUtils
import net.rekall.database.nft.NFTItemDBUtils
import net.rekall.repository.RKRepository
import net.rekall.utils.MMKVUtils
import java.math.BigInteger

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

