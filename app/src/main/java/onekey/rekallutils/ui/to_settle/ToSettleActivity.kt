package net.rekall.ui.to_settle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import net.rekall.R
import net.rekall.base.BaseActivity
import net.rekall.bean.ToSettleBean
import net.rekall.bean.ToSettleStatus
import net.rekall.databinding.ActivityToSettleBinding
import net.rekall.ui.to_settle.adapter.ToSettleListAdapter
import java.math.BigDecimal
import java.math.RoundingMode

class ToSettleActivity : BaseActivity() {


    companion object{
        fun start(context:Context,datas :ArrayList<ToSettleBean>,pwd:String){
            val intent = Intent(context, ToSettleActivity::class.java)
            intent.putParcelableArrayListExtra("data",datas)
            intent.putExtra("pwd",pwd)
            context.startActivity(intent)
        }
    }
    lateinit var mBinding: ActivityToSettleBinding

    private val viewModel = ToSettleModel()
    private val mAdapter = ToSettleListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val datas = intent.getParcelableArrayListExtra<ToSettleBean>("data")
       val pwd =  intent.getStringExtra("pwd")
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_to_settle)
        viewModel.lists.value = datas
        mBinding.lifecycleOwner = this
        mBinding.list.layoutManager = LinearLayoutManager(this)
        mBinding.list.adapter =  mAdapter

        viewModel.lists.observe(this){
            mAdapter.setList(it)
            val finishList = it.toMutableList().filter { it.tosettlestatus == ToSettleStatus.FINISH }
            val profit = finishList.map { it.profit }
            var profitNum = 0.0
            profit.forEach { profitNum += it }
            mBinding.finishNumTv.text = "${finishList.size}/${it.size}"
            mBinding.finishCountTv.text = BigDecimal(profitNum).setScale(5, RoundingMode.CEILING).toPlainString()
        }
        pwd?.let { viewModel.settle(pwd) }
    }
}