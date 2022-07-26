package onekey.rekallutils.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

enum class ToSettleStatus{
    IN_LINE,
    //APPROVE,
    TO_SETTLE,FINISH,NONE,FAIL
}
@Parcelize
data class ToSettleBean(
    val owner: String,
    val nftaddress: String,
    val tokenid: String,
    var tosettlestatus: ToSettleStatus,
    var txid: String = "",
    val contenttype: String,
    val name: String,
    val description: String,
    var collection: String,
    val image: String,
    val profit: Double,
    var days: Double,
    var nftpoolAddress:String,
):Parcelable
