package net.rekall.ui.to_stake.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import net.rekall.R
import net.rekall.bean.ToSettleBean
import net.rekall.bean.ToStakeBean
import net.rekall.bean.ToStakeStatus
import net.rekall.databinding.ItemToSettlementLayoutBinding
import net.rekall.databinding.ItemToStakeLayoutBinding
import net.rekall.utils.ResHelper
import net.rekall.utils.imagehelper.ImageHelper
import java.math.BigDecimal
import java.math.RoundingMode


class ToStakeListAdapter:
    BaseQuickAdapter<ToStakeBean, BaseDataBindingHolder<ItemToStakeLayoutBinding>>(R.layout.item_to_stake_layout) {



    override fun convert(
        holder: BaseDataBindingHolder<ItemToStakeLayoutBinding>,
        item: ToStakeBean
    ) {
        holder.dataBinding?.run {

            tvPower.text = BigDecimal(item.power).toPlainString()
            tvName.text = item.name
            ImageHelper.get().getEngine().displayImageUrl( ivNft,
                item?.image)
           tvStatus.text = ResHelper.getString(when (item.tostakestatus.name){
               ToStakeStatus.IN_LINE.name->R.string.in_line
               ToStakeStatus.APPROVE.name->R.string.approve
               ToStakeStatus.TO_STAKE.name->R.string.to_stake
               ToStakeStatus.FINISH.name->R.string.finish
               ToStakeStatus.NONE.name->R.string.unknown
               ToStakeStatus.FAIL.name->R.string.fail
               else->R.string.unknown
           })
        }
    }
}