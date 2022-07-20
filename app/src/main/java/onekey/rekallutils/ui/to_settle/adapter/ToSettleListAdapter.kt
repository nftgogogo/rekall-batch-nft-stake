package net.rekall.ui.to_settle.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import net.rekall.R
import net.rekall.bean.ToSettleBean
import net.rekall.bean.ToSettleStatus
import net.rekall.bean.ToStakeStatus
import net.rekall.databinding.ItemToSettlementLayoutBinding
import net.rekall.utils.ResHelper
import net.rekall.utils.imagehelper.ImageHelper
import java.math.BigDecimal
import java.math.RoundingMode


class ToSettleListAdapter:
    BaseQuickAdapter<ToSettleBean, BaseDataBindingHolder<ItemToSettlementLayoutBinding>>(R.layout.item_to_settlement_layout) {



    override fun convert(
        holder: BaseDataBindingHolder<ItemToSettlementLayoutBinding>,
        item: ToSettleBean
    ) {
        holder.dataBinding?.run {

            tvPower.text = BigDecimal(item.power).toPlainString()
            tvName.text = item.name
            tvProfit.text = BigDecimal(item.profit).setScale(5, RoundingMode.CEILING).toPlainString()
            ImageHelper.get().getEngine().displayImageUrl( ivNft,
                item?.image)
            tvStatus.text = ResHelper.getString(when (item.tosettlestatus.name){
                ToSettleStatus.IN_LINE.name->R.string.in_line
                ToSettleStatus.TO_SETTLE.name->R.string.to_settle
                ToSettleStatus.FINISH.name->R.string.finish
                ToSettleStatus.NONE.name->R.string.unknown
                ToSettleStatus.FAIL.name->R.string.fail
                else->R.string.unknown
            })
        }
    }
}