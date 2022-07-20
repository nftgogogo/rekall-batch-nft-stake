package net.rekall.database.nft

import org.litepal.LitePal
import org.litepal.crud.LitePalSupport

class NFTIndex : LitePalSupport {

    companion object{
       fun saveAll(list:List<NFTIndex>){
           val itemList = mutableListOf<NFTIndex>()
           list.forEach {
               it.saveOrUpdate( "nftAddress = ?", it.nftAddress)
           }

        }
        fun getIndex(nftAddress:String):String{
           return  LitePal.where( "nftAddress = ?", nftAddress).findFirst(NFTIndex::class.java)?.index?:"-1"

        }
    }
    constructor()

    constructor(index:String,nftAddress:String){
        this.index = index
        this.nftAddress = nftAddress
    }

    var id:Long = -1
    var index:String = ""
    var nftAddress:String = ""
}