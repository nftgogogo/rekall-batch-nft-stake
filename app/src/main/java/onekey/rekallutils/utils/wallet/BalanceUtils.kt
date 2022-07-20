package onekey.rekallutils.utils.wallet

import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode


class BalanceUtils {
    companion object {

        private const val weiInEth = "1000000000000000000"

        @Throws(Exception::class)
        fun EthToWei(eth: String?): String {
            val wei =
                BigDecimal(eth).multiply(BigDecimal(weiInEth))
            return wei.toBigInteger().toString()
        }

        @Throws(Exception::class)
        fun EthToWeiBigInt(eth: String?): BigDecimal {
            val wei =
                BigDecimal(eth).multiply(BigDecimal(weiInEth))
            return wei
        }

        fun tokenToWei(number: BigDecimal, decimals: Int): BigDecimal {
            val weiFactor = BigDecimal.TEN.pow(decimals)
            return number.multiply(weiFactor)
        }

        fun weiToToken(number: BigDecimal, decimals: Int): BigDecimal {
            val weiFactor = BigDecimal.TEN.pow(decimals)
            return number.divide(weiFactor)
        }

        @Throws(java.lang.Exception::class)
        fun weiToEth(wei: BigInteger, sigFig: Int): String {
            val eth: BigDecimal = weiToEth(wei.toBigDecimal())
            val scale = sigFig - eth.precision() + eth.scale()
            val eth_scaled =
                eth.setScale(scale, RoundingMode.HALF_UP)
            return eth_scaled.toString()
        }

        fun weiToEth(wei: BigDecimal): BigDecimal {
            return Convert.fromWei(
                wei,
                Convert.Unit.ETHER
            )
        }

        fun gweiToWei(gwei: BigDecimal?): BigInteger? {
            return Convert.toWei(gwei, Convert.Unit.GWEI)
                .toBigInteger()
        }
    }
}