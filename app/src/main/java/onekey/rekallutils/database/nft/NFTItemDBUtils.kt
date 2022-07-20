package net.rekall.database.nft

import org.litepal.LitePal
import java.math.BigDecimal
import java.math.BigInteger

class NFTItemDBUtils {
    companion object{
        fun  saveUserNFTAll(items :List<UserNFTItem>): MutableList<UserNFTItem> {
            val itemList = mutableListOf<UserNFTItem>()
            items.forEach {
                val item = LitePal.where(
                    "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                    it.ownerAddress,
                    it.nftAddress,
                    it.tokenId
                ).findFirst(UserNFTItem::class.java)
                if(item == null){
                    it.save()
                    itemList.add(it)
                }else{
                    it.update(item.id)
                    itemList.add(item)
                }
            }
            return itemList
        }

        fun saveProfit(item:UserNFTItem,profit:BigDecimal){
            val items = LitePal.where(
                "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                item.ownerAddress,
                item.nftAddress,
                item.tokenId.toString()
            ).find(UserNFTItem::class.java)
            if(items.isNotEmpty()){
                items.forEach {
                    it.profit = profit.toDouble()
                    it.save()
                }
            }
        }

        fun savePrower(item:UserNFTItem,power:BigDecimal){
            val items = LitePal.where(
                "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                item.ownerAddress,
                item.nftAddress,
                item.tokenId.toString()
            ).find(UserNFTItem::class.java)
            if(items.isNotEmpty()){
                items.forEach {
                    it.power = power.toDouble()
                    it.save()
                }
            }
        }


        fun deleteUser(address:String){
             LitePal.deleteAll(UserNFTItem::class.java,"ownerAddress = ?", address)
        }

        fun getNFTListForStatus(owner:String,status: StakingState): MutableList<UserNFTItem>? {
         return LitePal.where("ownerAddress = ? and status = ?", owner,status.state.toString()).find(UserNFTItem::class.java)
        }

        fun  updateUserNFStatus(
            ownerAddress: String,
            nftAddress: String,
            tokenId: BigInteger,
            status: StakingState
        ) {
                val items = LitePal.where(
                    "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                   ownerAddress,
                    nftAddress,
                     tokenId.toString()
                ).find(UserNFTItem::class.java)
            if(items.isEmpty()){
                UserNFTItem(ownerAddress,nftAddress,tokenId,status).save()
            }else{
                items.forEach {
                    it.status = status.state
                    it.save()
                }
            }
        }
    }
}