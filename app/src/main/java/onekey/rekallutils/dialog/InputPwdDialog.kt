package onekey.rekallutils.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import onekey.rekallutils.R
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.databinding.DialogInputPwdBinding
import onekey.rekallutils.utils.ToastHelper
import onekey.rekallutils.utils.wallet.Md5Utils

class InputPwdDialog : DialogFragment() {

    companion object {

        fun newInstance(): InputPwdDialog {
            val fragment = InputPwdDialog()
            return fragment
        }
    }

     var comfirmListener:((String)->Unit)?=null

    private lateinit var mBinding: DialogInputPwdBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_input_pwd, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }
    private var pwd = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentAccount = WalletDBUtils.currentAccount()
        mBinding.etPwd.doAfterTextChanged {
            pwd = it?.trim().toString()
        }
        mBinding.cancelBtn.setOnClickListener {
            dismissAllowingStateLoss()
        }
        mBinding.confirmBtn.setOnClickListener {
            if (pwd.isBlank()) {
                ToastHelper.showMessage(R.string.please_enter_the_pwd)
            } else if (Md5Utils.md5(pwd)
                    .compareTo(currentAccount?.password?:"") != 0
            ) {
                ToastHelper.showMessage(R.string.please_enter_correct_pwd)
            }else{
                comfirmListener?.invoke(pwd)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        val window = dialog!!.window

        window?.setBackgroundDrawableResource(R.color.color_33000000)
        val attributes = window!!.attributes

        val displayMetrics = resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels
        attributes.width = widthPixels

        attributes.gravity = Gravity.CENTER

        window.attributes = attributes
    }
}