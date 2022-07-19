package net.rekall.ui.home.adapter

import android.text.TextUtils
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import net.rekall.R
import net.rekall.database.nft.UserNFTItem
import net.rekall.databinding.ItemNftLayoutBinding
import net.rekall.databinding.ItemNotStakeNftLayoutBinding
import net.rekall.utils.ResHelper
import net.rekall.utils.imagehelper.ImageHelper
import java.math.BigDecimal
import java.util.LinkedHashSet


class NotStakeListAdapter:
    BaseQuickAdapter<UserNFTItem, BaseDataBindingHolder<ItemNotStakeNftLayoutBinding>>(R.layout.item_not_stake_nft_layout) {

    val  checkIndexList: LinkedHashSet<Int> = linkedSetOf()

    override fun convert(
        holder: BaseDataBindingHolder<ItemNotStakeNftLayoutBinding>,
        item: UserNFTItem
    ) {
        holder.dataBinding?.run {

            tvPower.text = BigDecimal(item.power).toPlainString()
            tvName.text = item.name
            checkbox.isChecked = checkIndexList.contains(holder.adapterPosition)
            ImageHelper.get().getEngine().displayImageUrl( ivNft,
                item?.image)
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