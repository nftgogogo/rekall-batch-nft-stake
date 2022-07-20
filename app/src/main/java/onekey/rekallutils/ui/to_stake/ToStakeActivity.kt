package onekey.rekallutils.ui.to_stake

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseActivity
import onekey.rekallutils.bean.ToStakeBean
import onekey.rekallutils.bean.ToStakeStatus
import onekey.rekallutils.databinding.ActivityToStakeBinding
import onekey.rekallutils.ui.to_stake.adapter.ToStakeListAdapter

class ToStakeActivity : BaseActivity() {


    companion object{
        fun start(context:Context, datas :ArrayList<ToStakeBean>, pwd:String){
            val intent = Intent(context, ToStakeActivity::class.java)
            intent.putParcelableArrayListExtra("data",datas)
            intent.putExtra("pwd",pwd)
            context.startActivity(intent)
        }
    }
    lateinit var mBinding: ActivityToStakeBinding

    private val viewModel = ToStakeModel()
    private val mAdapter = ToStakeListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val datas = intent.getParcelableArrayListExtra<ToStakeBean>("data")
       val pwd =  intent.getStringExtra("pwd")
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_to_stake)
        viewModel.lists.value = datas
        mBinding.lifecycleOwner = this
        mBinding.list.layoutManager = LinearLayoutManager(this)
        mBinding.list.adapter =  mAdapter

        viewModel.lists.observe(this){
            mAdapter.setList(it)
            val finishList = it.toMutableList().filter { it.tostakestatus == ToStakeStatus.FINISH }
            mBinding.finishNumTv.text = "${finishList.size}/${it.size}"
        }
        pwd?.let { viewModel.stake(pwd) }
    }
}