package onekey.rekallutils.ui.home

import android.text.TextUtils
import android.util.Log
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import onekey.rekallutils.BuildConfig
import onekey.rekallutils.R
import onekey.rekallutils.base.BaseConfig
import onekey.rekallutils.base.BaseConfig.Companion.EKADecimal
import onekey.rekallutils.base.BaseConfig.Companion.chainId
import onekey.rekallutils.database.WalletDBUtils
import onekey.rekallutils.database.nft.UserNFTItem
import onekey.rekallutils.repository.RKRepository
import onekey.rekallutils.repository.struct.NFTStakeItemStruct
import onekey.rekallutils.utils.Gsons
import onekey.rekallutils.utils.ToastHelper
import onekey.rekallutils.utils.wallet.ETHWalletUtils
import onekey.rekallutils.constant.FACTORY_ADDRESS
import onekey.rekallutils.constant.NFTPOOL_ADDRESS
import onekey.rekallutils.constant.bsc_scan_staking_list_1
import onekey.rekallutils.constant.bsc_scan_staking_list_2
import org.litepal.LitePal
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import rxhttp.*
import rxhttp.wrapper.param.RxHttp
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.pow

enum class TranstactionStatus {
    FAIL, SUC, UNKONWN
}

class HomeModel {

    private val getStakeListWithUser = "getStakeListWithUser"
    private val tokenURI = "tokenURI"
    private val allNFTsLength = "allNFTsLength"
    private val allNFTs = "allNFTs"
    private val tokenOfOwnerByIndex = "tokenOfOwnerByIndex"
    private val balanceOf = "balanceOf"
    private val stake = "stake"
    private val approve = "approve"
    private val isApprovedForAll = "isApprovedForAll"
    private val setApprovalForAll = "setApprovalForAll"
    private val getNftProfit = "getNftProfit"
    private val getNftPower = "getNftPower"
    private val getPower = "getPower"
    private val settlement = "settlement"


    suspend fun getStakeListWithUser(address: String): MutableList<NFTStakeItemStruct> {
        val res1 = RxHttp.get(String.format(bsc_scan_staking_list_1, address.replace("0x", "")))
            .toClass<String>()
            .awaitResult()
            .onFailure {
                ToastHelper.showMessage(R.string.refresh_error)
            }.getOrNull()
        val res2 = RxHttp.get(String.format(bsc_scan_staking_list_2, address.replace("0x", "")))
            .toClass<String>()
            .awaitResult()
            .onFailure {
                ToastHelper.showMessage(R.string.refresh_error)
            }.getOrNull()
        val list = mutableListOf<NFTStakeItemStruct>()
        if(!TextUtils.isEmpty(res1)){
            val resEl = Gsons.fromJson(res1, JsonElement::class.java)
            val result = resEl.asJsonObject["result"]
            if(result != null){
                val asJsonArray = result.asJsonArray
                asJsonArray.forEach {
                    val topics = it.asJsonObject["topics"]
                    if(topics != null){
                        val topicsList = topics.asJsonArray
                        if(topicsList.size()>=4){
                          val tokenId =   TypeDecoder.decodeNumeric(topicsList[3]!!.asString,Uint256::class.java)
                          val nftAddress =   TypeDecoder.decodeAddress(topicsList[2]!!.asString)
                            list.add(NFTStakeItemStruct(nftAddress,tokenId))
                        }
                    }
                }
            }
        }
        if(!TextUtils.isEmpty(res2)){
            Log.i("testtest",res2+"")
            val resEl = Gsons.fromJson(res2, JsonElement::class.java)
            val result = resEl.asJsonObject["result"]
            val message = resEl.asJsonObject["message"]
            if(message.asString!="NOTOK"&&result != null){
                val asJsonArray = result.asJsonArray
                asJsonArray.forEach {
                    val topics = it.asJsonObject["topics"]
                    if(topics != null){
                        val topicsList = topics.asJsonArray
                        if(topicsList.size()>=4){
                          val tokenId =   TypeDecoder.decodeNumeric(topicsList[3]!!.asString,Uint256::class.java)
                          val nftAddress =   TypeDecoder.decodeAddress(topicsList[2]!!.asString)
                            list.add(NFTStakeItemStruct(nftAddress,tokenId))
                        }
                    }
                }
            }
        }
        return list
        /*  // val address = entity.value!!.address
          val inputParameters: MutableList<Address> = mutableListOf()
          inputParameters.add(Address(address))
          val outputParameters: MutableList<TypeReference<*>> = ArrayList()
          val itemParameters: TypeReference<DynamicArray<NFTStakeItemStruct>> =
              object : TypeReference<DynamicArray<NFTStakeItemStruct>>() {}
          outputParameters.add(itemParameters)
          val function = Function(
              getStakeListWithUser,
              inputParameters as List<Address>,
              outputParameters
          )
          val encodedFunction = FunctionEncoder.encode(function)
          val response = RKRepository.get().web3j().ethCall(
              Transaction.createEthCallTransaction(
                  entity.value!!.address address,
                 getNFTPoolAddress(),
                 encodedFunction
             ),
             DefaultBlockParameterName.LATEST
         ).send()
         val someTypes = FunctionReturnDecoder.decode(
             response.value, function.outputParameters
         )
         if (someTypes.size <= 0) {
             return mutableListOf()
         } else {
             val dynamicArray = someTypes[0].value as ArrayList<NFTStakeItemStruct>
             return dynamicArray.toMutableList()
         }*/
    }


