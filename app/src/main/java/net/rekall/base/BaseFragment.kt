package net.rekall.base

import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

open class BaseFragment : Fragment(), CoroutineScope by MainScope() {
}