package onekey.rekallutils.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import onekey.rekallutils.R
import onekey.rekallutils.constant.DEFAULT_PRC_URL
import onekey.rekallutils.constant.PRC_URL_KEY
import onekey.rekallutils.databinding.FragmentWalletBinding
import onekey.rekallutils.ui.start.StartActivity
import onekey.rekallutils.utils.MMKVUtils
import onekey.rekallutils.utils.ToastHelper

class WalletFragment: Fragment() {

    private lateinit var mBinding:FragmentWalletBinding
    private val viewModel = WalletViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }
    private var privateKey:String = ""
    private var pwd:String = ""
    private var prc_url:String = ""
    private var reset_prc_url:String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.entity.observe(mBinding.lifecycleOwner!!){
            mBinding.flInLog.visibility = if(it !=null) View.VISIBLE else View.GONE
            mBinding.flLogOut.visibility = if(it ==null) View.VISIBLE else View.GONE
            if(it != null){
                mBinding.tvAddress.text = it.address
                mBinding.tvBnbBalance.text = "${if(it.BNBBalance.compareTo(-1.0) == 0) "-" else it.BNBBalance.toString()} BNB"
                mBinding.tvEkaBalance.text = "${if(it.ekaBalance.compareTo(-1.0) == 0) "-" else it.ekaBalance.toString()} EKA"
                mBinding.tvPrcUrl.text = MMKVUtils.getString(PRC_URL_KEY)
                mBinding.logoutBtn.setOnClickListener {
                    viewModel.removeAll()
                    activity?.finish()
                    StartActivity.start(requireContext())
                }
                mBinding.etPrcUrlReset.doAfterTextChanged {
                    reset_prc_url = it?.trim().toString()?:""
                }
                mBinding.resetPrcUrl.setOnClickListener {
                    mBinding.llResetPrcUrl.visibility = View.VISIBLE
                }
                mBinding.resetPrcUrlBtn.setOnClickListener {
                    if(reset_prc_url.isBlank()){
                        ToastHelper.showMessage(R.string.please_enter_the_prc_url)
                        return@setOnClickListener
                    }
                    viewModel.resetPRCURL(reset_prc_url)
                    mBinding.tvPrcUrl.text = reset_prc_url
                    ToastHelper.showMessage(R.string.success)
                    mBinding.llResetPrcUrl.visibility = View.GONE
                }
                mBinding.refresh.setOnClickListener {
                    viewModel.getBalance()
                }
            }else{
                mBinding.etPrivateKey.doAfterTextChanged {
                    privateKey = it?.trim().toString()?:""
                }
                mBinding.etPwd.doAfterTextChanged {
                    pwd = it?.trim().toString()?:""
                }
                mBinding.etPrcUrl.doAfterTextChanged {
                    prc_url = it?.trim().toString()?:""
                }
                mBinding.etPrcUrl.setText(DEFAULT_PRC_URL)
                mBinding.comfirmBtn.setOnClickListener {
                    if(privateKey.isBlank()){
                        ToastHelper.showMessage(R.string.please_enter_private_key)
                        return@setOnClickListener
                    }
                    if(pwd.isBlank()){
                        ToastHelper.showMessage(R.string.please_enter_the_pwd)
                        return@setOnClickListener
                    }
                    if(prc_url.isBlank()){
                        ToastHelper.showMessage(R.string.please_enter_the_prc_url)
                        return@setOnClickListener
                    }
                    viewModel.privateKeyImport(privateKey, pwd,prc_url)
                }
            }
        }
        viewModel.getCurrentAccount()
    }
}