package net.rekall.ui.home.adapter

import android.text.TextUtils
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import net.rekall.R
import net.rekall.database.nft.UserNFTItem
import net.rekall.databinding.ItemNftLayoutBinding
import net.rekall.utils.imagehelper.ImageHelper
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.LinkedHashSet


class NFTListAdapter :
    BaseQuickAdapter<UserNFTItem, BaseDataBindingHolder<ItemNftLayoutBinding>>(R.layout.item_nft_layout) {
//    val  checkList: LinkedHashSet<UserNFTItem> = linkedSetOf()
    val  checkIndexList: LinkedHashSet<Int> = linkedSetOf()


    override fun convert(
        holder: BaseDataBindingHolder<ItemNftLayoutBinding>,
        item: UserNFTItem
    ) {
        holder.dataBinding?.run {
            tvName.text = item.name
            tvPower.text = BigDecimal(item.power).toPlainString()
            tvProfit.text = BigDecimal(item.profit).setScale(5,RoundingMode.CEILING).toPlainString()
            ImageHelper.get().getEngine().displayImageUrl( ivNft,
                item?.image)
            if(item.profit.compareTo(0.0) == 0){
                checkbox.isEnabled = false
                checkbox.isChecked = false
               // checkList.removeAll { it.nftAddress == item.nftAddress && it.tokenId == item.tokenId }
                checkIndexList.remove(holder.adapterPosition)
            }else{
                checkbox.isEnabled = true
                //checkbox.isChecked = checkList.indexOfFirst { it.nftAddress == item.nftAddress && it.tokenId == item.tokenId }!=-1
                checkbox.isChecked = checkIndexList.contains(holder.adapterPosition)

                checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if(isChecked){
                        checkIndexList.add(holder.adapterPosition)
                    }else{
                        checkIndexList.remove(holder.adapterPosition)
                    }
                }
            }

        }
    }
}