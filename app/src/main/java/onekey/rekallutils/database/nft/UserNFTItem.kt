package onekey.rekallutils.database.nft

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.litepal.annotation.Column
import org.litepal.crud.LitePalSupport
import java.math.BigInteger

enum class StakingState(var state: Long) {
    STAKING(1),
    NONE(0),
    UNKNOWN(-1),
}
@Parcelize
class UserNFTItem() : Parcelable, LitePalSupport() {


    constructor(ownerAddress: String,nftAddress:String,tokenId:BigInteger,state: StakingState) : this() {
        this.ownerAddress = ownerAddress
        this.nftAddress = nftAddress
        this.tokenId = tokenId.toString()
        this.status = state.state
    }

    constructor(ownerAddress: String, nftAddress:String, tokenId:String, state: StakingState,
                contentType:String, name:String, description:String, collection:String, image:String,nftPoolAddress:String?=null) : this() {
        this.ownerAddress = ownerAddress
        this.nftAddress = nftAddress
        this.tokenId = tokenId
        this.status = state.state
        this.contentType = contentType
        this.name = name
        this.description = description
        this.collection = collection
        this.image = image
        this.nftPoolAddress = nftPoolAddress
    }



    var id:Long = -1
    var ownerAddress = ""
    var nftAddress = ""

    @Column
    var tokenId: String = ""
    var status = StakingState.NONE.state
    var contentType = ""
    var name = ""
    var description = ""
    var collection = ""
    var image = ""
    var profit = 0.0
    var power = 0.0
    var days = 0.0
    var nftPoolAddress:String? = null
}