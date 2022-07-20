package net.rekall.ui.viewpager2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BaseViewPager2Adapter : FragmentStateAdapter {

    private var fragments: List<Fragment> = mutableListOf()

    constructor(fragment: Fragment) : super(fragment)

    constructor(activity: FragmentActivity) : super(activity)

    constructor(activity: FragmentActivity, fragments: List<Fragment>) : super(activity) {
        this.fragments = fragments.toMutableList()
    }

    constructor(fragment: Fragment, fragments: List<Fragment>) : this(fragment) {
        this.fragments = fragments.toMutableList()
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}
