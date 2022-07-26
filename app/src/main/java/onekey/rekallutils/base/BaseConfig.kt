package onekey.rekallutils.base

import java.math.BigInteger

class BaseConfig {
    companion object{
        val bscFastGasPrice: BigInteger = BigInteger.valueOf(5)
        val bscGasLimit: BigInteger = BigInteger.valueOf(500000)
        val chainId: Long = 56
        val EKADecimal:Int = 18
        val BNBDecimal:Int = 18
        val EKAContractAddress:String  = "0x9533fa068c12d03011ea15dc64d382930f15593d"
    }
}