package onekey.rekallutils.ui.home.not_stake

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2
import onekey.rekallutils.R
import onekey.rekallutils.bean.ToStakeBean
import onekey.rekallutils.bean.ToStakeStatus
import onekey.rekallutils.databinding.FragmentNotStakeBinding
import onekey.rekallutils.dialog.InputPwdDialog
import onekey.rekallutils.ui.home.adapter.NotStakeListAdapter
import onekey.rekallutils.ui.to_stake.ToStakeActivity
import onekey.rekallutils.utils.ToastHelper

class NotStakeFragment : Fragment() {

    companion object {
        const val REFRESH_NOT_STAKE = "REFRESH_NOT_STAKE"
    }

    private lateinit var mBinding: FragmentNotStakeBinding
    private val viewModel = NotStakeNftViewModel()
    private val mAdapter = NotStakeListAdapter()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate<FragmentNotStakeBinding>(
            inflater,
            R.layout.fragment_not_stake,
            container,
            false
        )
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    private var isFirst = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.list.layoutManager = LinearLayoutManager(requireContext())
        mBinding.list.adapter = mAdapter
        mBinding.refreshLayout.setEnableLoadMore(false)


        LiveDataBus2.get().with(REFRESH_NOT_STAKE)
            .observe(mBinding.lifecycleOwner!!) {
                mBinding.refreshLayout.autoRefresh()
            }

        mBinding.refreshLayout.setOnRefreshListener {
            viewModel.getNotAll()
        }
        viewModel.lists.observe(mBinding.lifecycleOwner!!) {
            mBinding.checkboxSelectAll.isChecked = false
            mAdapter.checkIndexList.clear()
            mAdapter.setList(it)
            isFirst = false
        }
        viewModel.isRefreshing.observe(mBinding.lifecycleOwner!!) {
            if (!it) {
                mBinding.refreshLayout.finishRefresh()
            }
        }
        mBinding.selectAll.setOnClickListener {
            mBinding.checkboxSelectAll.isChecked = !mBinding.checkboxSelectAll.isChecked
        }
        mBinding.checkboxSelectAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mAdapter.checkIndexList.clear()
                mAdapter.checkIndexList.addAll(mAdapter.data.toMutableList().mapIndexed { index, userNFTItem ->  index})
            } else {
                mAdapter.checkIndexList.clear()
            }
            mAdapter.notifyDataSetChanged()
        }
        mBinding.toStakeBtn.setOnClickListener {
            val selectList = mAdapter.data.toMutableList().filterIndexed { index, userNFTItem ->
                mAdapter.checkIndexList.contains(index)
            }.toMutableList()
            if (mAdapter.checkIndexList.isEmpty()) {
                ToastHelper.showMessage(R.string.peleas_select_one)
                return@setOnClickListener
            }
            viewModel.getBalanceChange(selectList){
                if (viewModel.isRefreshing.value != false) {
                    ToastHelper.showMessage(R.string.please_wait_init_data)
                    return@getBalanceChange
                }
                if (mAdapter.checkIndexList.isEmpty()) {
                    ToastHelper.showMessage(R.string.peleas_select_one)
                } else {
                    val dialog = InputPwdDialog.newInstance()
                    dialog.comfirmListener = {
                        val temp = selectList
                        val datas = temp.map { item ->
                            ToStakeBean(
                                item.ownerAddress,
                                item.nftAddress,
                                item.tokenId,
                                ToStakeStatus.IN_LINE,
                                "",
                                item.contentType,
                                item.name,
                                item.description,
                                item.collection,
                                item.image,item.power
                            )
                        }
                        ToStakeActivity.start(requireContext(), ArrayList(datas), it)
                        dialog.dismissAllowingStateLoss()
                    }
                    dialog.show(parentFragmentManager, dialog.toString())
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