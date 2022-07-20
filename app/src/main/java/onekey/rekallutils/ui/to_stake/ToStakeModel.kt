package onekey.rekallutils.ui.to_stake

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2
import kotlinx.coroutines.*
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseViewModel
import onekey.rekallutils.bean.ToStakeBean
import onekey.rekallutils.bean.ToStakeStatus
import onekey.rekallutils.database.nft.NFTIndex
import onekey.rekallutils.ui.home.HomeModel
import onekey.rekallutils.ui.home.TranstactionStatus
import onekey.rekallutils.ui.home.not_stake.NotStakeFragment.Companion.REFRESH_NOT_STAKE
import onekey.rekallutils.utils.ToastHelper

class ToStakeModel : BaseViewModel() {
    val lists = MutableLiveData(mutableListOf<ToStakeBean>())
    val model = HomeModel()


    fun stake(pwd: String) {
        viewModelScope.launch {
            getCurrentAccount()
            try {
                entity.value?.let { account ->
                    withContext(Dispatchers.IO) {
                        while ( canLoop() && viewModelScope.isActive) {
                            val index =
                                lists.value?.indexOfFirst { it.tostakestatus != ToStakeStatus.FINISH  && it.tostakestatus != ToStakeStatus.FAIL}?:-1
                            if(index != -1){
                                val item = lists.value?.get(index)
                                item?.let {
                                    Log.i("testtest","${item.tostakestatus} ${index} ${item.tokenid}")
                                    when(it.tostakestatus){
                                        ToStakeStatus.TO_STAKE->{
                                            val txid = item.txid
                                            val status = model.checkTranstactionStatus(txid)
                                            if (status == TranstactionStatus.SUC) {
                                                item.tostakestatus = ToStakeStatus.FINISH
                                            } else if (status == TranstactionStatus.FAIL) {
                                                item.tostakestatus = ToStakeStatus.FAIL
                                            }
                                            setList(index, item)
                                        }
                                        ToStakeStatus.APPROVE->{
                                            val txid = item.txid
                                            if(txid.isNotBlank() || txid == "already known"){
                                                var status = TranstactionStatus.UNKONWN
                                                if(txid != "already known"){
                                                    status = model.checkTranstactionStatus(txid)
                                                }
                                                if (txid == "already known" || status == TranstactionStatus.SUC) {
                                                    val index2 = NFTIndex.getIndex(item.nftaddress)
                                                    if (index2 != "-1") {
                                                        item.tostakestatus = ToStakeStatus.TO_STAKE
                                                        item.txid = /*"0xa65a5e45ae367d48c6fe7a961b300d36d7eee3d64419c160f002c11978e65a82"*/
                                                            model.stake(
                                                                pwd,
                                                                account.address,
                                                                item.nftaddress,
                                                                index2,
                                                                item.tokenid
                                                            )
                                                    }
                                                } else if (status == TranstactionStatus.FAIL) {
                                                    item.tostakestatus = ToStakeStatus.FAIL
                                                }
                                                setList(index, item)
                                            }
                                        }
                                        ToStakeStatus.IN_LINE->{
                                            val hash = /*"0x941cc0687b5a7b6e10267e17f74ed363b80b14ddd01d8ca2effdaae88d09ee84"*/ model.approve(
                                                account.address,
                                                pwd,
                                                item.nftaddress,item.tokenid
                                            )
                                            if(hash == "already known"){
                                                item.tostakestatus = ToStakeStatus.APPROVE
                                                item.txid = "already known"
                                                setList(index, item)
                                            }else if(hash != ""){
                                                item.tostakestatus = ToStakeStatus.APPROVE
                                                item.txid = hash
                                                setList(index, item)
                                            }
                                        }
                                    }
                                }
                                delay(1000)
                            }else{
                                viewModelScope.cancel()
                            }
                        }
                        LiveDataBus2.get().with(REFRESH_NOT_STAKE).postValue(true)
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
        return lists.value?.indexOfFirst { it.tostakestatus == ToStakeStatus.TO_STAKE ||
                it.tostakestatus == ToStakeStatus.APPROVE ||
                it.tostakestatus == ToStakeStatus.IN_LINE
        } != -1
    }

    private fun setList(index: Int, item: ToStakeBean?) {
        item?.let {
            val data = lists.value?.toMutableList()
            data?.set(index, item)
            lists.value?.clear()
            lists.postValue(data)
        }
    }
}