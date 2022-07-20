package onekey.rekallutils.ui.home.staking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseFragment
import onekey.rekallutils.bean.ToSettleBean
import onekey.rekallutils.bean.ToSettleStatus
import onekey.rekallutils.databinding.FragmentStakingStakeBinding
import onekey.rekallutils.dialog.InputPwdDialog
import onekey.rekallutils.ui.home.adapter.NFTListAdapter
import onekey.rekallutils.ui.to_settle.ToSettleActivity
import onekey.rekallutils.utils.ToastHelper

class StakingFragment : BaseFragment() {

    companion object{
        val  REFRESH_STAKING = "REFRESH_STAKING"
    }

    private lateinit var mBinding: FragmentStakingStakeBinding
    private val viewModel = StakingNftViewModel()
    private val  mAdapter = NFTListAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_staking_stake, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }
    private var isFirst = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.list.layoutManager = LinearLayoutManager(requireContext())
        mBinding.list.adapter =  mAdapter
        mBinding.refreshLayout.setEnableLoadMore(false)

        LiveDataBus2.get().with(REFRESH_STAKING).observe(mBinding.lifecycleOwner!!){
            mBinding.refreshLayout.autoRefresh()
        }

        mBinding.refreshLayout.setOnRefreshListener {
            viewModel.getStakeListWithUser()
        }
        viewModel.lists.observe(mBinding.lifecycleOwner!!){
            mAdapter.setList(it)

            isFirst = false
        }
        viewModel.isRefreshing.observe(mBinding.lifecycleOwner!!){
            if(!it){
                mBinding.checkboxSelectAll.isChecked = false
                mAdapter.checkIndexList.clear()
                mBinding.refreshLayout.finishRefresh()
            }
        }
        mBinding.selectAll.setOnClickListener {
            mBinding.checkboxSelectAll.isChecked = !mBinding.checkboxSelectAll.isChecked
        }
        mBinding.checkboxSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
              /*  mAdapter.checkList.clear()
                mAdapter.checkList.addAll(mAdapter.data.toMutableList().filter { it.profit.compareTo(0.0) != 0 })*/
                  mAdapter.checkIndexList.clear()
                    mAdapter.data.forEachIndexed { index, userNFTItem ->
                        if(userNFTItem.profit.compareTo(0.0) != 0 ){
                            mAdapter.checkIndexList.add(index)
                        }
                    }
            }else{
                //mAdapter.checkList.clear()
                mAdapter.checkIndexList.clear()
            }
            mAdapter.notifyDataSetChanged()
        }
        mBinding.toStake.setOnClickListener {
            val selectList = mAdapter.data.toMutableList().filterIndexed { index, userNFTItem ->
                mAdapter.checkIndexList.contains(index)
            }.toMutableList()
            if (mAdapter.checkIndexList.isEmpty()) {
                ToastHelper.showMessage(R.string.peleas_select_one)
                return@setOnClickListener
            }
            viewModel.getBalanceChange(selectList){
                if(viewModel.isRefreshing.value != false){
                    ToastHelper.showMessage(R.string.please_wait_init_data)
                    return@getBalanceChange
                }
                if(mAdapter.checkIndexList.isEmpty()){
                    ToastHelper.showMessage(R.string.peleas_select_one)
                }else{
                    val dialog = InputPwdDialog.newInstance()
                    dialog.comfirmListener = {
                        val temp = selectList
                        val datas = temp.map { item ->
                            ToSettleBean(
                                item.ownerAddress,
                                item.nftAddress,
                                item.tokenId,
                                ToSettleStatus.IN_LINE,
                                "",
                                item.contentType,
                                item.name,
                                item.description,
                                item.collection,
                                item.image,item.profit,item.power
                            )
                        }
                        ToSettleActivity.start(requireContext(), ArrayList(datas),it)
                        dialog.dismissAllowingStateLoss()
                    }
                    dialog.show(parentFragmentManager,dialog.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            mBinding.refreshLayout.autoRefresh()
        }
    }
}