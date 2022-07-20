package onekey.rekallutils.ui.start

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseActivity
import onekey.rekallutils.constant.DEFAULT_PRC_URL
import onekey.rekallutils.databinding.ActivityStartBinding
import onekey.rekallutils.ui.MainActivity
import onekey.rekallutils.utils.ToastHelper

class StartActivity : BaseActivity() {

    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, StartActivity::class.java)
            context.startActivity(starter)
        }
    }

    lateinit var mBinding:ActivityStartBinding
    private var privateKey:String = ""
    private var pwd:String = ""
    private var prc_url:String = ""
    private val viewModel = StartViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_start)
        viewModel.getCurrentAccount()
        viewModel.entity.observe(this){
            if(it == null){
                mBinding.etPrivateKey.doAfterTextChanged {
                    privateKey = it?.trim().toString()?:""
                }
                prc_url = DEFAULT_PRC_URL
                mBinding.etPrcUrl.setText(DEFAULT_PRC_URL)
                mBinding.etPwd.doAfterTextChanged {
                    pwd = it?.trim().toString()?:""
                }
                mBinding.etPrcUrl.doAfterTextChanged {
                    prc_url = it?.trim().toString()?:""
                }
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
            }else{
                MainActivity.start(this)
                finish()
            }
        }

    }
}