package onekey.rekallutils.ui.to_settle.adapter

import android.graphics.drawable.ColorDrawable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import onekey.rekallutils.R
import onekey.rekallutils.bean.ToSettleBean
import onekey.rekallutils.bean.ToSettleStatus
import onekey.rekallutils.databinding.ItemToSettlementLayoutBinding
import onekey.rekallutils.utils.ResHelper
import onekey.rekallutils.utils.imagehelper.ImageHelper
import java.math.BigDecimal
import java.math.RoundingMode


class ToSettleListAdapter:
    BaseQuickAdapter<ToSettleBean, BaseDataBindingHolder<ItemToSettlementLayoutBinding>>(R.layout.item_to_settlement_layout) {



    override fun convert(
        holder: BaseDataBindingHolder<ItemToSettlementLayoutBinding>,
        item: ToSettleBean
    ) {
        holder.dataBinding?.run {

            tvPower.text = BigDecimal(item.days).toPlainString()
            tvName.text = item.name
            tvProfit.text = BigDecimal(item.profit).setScale(5, RoundingMode.CEILING).toPlainString()
            ImageHelper.get().getEngine().displayImageUrl( ivNft,
                item?.image, errorPlaceHolder = ColorDrawable(ResHelper.getColor(R.color.mainColor))
            )
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