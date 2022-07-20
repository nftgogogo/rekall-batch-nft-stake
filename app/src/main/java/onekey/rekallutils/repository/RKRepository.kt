package net.rekall.repository

import android.util.Log
import app.easypocket.lib.utils.livedataBus2.LiveDataBus2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.rekall.App
import net.rekall.BuildConfig
import net.rekall.base.BaseConfig
import net.rekall.base.BaseConfig.Companion.BNBDecimal
import net.rekall.constant.DEFAULT_PRC_URL
import net.rekall.constant.ETHGasConstants
import net.rekall.constant.PRC_URL_KEY
import net.rekall.database.AccountWalletEntity
import net.rekall.ui.MainActivity
import net.rekall.utils.MMKVUtils
import net.rekall.utils.wallet.BalanceUtils
import net.rekall.utils.wallet.Md5Utils
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.*
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow


class RKRepository {
    private constructor()




    private fun getPRCURL():String{
        val prcUrl = MMKVUtils.getString(PRC_URL_KEY, DEFAULT_PRC_URL)
        if(prcUrl.isBlank()){
            LiveDataBus2.get().with(MainActivity.LOG_OUT).postValue(true)
        }
        return prcUrl
    }

    fun reBuildHttpService(){
        httpService = HttpService(
            getPRCURL(),
            initOKhttpClient(),
            false)
        web3j = Web3j.build(httpService)
    }



    private val TIME_OUT = 60 * 1000L
    private val AUTHORIZATION = "Authorization"
    private val ETH_CHAIN_ID = 1
    private val cacheSize: Int = 10 * 10 * 1024

