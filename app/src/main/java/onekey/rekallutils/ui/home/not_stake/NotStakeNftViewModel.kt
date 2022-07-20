package onekey.rekallutils.ui.home.not_stake

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.database.nft.NFTIndex
import onekey.rekallutils.database.nft.NFTItemDBUtils
import onekey.rekallutils.database.nft.StakingState
import onekey.rekallutils.database.nft.UserNFTItem
import onekey.rekallutils.repository.RKRepository
import onekey.rekallutils.ui.home.HomeModel
import onekey.rekallutils.utils.ResHelper
import onekey.rekallutils.utils.ToastHelper
import java.math.BigDecimal
import java.math.BigInteger

class NotStakeNftViewModel : BaseViewModel() {

    val model = HomeModel()
    val isRefreshing = MutableLiveData(false)
    val isFirst = MutableLiveData(true)
    val loading = MutableLiveData(false)
    val lists: MutableLiveData<MutableList<UserNFTItem>> = MutableLiveData()
    fun getNotAll() {
        getCurrentAccount()
        entity.value?.let { accout ->

            lists.postValue(
                NFTItemDBUtils.getNFTListForStatus(accout.address, StakingState.NONE)
                    ?: mutableListOf()
            )
            isRefreshing.value = true
            viewModelScope.launch {
                try {
                    val res = withContext(Dispatchers.IO) {
                        model.allnftIndex(accout.address)
                    }

                    val nftAddressList = mutableListOf<String>()
                    for (i in 0 until res.toInt()) {
                        val nftAddress = withContext(Dispatchers.IO) {
                            model.allnft(BigInteger(i.toString()), accout.address)
                        }
                        nftAddressList.add(nftAddress)
                    }
                    val nftMap = mutableMapOf<String, Long>()
                    val nftIndexMap = mutableMapOf<String, Long>()
                    nftAddressList.forEachIndexed { index, it ->
                        val balanceOf = withContext(Dispatchers.IO) {
                            model.balanceOf(accout.address, it)
                        }
                        nftIndexMap[it] = index.toLong()
                        if (balanceOf.compareTo(BigInteger.ZERO) != 0) {
                            nftMap[it] = balanceOf.toLong()
                        }
                    }
                    if (nftAddressList.isNotEmpty()) {
                        NFTIndex.saveAll(nftIndexMap.map { NFTIndex(it.value.toString(), it.key) })
                    }
                    if (nftMap.isNotEmpty()) {
                        val nfts = mutableListOf<UserNFTItem>()
                        nftMap.forEach {
                            for (j in 0 until it.value) {
                                val tokenId = withContext(Dispatchers.IO) {
                                    model.tokenOfOwnerByIndex(
                                        BigInteger(j.toString()),
                                        accout.address,
                                        it.key
                                    )
                                }
                                NFTItemDBUtils.updateUserNFStatus(
                                    accout.address,
                                    it.key,
                                    tokenId,
                                    StakingState.NONE
                                )
                                val url = withContext(Dispatchers.IO) {
                                    model.tokenURI(accout.address, it.key, tokenId)
                                }

                                nfts.addAll(withContext(Dispatchers.IO) {
                                    model.getNftItemMsg(
                                        url,
                                        accout.address,
                                        it.key,
                                        tokenId.toString()
                                    )
                                })

                            }
                        }
                        if(nfts.isNotEmpty()){
                            getNftPower(nfts.toMutableList())
                        }
                        lists.postValue(nfts)
                    } else {
                        lists.value?.forEach {
                            it.status = StakingState.UNKNOWN.state
                            it.save()
                        }
                        lists.postValue(mutableListOf())
                    }
                    isRefreshing.postValue(false)
                    isFirst.postValue(false)
                } catch (e: Exception) {
                    ToastHelper.showMessage(R.string.refresh_error)
                    isRefreshing.postValue(false)
                    isFirst.postValue(false)
                }
            }
        }?: kotlin.run {
            isRefreshing.postValue(false)
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
                    RKRepository.get().estimateGasFee() * BigDecimal(selectList.size * 2)
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

    fun getFee(): BigDecimal {
        return RKRepository.get().estimateGasFee()
    }

    private suspend  fun  getNftPower(list :MutableList<UserNFTItem>){
        viewModelScope.launch {
            try {
                list.forEachIndexed {
                        index,item->
                    try {
                        val power = model.getPower(item.ownerAddress,item.nftAddress, item.tokenId)
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

    private fun getData() {
        lists.postValue(
            NFTItemDBUtils.getNFTListForStatus(
                entity.value!!.address,
                StakingState.NONE
            ) ?: mutableListOf()
        )
    }
}