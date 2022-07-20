package net.rekall.base

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.rekall.base.BaseConfig.Companion.EKAContractAddress
import net.rekall.base.BaseConfig.Companion.EKADecimal
import net.rekall.database.AccountWalletEntity
import net.rekall.database.WalletDBUtils
import net.rekall.repository.RKRepository
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger

open class BaseViewModel : ViewModel() {

    val entity = MutableLiveData<AccountWalletEntity?>(null)

    fun getCurrentAccount() {
        entity.value = WalletDBUtils.currentAccount()
        entity.value?.let {
            getBalance()
        }
    }


    fun getBalance() {
        viewModelScope.launch {
            entity.value?.let {
                val balance = withContext(Dispatchers.IO) {
                    RKRepository.get().getBalance(it.address)
                }
                val ekaBalance = withContext(Dispatchers.IO) {
                    RKRepository.get().getERCBalance(it.address, EKAContractAddress, EKADecimal)
                }
                WalletDBUtils.setAccountBalance(it.address,balance,ekaBalance)
                entity.value = WalletDBUtils.currentAccount()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            viewModelScope.cancel()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}