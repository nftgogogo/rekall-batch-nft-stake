package net.rekall.ui.home.staking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.rekall.R
import net.rekall.base.BaseViewModel
import net.rekall.database.WalletDBUtils
import net.rekall.database.nft.NFTItemDBUtils
import net.rekall.database.nft.StakingState
import net.rekall.database.nft.UserNFTItem
import net.rekall.repository.RKRepository
import net.rekall.ui.home.HomeModel
import net.rekall.utils.ResHelper
import net.rekall.utils.ToastHelper
import org.litepal.LitePal
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