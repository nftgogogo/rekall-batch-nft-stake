package onekey.rekallutils.ui.viewpager2

import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.viewpager2.widget.ViewPager2
import net.lucode.hackware.magicindicator.MagicIndicator

object MagicViewPager2Helper {

    fun bind(
        magicIndicator: MagicIndicator,
        viewPager2: ViewPager2,
        pageSelected: ((Int) -> Unit)? = null,
        onPageScrolled: ((Int, Float, Int) -> Unit)? = null,
        onPageScrollStateChanged: ((Int) -> Unit)? = null
    ) {
        val callback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                magicIndicator.onPageScrollStateChanged(state)
                onPageScrollStateChanged?.invoke(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
                onPageScrolled?.invoke(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                magicIndicator.onPageSelected(position)
                pageSelected?.invoke(position)
            }
        }

        viewPager2.doOnAttach {
            viewPager2.registerOnPageChangeCallback(callback)
        }
        viewPager2.doOnDetach {
            viewPager2.unregisterOnPageChangeCallback(callback)
        }
    }
}