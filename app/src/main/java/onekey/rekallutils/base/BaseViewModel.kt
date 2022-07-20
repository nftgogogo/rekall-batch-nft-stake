package onekey.rekallutils.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onekey.rekallutils.base.BaseConfig.Companion.EKAContractAddress
import onekey.rekallutils.base.BaseConfig.Companion.EKADecimal
import onekey.rekallutils.database.AccountWalletEntity
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.repository.RKRepository

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