    fun tokenURI(fromAddress: String, nftAddress: String, tokenId: BigInteger): String {
        // val address = entity.value!!.address
        val inputParameters: MutableList<Uint256> = mutableListOf()
        inputParameters.add(Uint256(tokenId))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Utf8String> = object : TypeReference<Utf8String>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            tokenURI,
            inputParameters as List<Uint256>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = RKRepository.get().web3j().ethCall(
            Transaction.createEthCallTransaction(
                /*entity.value!!.address*/ fromAddress,
                nftAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        if (someTypes.size <= 0) {
            return ""
        } else {
            val uri = someTypes[0].value
            return uri as String
        }
    }

    fun getFactoryAddress(): String {
        return if (BuildConfig.DEBUG) {
            //TEST_FACTORY_ADDRESS
            FACTORY_ADDRESS
        } else {
            FACTORY_ADDRESS
        }
    }

    fun getNFTPoolAddress(): String {
        return if (BuildConfig.DEBUG) {
            //  TEST_NFTPOOL_ADDRESS
            NFTPOOL_ADDRESS
        } else {
            NFTPOOL_ADDRESS
        }
    }

    fun checkTranstactionStatus(txId: String): TranstactionStatus {
        val receipt = RKRepository.get().web3j().ethGetTransactionReceipt(txId).send().result
        return if (receipt == null) TranstactionStatus.UNKONWN else if (receipt.status == "0x0") TranstactionStatus.FAIL else TranstactionStatus.SUC
    }

    fun allnftIndex(fromAddress: String): BigDecimal {
        // val address = entity.value!!.address
        val inputParameters: MutableList<Type<*>> = mutableListOf()
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            allNFTsLength,
            inputParameters as List<Type<*>>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = RKRepository.get().web3j().ethCall(
            Transaction.createEthCallTransaction(
                /*entity.value!!.address*/ fromAddress,
                getFactoryAddress(),
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        if (someTypes.size <= 0) {
            return BigDecimal.ZERO
        } else {
            val uri = someTypes[0].value
            return BigDecimal(uri.toString())
        }
    }

    fun allnft(index: BigInteger, fromAddress: String): String {
        // val address = entity.value!!.address
        val inputParameters: MutableList<Uint256> = mutableListOf()
        inputParameters.add(Uint256(index))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Address> = object : TypeReference<Address>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            allNFTs,
            inputParameters as List<Uint256>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = RKRepository.get().web3j().ethCall(
            Transaction.createEthCallTransaction(
                /*entity.value!!.address*/ fromAddress,
                getFactoryAddress(),
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        if (someTypes.size <= 0) {
            return ""
        } else {
            val uri = someTypes[0].value
            return uri.toString()
        }
    }

    fun balanceOf(owner: String, nftAddress: String): BigInteger {
        // val address = entity.value!!.address
        val inputParameters: MutableList<Address> = mutableListOf()
        inputParameters.add(Address(owner))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            balanceOf,
            inputParameters as List<Address>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val transaction = Transaction(
            owner,
            null as BigInteger?,
            null as BigInteger?,
            null as BigInteger?,
            nftAddress,
            null as BigInteger?,
            encodedFunction, 56L, null as BigInteger?, null as BigInteger?
        )

        val response = RKRepository.get().web3j().ethCall(
            /*Transaction.createEthCallTransaction(
                *//*entity.value!!.address*//* owner,
                nftAddress ,
                encodedFunction
            ),*/
            transaction,
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        return if (someTypes.size <= 0) {
            BigInteger.ZERO
        } else {
            val uri = someTypes[0].value
            BigInteger(uri.toString())
        }
    }

    fun tokenOfOwnerByIndex(index: BigInteger, owner: String, nftAddress: String): BigInteger {
        // val address = entity.value!!.address
        val inputParameters: MutableList<Type<*>> = mutableListOf()
        inputParameters.add(Address(owner))
        inputParameters.add(Uint256(index))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            tokenOfOwnerByIndex,
            inputParameters as List<Type<*>>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = RKRepository.get().web3j().ethCall(
            Transaction.createEthCallTransaction(
                /*entity.value!!.address*/ owner,
                nftAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        if (someTypes.size <= 0) {
            return BigInteger.ZERO
        } else {
            val uri = someTypes[0].value
            return BigInteger(uri.toString())
        }
    }

    private fun getDividenTime(): Int {

        var dividingTime = 1655107200
        val current = System.currentTimeMillis() / 1000
        val Day = 86400
        while (dividingTime < current) {
            dividingTime += Day
        }
        return dividingTime
    }

    suspend fun getNftProfit(owner: String, nftAddress: String, tokenId: String): BigDecimal {
        return withContext(Dispatchers.IO) {
            try {
                // val address = entity.value!!.address
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Address(nftAddress))
                inputParameters.add(Uint256(BigInteger(tokenId)))
                inputParameters.add(Uint256(BigInteger.valueOf(getDividenTime().toLong())))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
                outputParameters.add(itemParameters)
                val function = Function(
                    getNftProfit,
                    inputParameters as List<Type<*>>,
                    outputParameters
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val response = RKRepository.get().web3j().ethCall(
                    Transaction.createEthCallTransaction(
                        /*entity.value!!.address*/ owner,
                        getNFTPoolAddress(),
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()
                val someTypes = FunctionReturnDecoder.decode(
                    response.value, function.outputParameters
                )
                if (someTypes.size <= 0) {
                    return@withContext BigDecimal.ZERO
                } else {
                    val uri = someTypes[0].value
                    return@withContext BigDecimal(uri.toString()).divide(
                        BigDecimal(
                            10.0.pow(
                                EKADecimal
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                return@withContext BigDecimal.ZERO
            }
        }
    }


    suspend fun getNftPower(owner: String, nftAddress: String, tokenId: String): BigDecimal {
        return withContext(Dispatchers.IO) {
            try {
                // val address = entity.value!!.address
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Address(nftAddress))
                inputParameters.add(Uint256(BigInteger(tokenId)))
                inputParameters.add(Uint256(BigInteger.valueOf(getDividenTime().toLong())))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
                outputParameters.add(itemParameters)
                val function = Function(
                    getNftPower,
                    inputParameters as List<Type<*>>,
                    outputParameters
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val response = RKRepository.get().web3j().ethCall(
                    Transaction.createEthCallTransaction(
                        /*entity.value!!.address*/ owner,
                        getNFTPoolAddress(),
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()
                val someTypes = FunctionReturnDecoder.decode(
                    response.value, function.outputParameters
                )
                if (someTypes.size <= 0) {
                    return@withContext BigDecimal.ZERO
                } else {
                    val uri = someTypes[0].value
                    Log.i("testtest", uri.toString())
                    return@withContext BigDecimal(uri.toString())
                }
            } catch (e: Exception) {
                return@withContext BigDecimal.ZERO
            }
        }
    }


    suspend fun getPower(owner: String, nftAddress: String, tokenId: String): BigDecimal {
        return withContext(Dispatchers.IO) {
            try {
                // val address = entity.value!!.address
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Uint256(BigInteger(tokenId)))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val itemParameters: TypeReference<Uint256> = object : TypeReference<Uint256>() {}
                outputParameters.add(itemParameters)
                val function = Function(
                    getPower,
                    inputParameters as List<Type<*>>,
                    outputParameters
                )
                val encodedFunction = FunctionEncoder.encode(function)
                val response = RKRepository.get().web3j().ethCall(
                    Transaction.createEthCallTransaction(
                        /*entity.value!!.address*/ owner,
                        nftAddress,
                        encodedFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()
                val someTypes = FunctionReturnDecoder.decode(
                    response.value, function.outputParameters
                )
                if (someTypes.size <= 0) {
                    return@withContext BigDecimal.ZERO
                } else {
                    val uri = someTypes[0].value
                    Log.i("testtest", uri.toString())
                    return@withContext BigDecimal(uri.toString())
                }
            } catch (e: Exception) {
                return@withContext BigDecimal.ZERO
            }
        }
    }


    fun getChainId(): Long {
        return if (BuildConfig.DEBUG)
           // testChainId
            chainId
        else chainId
    }


    fun isApprovedForAll(owner: String, nftAddress: String): Boolean {
        val inputParameters: MutableList<Address> = mutableListOf()
        inputParameters.add(Address(owner))
        inputParameters.add(Address(getNFTPoolAddress()))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Bool> = object : TypeReference<Bool>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            isApprovedForAll,
            inputParameters as List<Uint256>,
            outputParameters
        )
        val encodedFunction = FunctionEncoder.encode(function)
        val response = RKRepository.get().web3j().ethCall(
            Transaction.createEthCallTransaction(
                /*entity.value!!.address*/ owner,
                nftAddress,
                encodedFunction
            ),
            DefaultBlockParameterName.LATEST
        ).sendAsync().get()
        val someTypes = FunctionReturnDecoder.decode(
            response.value, function.outputParameters
        )
        if (someTypes.size <= 0) {
            return false
        } else {
            val uri = someTypes[0].value
            return uri as Boolean
        }
    }


    fun setApprovalForAll(owner: String, pwd: String, nftAddress: String): String {
        val inputParameters: MutableList<Type<*>> = mutableListOf()
        inputParameters.add(Address(getNFTPoolAddress()))
        inputParameters.add(Bool(true))
        val outputParameters: MutableList<TypeReference<*>> = ArrayList()
        val itemParameters: TypeReference<Utf8String> = object : TypeReference<Utf8String>() {}
        outputParameters.add(itemParameters)
        val function = Function(
            setApprovalForAll,
            inputParameters as List<Uint256>,
            outputParameters
        )

        val ethGetTransactionCount = RKRepository.get().web3j()
            .ethGetTransactionCount(owner, DefaultBlockParameterName.LATEST).send()
        val nonce = ethGetTransactionCount.transactionCount
        val gasPrice =
            Convert.toWei(BigDecimal(BaseConfig.bscFastGasPrice), Convert.Unit.GWEI).toBigInteger()
        val gasLimit = BigInteger.valueOf(BaseConfig.bscGasLimit.toLong())
        val data = FunctionEncoder.encode(function)
        val rawTransaction =
            RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                nftAddress,
                BigInteger.valueOf(0),
                data
            )
        val account = WalletDBUtils.queryAccountWallet(owner)
        val privateKey = ETHWalletUtils.derivePrivateKey(account.id, pwd)
        val credentials = Credentials.create(privateKey)
        val signMessage = TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials)
        val toHexString = Numeric.toHexString(signMessage)
        val response = RKRepository.get().web3j().ethSendRawTransaction(
            toHexString
        ).sendAsync().get()
        return response.transactionHash ?: ""
    }

    suspend fun approve(owner: String, pwd: String, nftAddress: String, tokenId: String): String {
        return withContext(Dispatchers.IO) {
            // val address = entity.value!!.address
            try {
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Address(getNFTPoolAddress()))
                inputParameters.add(Uint256(BigInteger(tokenId)))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val function = Function(
                    approve,
                    inputParameters as List<Address>,
                    outputParameters
                )
                val ethGetTransactionCount = RKRepository.get().web3j()
                    .ethGetTransactionCount(owner, DefaultBlockParameterName.LATEST).send()
                val nonce = ethGetTransactionCount.transactionCount
                val gasPrice =
                    Convert.toWei(BigDecimal(BaseConfig.bscFastGasPrice), Convert.Unit.GWEI)
                        .toBigInteger()
                val gasLimit = BigInteger.valueOf(BaseConfig.bscGasLimit.toLong())
                val data = FunctionEncoder.encode(function)
                val rawTransaction =
                    RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        nftAddress,
                        BigInteger.valueOf(0),
                        data
                    )
                val account = WalletDBUtils.queryAccountWallet(owner)
                val privateKey = ETHWalletUtils.derivePrivateKey(account.id, pwd)
                val credentials = Credentials.create(privateKey)
                val signMessage =
                    TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials)
                val toHexString = Numeric.toHexString(signMessage)
                val response = RKRepository.get().web3j().ethSendRawTransaction(
                    toHexString
                ).send()
                if (response.error != null && response.error.message == "already known") {
                    return@withContext "already known"
                }
                return@withContext response.transactionHash ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext ""
            }
        }
    }


    suspend fun stake(
        pwd: String,
        owner: String,
        nftAddress: String,
        nftIndex: String,
        tokenId: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // val address = entity.value!!.address
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Uint256(BigInteger(nftIndex)))
                inputParameters.add(Uint256(BigInteger(tokenId)))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val function = Function(
                    stake,
                    inputParameters as List<Type<*>>,
                    outputParameters
                )
                val ethGetTransactionCount = RKRepository.get().web3j()
                    .ethGetTransactionCount(owner, DefaultBlockParameterName.LATEST).send()
                val nonce = ethGetTransactionCount.transactionCount
                val gasPrice =
                    Convert.toWei(BigDecimal(BaseConfig.bscFastGasPrice), Convert.Unit.GWEI)
                        .toBigInteger()
                val gasLimit = BigInteger.valueOf(BaseConfig.bscGasLimit.toLong())
                val data = FunctionEncoder.encode(function)
                val rawTransaction =
                    RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        getNFTPoolAddress(),
                        BigInteger.valueOf(0),
                        data
                    )
                val account = WalletDBUtils.queryAccountWallet(owner)
                val privateKey = ETHWalletUtils.derivePrivateKey(account.id, pwd)
                val credentials = Credentials.create(privateKey)
                val signMessage =
                    TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials)
                val toHexString = Numeric.toHexString(signMessage)


                val response = RKRepository.get().web3j().ethSendRawTransaction(
                    toHexString
                ).send()
                return@withContext response.transactionHash ?: ""
            } catch (e: Exception) {
                return@withContext ""
            }
        }
    }

    suspend fun settle(
        pwd: String,
        owner: String,
        nftIndex: String,
        tokenId: String
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                // val address = entity.value!!.address
                val inputParameters: MutableList<Type<*>> = mutableListOf()
                inputParameters.add(Uint256(BigInteger(nftIndex)))
                inputParameters.add(Uint256(BigInteger(tokenId)))
                val outputParameters: MutableList<TypeReference<*>> = ArrayList()
                val function = Function(
                    settlement,
                    inputParameters as List<Type<*>>,
                    outputParameters
                )
                val ethGetTransactionCount = RKRepository.get().web3j()
                    .ethGetTransactionCount(owner, DefaultBlockParameterName.LATEST).send()
                val nonce = ethGetTransactionCount.transactionCount
                val gasPrice =
                    Convert.toWei(BigDecimal(BaseConfig.bscFastGasPrice), Convert.Unit.GWEI)
                        .toBigInteger()
                val gasLimit = BigInteger.valueOf(BaseConfig.bscGasLimit.toLong())
                val data = FunctionEncoder.encode(function)
                val rawTransaction =
                    RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        getNFTPoolAddress(),
                        BigInteger.valueOf(0),
                        data
                    )
                val account = WalletDBUtils.queryAccountWallet(owner)
                val privateKey = ETHWalletUtils.derivePrivateKey(account.id, pwd)
                val credentials = Credentials.create(privateKey)
                val signMessage =
                    TransactionEncoder.signMessage(rawTransaction, getChainId(), credentials)
                val toHexString = Numeric.toHexString(signMessage)


                val response = RKRepository.get().web3j().ethSendRawTransaction(
                    toHexString
                ).send()
                return@withContext response.transactionHash ?: ""
            } catch (e: Exception) {
                return@withContext ""
            }
        }
    }


    fun getNftItemMsg(
        url: String,
        owner: String,
        nftAddress: String,
        tokenId: String
    ): List<UserNFTItem> {
        try {
            val items = LitePal.where(
                "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                owner,
                nftAddress,
                tokenId
            ).find(UserNFTItem::class.java)
            val execute = RxHttp.get(url)
                .execute()
            if (execute.isSuccessful) run {
                val response = Gsons.fromJson(
                    execute.body?.string(),
                    JsonElement::class.java
                )
                val asJsonObject = response.asJsonObject
                if (items.isNotEmpty()) {
                    items.forEach { item ->
                        item.contentType = asJsonObject.get("Content-Type")?.asString ?: ""
                        item.name = asJsonObject.get("name")?.asString ?: ""
                        item.description = asJsonObject.get("description")?.asString ?: ""
                        item.collection = asJsonObject.get("collection")?.asString ?: ""
                        item.image = asJsonObject.get("image")?.asString ?: ""
                        item.saveOrUpdate(
                            "ownerAddress = ? and nftAddress = ? and tokenId = ?",
                            item.ownerAddress, item.nftAddress, item.tokenId.toString()
                        )
                    }
                }
                return items
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
        return emptyList()
    }

}