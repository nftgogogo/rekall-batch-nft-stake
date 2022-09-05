package onekey.rekallutils.ui.home.adapter

import android.graphics.drawable.ColorDrawable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import onekey.rekallutils.R
import onekey.rekallutils.database.nft.UserNFTItem
import onekey.rekallutils.databinding.ItemNotStakeNftLayoutBinding
import onekey.rekallutils.utils.ResHelper
import onekey.rekallutils.utils.imagehelper.ImageHelper
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
                item?.image, errorPlaceHolder = ColorDrawable(ResHelper.getColor(R.color.mainColor))
            )
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