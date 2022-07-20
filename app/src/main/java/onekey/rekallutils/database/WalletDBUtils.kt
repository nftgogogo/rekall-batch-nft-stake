package net.rekall.database

import org.litepal.LitePal
import java.math.BigDecimal

class WalletDBUtils {
    companion object{
        var accountWalletEntity:AccountWalletEntity?=null

        fun walletNameChecking(walletName:String):Boolean{
            return LitePal.where("name = ?",walletName).findFirst(AccountWalletEntity::class.java) != null
        }

        fun queryAccountWallet(address:String):AccountWalletEntity{
            return LitePal.where("address = ?",address).findFirst(AccountWalletEntity::class.java)
        }

        fun queryAccountWallet(walletId:Long): AccountWalletEntity {
            return  LitePal.find(AccountWalletEntity::class.java,walletId)
        }

        fun saveAccountWallet(entity: AccountWalletEntity){
            entity.save()
        }

        fun loginAccount(walletId:Long){
            val wallet = queryAccountWallet(walletId)
            wallet.accountState = AccountState.LOGIN.state
            wallet.save()
        }

        fun currentAccount(): AccountWalletEntity? {
            if(accountWalletEntity != null){
                return accountWalletEntity
            }else{
                return  LitePal.where("accountState =  ?","${AccountState.LOGIN.state}").findFirst(AccountWalletEntity::class.java)
            }
        }

        fun setAccountBNBBalance(address: String,balance:BigDecimal) {
            val account =
                LitePal.where("address =  ?", address).findFirst(AccountWalletEntity::class.java)
            account.BNBBalance = balance.toDouble()
            account.save()
        }

        fun setAccountBalance(address: String,balance:BigDecimal,ekaBalance:BigDecimal) {
            val account =
                LitePal.where("address =  ?", address).findFirst(AccountWalletEntity::class.java)
            account.BNBBalance = balance.toDouble()
            account.ekaBalance = ekaBalance.toDouble()
            account.save()
        }

        fun deleteAccountWallet(walletId:Long){
            LitePal.delete(AccountWalletEntity::class.java,walletId)
        }

        fun getAll(): MutableList<AccountWalletEntity>? {
            return LitePal.findAll(AccountWalletEntity::class.java)
        }
    }
}