    private fun initOKhttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        return OkHttpClient.Builder()
            .cache(
                Cache(
                    File(App.get().cachePath),
                    cacheSize.toLong()
                )
            )
            .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .callTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
            .addInterceptor(httpLoggingInterceptor)
          //  .addInterceptor(HttpResponseCodeInterceptor())
            .build()

    }

    private var httpService: HttpService  = HttpService(
        getPRCURL(),
        initOKhttpClient(),
        false)

    fun web3j(): Web3j {
        return this.web3j
    }

    private var web3j: Web3j = Web3j.build(httpService)

    fun getEthChainId(): Int {
        return ETH_CHAIN_ID
    }

    suspend fun getGasPrice(): BigInteger {

        return withContext(Dispatchers.IO) {
            try {
                val price = web3j
                    .ethGasPrice()
                    .send()
                if (price.gasPrice >= BalanceUtils.gweiToWei(BigDecimal.ONE)
                ) {
                    return@withContext price.gasPrice
                } else {
                    //didn't update the current price correctly, switch to default:
                    return@withContext BigInteger(ETHGasConstants.DEFAULT_GAS_PRICE)
                }
            } catch (ex: Exception) {
                return@withContext BigInteger(ETHGasConstants.DEFAULT_GAS_PRICE)
            }
        }
    }

    fun getBalance(fromAddress: String): BigDecimal {
        try {
            val result = web3j().ethGetBalance(fromAddress, DefaultBlockParameterName.LATEST).send()
            val balance = BigDecimal(result.balance)
            val fromWei = Convert.fromWei(balance, Convert.Unit.ETHER)
            return fromWei
        } catch (e: Exception) {
            return BigDecimal.ZERO
        }
    }

    fun getERCBalance(address: String, contractAddress: String, decimal: Int): BigDecimal {

        try {

            val methodName = "balanceOf"
            val inputParameters: MutableList<Type<*>> = java.util.ArrayList()
            val outputParameters: MutableList<TypeReference<*>> = java.util.ArrayList()
            inputParameters.add(Address(address))
            val typeReference: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
            outputParameters.add(typeReference)
            val function = Function(methodName, inputParameters, outputParameters)
            val data = FunctionEncoder.encode(function)
            val transaction =
                Transaction.createEthCallTransaction(address, contractAddress, data)
            val ethCall = web3j().ethCall(transaction, DefaultBlockParameterName.LATEST).send()
            val results = FunctionReturnDecoder.decode(ethCall.value, function.outputParameters)
            val bi = results[0].value as BigInteger
            val balanceDecimal = BigDecimal(bi)

            return balanceDecimal.movePointLeft(decimal).stripTrailingZeros()
        } catch (e: java.lang.Exception) {
            return BigDecimal.ZERO
        }
    }


    /**
     */
    suspend fun createTransaction(
        from: AccountWalletEntity,
        to: String?,
        contractAddress: String? = "",
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        password: String,
        info: String = ""
    ): EthSendTransaction {
        return if (contractAddress.isNullOrBlank()) {
            createEthTransaction(from, to, amount, gasPrice, gasLimit, password, info)
        } else {
            createERC20Transfer(
                from,
                to,
                contractAddress,
                amount,
                gasPrice,
                gasLimit,
                password,
                info
            )
        }
    }


    /**
     */
    fun isContractAddress(address: String): Boolean {
        val ethGetCode = web3j.ethGetCode(address, DefaultBlockParameterName.LATEST)
        val code = ethGetCode.send().code
        return "0x" != code
    }

    // transfer ether
    suspend fun createEthTransaction(
        from: AccountWalletEntity,
        to: String?,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        password: String,
        info: String = ""
    ): EthSendTransaction {
        val nonce = getLastTransactionNonce(web3j, from.address)
        return withContext(Dispatchers.IO) {
            val sendInfo = Numeric.toHexString(info.toByteArray())
            val credentials: Credentials =
                WalletUtils.loadCredentials(Md5Utils.md5(Md5Utils.md5(password)), from.keystorePath)
            val rawTransaction =
                RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    to,
                    amount
                )
            val signedMessage =
                TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)
            val ethSendTransaction =
                web3j.ethSendRawTransaction(hexValue).send()
            ethSendTransaction
        }


    }

    // transfer ERC20
    suspend fun createERC20Transfer(
        from: AccountWalletEntity,
        to: String?,
        contractAddress: String?,
        amount: BigInteger?,
        gasPrice: BigInteger?,
        gasLimit: BigInteger?,
        password: String,
        info: String = ""
    ): EthSendTransaction {
        val callFuncData: String = createTokenTransferData(to, amount)
        val nonce = getLastTransactionNonce(web3j, from.address)
        return withContext(Dispatchers.IO) {
            val credentials: Credentials =
                WalletUtils.loadCredentials(Md5Utils.md5(Md5Utils.md5(password)), from.keystorePath)
            val rawTransaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, contractAddress, callFuncData
            )
            val signedMessage =
                TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)
            val ethSendTransaction =
                web3j.ethSendRawTransaction(hexValue).send()
            ethSendTransaction
        }

    }

    suspend fun getLastTransactionNonce(
        web3j: Web3j,
        walletAddress: String?
    ): BigInteger {
        return withContext(Dispatchers.IO) {
            val ethGetTransactionCount = web3j
                .ethGetTransactionCount(
                    walletAddress,
                    DefaultBlockParameterName.PENDING
                ) // or DefaultBlockParameterName.LATEST
                .send()
            ethGetTransactionCount.transactionCount
        }
    }

    fun createTokenTransferData(
        to: String?,
        tokenAmount: BigInteger?
    ): String {
        val params =
            Arrays.asList<Type<*>>(
                Address(to), Uint256(tokenAmount)
            )
        val returnTypes =
            Arrays.asList<TypeReference<*>>(object :
                TypeReference<Bool?>() {})
        val function =
            Function("transfer", params, returnTypes)
        return FunctionEncoder.encode(function) ?: ""
//        return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
    }


    @Throws(Exception::class)
    private fun getERC20Balance(
        walletAddress: String,
        contractAddress: String,
        decimal: Int
    ): BigDecimal {
        var bigDecimal: BigDecimal? = null
        try {
            val function: Function =
                balanceOf(walletAddress)
            val responseValue: String =
                callSmartContractFunction(function, contractAddress, walletAddress)
            val response =
                FunctionReturnDecoder.decode(
                    responseValue, function.outputParameters
                )
            if (response.size == 1) {
                bigDecimal = BigDecimal((response[0] as Uint256).value)
            }
        } catch (e: Exception) {
            Log.d("TOKEN", "Err" + e.message)
        }
        if (bigDecimal == null || bigDecimal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        } else {
            val decimalDivisor =
                BigDecimal(10.0.pow(decimal))
            val ethBalance: BigDecimal = bigDecimal.divide(decimalDivisor)
            if (decimal > 4) {
                ethBalance //.setScale(4, RoundingMode.CEILING)
                    .toPlainString()
            } else {
                ethBalance //.setScale(type.decimal, RoundingMode.CEILING)
                    .toPlainString()
            }
            return ethBalance
        }
    }

    fun estimateGasFee(): BigDecimal {
        val gasPrice = Convert.toWei(BigDecimal( BaseConfig.bscFastGasPrice), Convert.Unit.GWEI).toBigInteger()
        val gasLimit = BigInteger.valueOf(BaseConfig.bscGasLimit.toLong())
        return BigDecimal(( gasPrice * gasLimit).toString()).divide(BigDecimal(10.0.pow(BNBDecimal).toString()))
    }

    private fun balanceOf(owner: String): Function {
        return Function(
            "balanceOf",
            listOf(Address(owner)),
            listOf(object : TypeReference<Uint256?>() {})
        )
    }


    @Throws(java.lang.Exception::class)
    private fun callSmartContractFunction(
        function: Function,
        contractAddress: String,
        walletAddress: String
    ): String {
        val encodedFunction = FunctionEncoder.encode(function)
        val response = web3j.ethCall(
            Transaction.createEthCallTransaction(
                walletAddress,
                contractAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        )
            .sendAsync().get()
        return response.value ?: ""
    }

    companion object {

        private val instance: RKRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            RKRepository()
        }

        @JvmStatic
        fun get(): RKRepository {
            return instance
        }
    }
}