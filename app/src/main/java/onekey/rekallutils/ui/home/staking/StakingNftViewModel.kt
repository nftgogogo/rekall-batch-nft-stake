package onekey.rekallutils.ui.home.staking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.database.nft.NFTItemDBUtils
import onekey.rekallutils.database.nft.StakingState
import onekey.rekallutils.database.nft.UserNFTItem
import onekey.rekallutils.repository.RKRepository
import onekey.rekallutils.ui.home.HomeModel
import onekey.rekallutils.utils.ResHelper
import onekey.rekallutils.utils.ToastHelper
import java.math.BigDecimal
import java.math.BigInteger

class StakingNftViewModel : BaseViewModel() {

    val model = HomeModel()
    val isRefreshing = MutableLiveData(false)
    val  lists: MutableLiveData<MutableList<UserNFTItem>> = MutableLiveData()

    fun getStakeListWithUser(){

        getCurrentAccount()
        if(entity.value != null){
            isRefreshing.value = true
            viewModelScope.launch {
                try {
                    val res = async(Dispatchers.IO) {
                        getData()
                        try {
                            model.getStakeListWithUser(entity.value!!.address)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mutableListOf()
                        }
                    }
                    val await = res.await()
                    val list = mutableListOf<UserNFTItem>()
                    if (await.isNotEmpty()) {
                        val temp = await.mapNotNull {
                            UserNFTItem(
                                entity.value!!.address,
                                it.nftAddress,
                                it.tokenId,
                                StakingState.STAKING
                            )
                        }
                        list.addAll(NFTItemDBUtils.saveUserNFTAll(temp))
                    }else{
                        lists.value?.forEach { it.status = StakingState.UNKNOWN.state
                            it.save()
                        }
                    }
                    tokenURI(list)
                    refreshProfit(list)
                    getNftPower(list)
                    lists.postValue(list)
                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                    ToastHelper.showMessage(R.string.refresh_error)
                    isRefreshing.postValue(false)
                }
            }
        }else{
            isRefreshing.postValue(false)
        }
    }

    private fun getData() {
        lists.postValue(
            NFTItemDBUtils.getNFTListForStatus(
                entity.value!!.address,
                StakingState.STAKING
            ) ?: mutableListOf()
        )
    }

    fun  tokenURI(list :MutableList<UserNFTItem>){
        list.forEachIndexed {
            index,item->
            if (item.image.isBlank()){
                tokenURI(index, item)
            }
        }
    }

    fun getBalanceChange(selectList: MutableList<UserNFTItem>, fuc: () -> Unit) {
        viewModelScope.launch {
            entity.value?.let {
                val balance = withContext(Dispatchers.IO) {
                    RKRepository.get().getBalance(it.address)
                }
                WalletDBUtils.setAccountBNBBalance(it.address, balance)
                val fee =
                    RKRepository.get().estimateGasFee() * BigDecimal(selectList.size)
                if (fee > balance) {
                    withContext(Dispatchers.Main) {
                        ToastHelper.showMessage(
                            ResHelper.getString(
                                R.string.estimate_gas_fee_not_enough,
                                fee.toString()
                            )
                        )
                    }
                } else {
                    fuc.invoke()
                }
            }

        }
    }

    suspend  fun  refreshProfit(list :MutableList<UserNFTItem>){
        viewModelScope.launch {
            try {
                list.forEachIndexed {
                        index,item->
                        try {
                            val profit = model.getNftProfit(item.ownerAddress,item.nftAddress, item.tokenId)
                            NFTItemDBUtils.saveProfit(item,profit)
                            item.profit = profit.toDouble()
                            lists.value?.set(index,item)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                }
                withContext(Dispatchers.IO){
                    getData()
                }
                isRefreshing.postValue(false)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    suspend  fun  getNftPower(list :MutableList<UserNFTItem>){
        viewModelScope.launch {
            try {
                list.forEachIndexed {
                        index,item->
                    try {
                        val power = model.getNftPower(item.ownerAddress,item.nftAddress, item.tokenId)
                        NFTItemDBUtils.savePrower(item,power)
                        item.power = power.toDouble()
                        lists.value?.set(index,item)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.IO){
                    getData()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    fun tokenURI(index:Int,item: UserNFTItem){
        viewModelScope.launch {
            try {
                async(Dispatchers.IO) {
                    try {
                        val url = model.tokenURI(item.ownerAddress,item.nftAddress, BigInteger(item.tokenId))
                        val nftItemMsgs =
                            model.getNftItemMsg(url, item.ownerAddress, item.nftAddress, item.tokenId)
                        lists.value?.set(index,if(nftItemMsgs.isEmpty()) item else nftItemMsgs.first())
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}