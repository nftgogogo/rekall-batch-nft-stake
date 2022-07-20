package onekey.rekallutils.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window.FEATURE_NO_TITLE
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseActivity
import onekey.rekallutils.constant.PRC_URL_KEY
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.database.nft.NFTItemDBUtils
import onekey.rekallutils.databinding.ActivityMainBinding
import onekey.rekallutils.ui.home.HomeFragment
import onekey.rekallutils.ui.start.StartActivity
import onekey.rekallutils.ui.viewpager2.BaseViewPager2Adapter
import onekey.rekallutils.ui.viewpager2.MagicViewPager2Helper
import onekey.rekallutils.ui.wallet.WalletFragment
import onekey.rekallutils.ui.widget.MainNavigator
import onekey.rekallutils.utils.MMKVUtils
import onekey.rekallutils.utils.wallet.ETHWalletUtils
import java.util.*

class MainActivity : BaseActivity() {

    companion object{

        const val LOG_OUT = "LOG_OUT"

        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            context.startActivity(starter)
        }
    }

    private val mFragments = LinkedList<Fragment>()
    lateinit var mBinding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        initTab()
        initVp()
        initTabData()

        LiveDataBus2.get().with(LOG_OUT).observe(this){
            WalletDBUtils.getAll()?.forEach {
                ETHWalletUtils.deleteWallet(it.id)
                NFTItemDBUtils.deleteUser(it.address)
                MMKVUtils.get().remove(PRC_URL_KEY)

            }
            StartActivity.start(this)
        }
    }

    private fun initVp() {
        mFragments.clear()
        mFragments.add(HomeFragment())
        mFragments.add(WalletFragment())
        mBinding.viewPager.adapter = BaseViewPager2Adapter(this, mFragments)
        MagicViewPager2Helper.bind(mBinding.indicator, mBinding.viewPager)
        mBinding.viewPager.isUserInputEnabled = false
        mBinding.viewPager.offscreenPageLimit = 2
    }

    private val mTabs = mutableListOf<Int>()
    private val tabTitles = mutableListOf<String>()
    private val mTabsClick = mutableListOf<Int>()

    private fun initTab() {
        mTabs.add(R.drawable.tab_home_unselect)
        mTabs.add(R.drawable.tab_wallet_unselect)
        mTabsClick.add(R.drawable.tab_home_select)
        mTabsClick.add(R.drawable.tab_wallet_select)
        tabTitles.add(getString(R.string.tab_home))
        tabTitles.add(getString(R.string.tab_wallet))
    }


    private fun initTabData() {
        val navigator = MainNavigator(this)
        mBinding.indicator.navigator = navigator
        navigator.isAdjustMode = true
        navigator.adapter = object : CommonNavigatorAdapter() {

            var selectIndex = 0

            override fun getCount(): Int {
                return mFragments.size
            }

            override fun getTitleView(
                context: Context,
                i: Int
            ): IPagerTitleView {
                val commonPagerTitleView = CommonPagerTitleView(context)
                val customLayout: View =
                    LayoutInflater.from(context).inflate(R.layout.item_main_tab, null)
                val titleImg =
                    customLayout.findViewById<ImageView>(R.id.main_tab_iv)
                val title =
                    customLayout.findViewById<TextView>(R.id.main_tab_tv)
                titleImg.setImageResource(mTabs[i])
                title.text = tabTitles[i]
                commonPagerTitleView.tag = i
                commonPagerTitleView.onPagerTitleChangeListener = object :
                    CommonPagerTitleView.OnPagerTitleChangeListener {
                    override fun onSelected(i: Int, i1: Int) {
                        selectIndex = i
                        titleImg.alpha = 1.0f
                        title.alpha = 1.0f
                        titleImg.setImageResource(mTabsClick[i])
                        title.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                               R.color.mainColor
                            )
                        )
                        mBinding.indicator.postInvalidate()
                    }

                    override fun onDeselected(i: Int, i1: Int) {
                        titleImg.alpha = 0.5f
                        title.alpha = 0.5f
                        titleImg.setImageResource(mTabs[i])
                        title.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                 R.color.mainGrayColor
                            )
                        )
                    }

                    override fun onLeave(
                        i: Int,
                        i1: Int,
                        leavePercent: Float,
                        leftToRight: Boolean
                    ) {
                        /*  titleImg.animate().scaleX(1.0f + (0.9f - 1.0f) * leavePercent)
                              .scaleY(1.0f + (0.9f - 1.0f) * leavePercent)
                              .setDuration(100)
                              .start()*/
                    }

                    override fun onEnter(
                        i: Int,
                        i1: Int,
                        enterPercent: Float,
                        leftToRight: Boolean
                    ) {
                        /* titleImg.animate().scaleX(0.9f + (1.0f - 0.9f) * enterPercent)
                             .scaleY(0.9f + (1.0f - 0.9f) * enterPercent)
                             .setDuration(0)
                             .start()*/
                    }
                }

                commonPagerTitleView.setOnClickListener {
                    mBinding.dividerLine.visibility =
                        if (commonPagerTitleView.tag == 0) View.GONE else View.VISIBLE
                    mBinding.viewPager.setCurrentItem(it.tag as Int, false)
                    navigator.onPageSelected(i)
                }
                commonPagerTitleView.setContentView(customLayout)
                return commonPagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
                return null
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(false)
    }

}