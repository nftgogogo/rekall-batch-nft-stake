package onekey.rekallutils.ui.to_settle

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2
import kotlinx.coroutines.*
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.bean.ToSettleBean
import onekey.rekallutils.bean.ToSettleStatus
import onekey.rekallutils.database.nft.NFTIndex
import onekey.rekallutils.ui.home.HomeModel
import onekey.rekallutils.ui.home.TranstactionStatus
import onekey.rekallutils.ui.home.staking.StakingFragment
import onekey.rekallutils.utils.ToastHelper

class ToSettleModel : BaseViewModel() {
    val lists = MutableLiveData<MutableList<ToSettleBean>>()
    val model = HomeModel()

    fun settle(pwd: String) {
        viewModelScope.launch {
            getCurrentAccount()
            try {
                entity.value?.let { account ->
                    withContext(Dispatchers.IO) {
                        while ( canLoop() && viewModelScope.isActive) {
                            val index =
                                lists.value?.indexOfFirst { it.tosettlestatus != ToSettleStatus.FINISH
                                        && it.tosettlestatus != ToSettleStatus.FAIL}?:-1
                            if(index != -1){
                                val item = lists.value?.get(index)
                                item?.let {
                                    Log.i("testtest","${item.tosettlestatus} ${index} ${item.tokenid}")
                                    when(it.tosettlestatus){
                                        ToSettleStatus.TO_SETTLE->{
                                            val txid = item.txid
                                            val status = model.checkTranstactionStatus(txid)
                                            if (status == TranstactionStatus.SUC) {
                                                item.tosettlestatus = ToSettleStatus.FINISH
                                            } else if (status == TranstactionStatus.FAIL) {
                                                item.tosettlestatus = ToSettleStatus.FAIL
                                            }
                                            setList(index, item)
                                        }
                                      /*  ToSettleStatus.APPROVE->{
                                            val txid = item.txid
                                            if(txid.isNotBlank() || txid == "already known"){
                                                var status = TranstactionStatus.UNKONWN
                                                if(txid != "already known"){
                                                    status = model.checkTranstactionStatus(txid)
                                                }
                                                if (txid == "already known" || status == TranstactionStatus.SUC) {
                                                    val index2 = NFTIndex.getIndex(item.nftaddress)
                                                    if (index2 != "-1") {
                                                        item.tosettlestatus = ToSettleStatus.TO_SETTLE
                                                        item.txid = *//*"0xa65a5e45ae367d48c6fe7a961b300d36d7eee3d64419c160f002c11978e65a82"*//*
                                                            model.settle(
                                                                pwd,
                                                                account.address,
                                                                index2,
                                                                item.tokenid
                                                            )
                                                    }
                                                } else if (status == TranstactionStatus.FAIL) {
                                                    item.tosettlestatus = ToSettleStatus.FAIL
                                                }
                                                setList(index, item)
                                            }
                                        }*/
                                        ToSettleStatus.IN_LINE->{
                                            val index2 = NFTIndex.getIndex(item.nftaddress)
                                            if (index2 != "-1") {
                                               val hash =  model.settle(
                                                    pwd,
                                                    account.address,
                                                    index2,
                                                    item.tokenid,item.nftpoolAddress
                                                )
                                                if(hash.isNotBlank()){
                                                    item.tosettlestatus = ToSettleStatus.TO_SETTLE
                                                    item.txid = hash//*"0xa65a5e45ae367d48c6fe7a961b300d36d7eee3d64419c160f002c11978e65a82"*//*
                                                    setList(index, item)
                                                }

                                            }
                                        }
                                    }
                                }
                                delay(1000)
                            }else{
                                viewModelScope.cancel()
                            }
                        }
                        LiveDataBus2.get().with(StakingFragment.REFRESH_STAKING).postValue(true)
                        val data = lists.value?.toMutableList()
                        lists.value?.clear()
                        lists.postValue(data)
                    }
                }
            }catch (e:CancellationException){

            } catch (e:Exception){
                e.printStackTrace()
                ToastHelper.showMessage(R.string.str_http_network_error)
            }
        }
    }

    private fun canLoop(): Boolean {
        return lists.value?.indexOfFirst { it.tosettlestatus == ToSettleStatus.TO_SETTLE
               /*  || it.tosettlestatus == ToSettleStatus.APPROVE */||
                it.tosettlestatus == ToSettleStatus.IN_LINE
        } != -1
    }

    private fun setList(index: Int, item: ToSettleBean?) {
        item?.let {
            val data = lists.value?.toMutableList()
            data?.set(index, item)
            lists.value?.clear()
            lists.postValue(data)
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}