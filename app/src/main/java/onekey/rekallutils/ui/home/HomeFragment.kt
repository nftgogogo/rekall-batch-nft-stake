package onekey.rekallutils.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import onekey.rekallutils.R
import onekey.rekallutils.databinding.FragmentHomeBinding
import onekey.rekallutils.ui.home.not_stake.NotStakeFragment
import onekey.rekallutils.ui.home.staking.StakingFragment
import onekey.rekallutils.ui.viewpager2.BaseViewPager2Adapter
import onekey.rekallutils.ui.viewpager2.MagicViewPager2Helper
import onekey.rekallutils.ui.widget.IndicatorAdapt
import onekey.rekallutils.utils.ResHelper
import java.util.*

class HomeFragment : Fragment() {


    private lateinit var mBinding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate<FragmentHomeBinding>(inflater, R.layout.fragment_home, null, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTab()
        initVp()
        initIndicator()
    }
    private val mFragments = LinkedList<Fragment>()
    private val mTabs = mutableListOf<String>()
    private fun initVp() {
        mFragments.clear()
        mFragments.add(NotStakeFragment())
        mFragments.add(StakingFragment())
        mBinding.viewPager.adapter = BaseViewPager2Adapter(this, mFragments)
        MagicViewPager2Helper.bind(mBinding.indicator, mBinding.viewPager)
        mBinding.viewPager.offscreenPageLimit = 2
    }


    private fun initTab() {
        mTabs.add(ResHelper.getString(R.string.not_stake))
        mTabs.add(ResHelper.getString(R.string.staking))
    }

    private fun initIndicator(){
       val  mAdapt =  IndicatorAdapt(requireContext(),mTabs)
        mAdapt.setOnIndicatorTapClickListener {
            mBinding.viewPager.setCurrentItem(it,true)
        }
        val commonNavigator= CommonNavigator(requireContext())
        commonNavigator.isAdjustMode = true
        commonNavigator.adapter = mAdapt
        mBinding.indicator.navigator = commonNavigator
    }
}