package net.rekall.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class ToStakeStatus{
    IN_LINE,APPROVE,TO_STAKE,FINISH,NONE,FAIL
}
@Parcelize
data class ToStakeBean(
    val owner:String, val nftaddress:String, val tokenid:String, var tostakestatus: ToStakeStatus, var txid:String = "",
    val contenttype:String, val name:String, val description:String, var collection:String, val image:String,val power:Double
):Parcelable
