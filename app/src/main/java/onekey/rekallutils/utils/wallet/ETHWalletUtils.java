package net.rekall.utils.wallet;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;


import net.rekall.database.AccountWalletEntity;
import net.rekall.database.WalletDBUtils;
import net.rekall.utils.AppFilePath;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ETHWalletUtils {

    public static final String TAG = "ETHWalletUtils";

    private static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();


    public static String ETH_JAXX_TYPE = "m/44'/60'/0'/0/0";
    public static String ETH_LEDGER_TYPE = "m/44'/60'/0'/0";
    public static String ETH_CUSTOM_TYPE = "m/44'/60'/1'/0/0";


    public static AccountWalletEntity generateMnemonic(String walletName, String pwd) {
        String[] pathArray = ETH_JAXX_TYPE.split("/");
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;

        DeterministicSeed ds = new DeterministicSeed(secureRandom, 128, passphrase, creationTimeSeconds);
        while (true) {
            List<String> mnemonic = ds.getMnemonicCode();
            HashSet hashSet = new HashSet(mnemonic); //去除重复
            if (hashSet.size() != mnemonic.size()) {
                ds = new DeterministicSeed(secureRandom, 128, passphrase, System.currentTimeMillis() / 1000);
            } else {
                break;
            }
            Log.d(TAG, "mnemonic: " + mnemonic);
        }
        return generateWalletByMnemonic(TextUtils.isEmpty(walletName) ? generateNewWalletName() : walletName, ds, pathArray, pwd);
    }


    public static AccountWalletEntity importMnemonic(String path, List<String> list, String name, String pwd) {
        if (!path.startsWith("m") && !path.startsWith("M")) {
            return null;
        }
        String[] pathArray = path.split("/");
        if (pathArray.length <= 1) {
            return null;
        }
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);
        return generateWalletByMnemonic(TextUtils.isEmpty(name) ? generateNewWalletName() : name, ds, pathArray, pwd);
    }

    @NonNull
    private static String generateNewWalletName() {
        char letter1 = (char) (int) (Math.random() * 26 + 97);
        char letter2 = (char) (int) (Math.random() * 26 + 97);

        String walletName = letter1 + String.valueOf(letter2) + "-新钱包";
        while (WalletDBUtils.Companion.walletNameChecking(walletName)) {
            letter1 = (char) (int) (Math.random() * 26 + 97);
            letter2 = (char) (int) (Math.random() * 26 + 97);
            walletName = letter1 + String.valueOf(letter2) + "-新钱包";
        }
        return walletName;
    }


    @Nullable
    public static AccountWalletEntity generateWalletByMnemonic(String walletName, DeterministicSeed ds,
                                                               String[] pathArray, String pwd) {
        //种子
        byte[] seedBytes = ds.getSeedBytes();
//        System.out.println(Arrays.toString(seedBytes));
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        if (seedBytes == null)
            return null;

        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);

        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }
        List<String> mnemonicEncrepty = new ArrayList<>();
        for (int i = 0; i < mnemonic.size(); i++) {
            mnemonicEncrepty.add(AESUtils.INSTANCE.encryptString(mnemonic.get(i), pwd));
        }
        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
        AccountWalletEntity AccountWalletEntity = generateWallet(walletName, pwd, keyPair);
        if (AccountWalletEntity != null) {
            AccountWalletEntity.setMnemonicArrayJson(mnemonicEncrepty);
        }
        return AccountWalletEntity;
    }

    @Nullable
    private static AccountWalletEntity generateWallet(String walletName, String pwd, ECKeyPair ecKeyPair) {
        WalletFile keyStoreFile;
        try {
            keyStoreFile = Wallet.create(Md5Utils.md5(Md5Utils.md5(pwd)), ecKeyPair, 1024, 1); // WalletUtils. .generateNewWalletFile();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String wallet_dir = AppFilePath.Wallet_DIR;
        String keystorePath = "keystore_" + walletName + ".json";
        File destination = new File(wallet_dir, keystorePath);

        //目录不存在则创建目录，创建不了则报错
        if (!createParentDir(destination)) {
            return null;
        }
        try {
            objectMapper.writeValue(destination, keyStoreFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        AccountWalletEntity AccountWalletEntity = new AccountWalletEntity();
        AccountWalletEntity.setName(walletName);
        AccountWalletEntity.setAddress(Keys.toChecksumAddress(keyStoreFile.getAddress()));
        AccountWalletEntity.setKeystorePath(destination.getAbsolutePath());
        AccountWalletEntity.setPassword(Md5Utils.md5(pwd));
        return AccountWalletEntity;
    }


    public static AccountWalletEntity loadWalletByKeystore(String keystore, String pwd) {
        Credentials credentials = null;
        try {
            WalletFile walletFile = null;
            walletFile = objectMapper.readValue(keystore, WalletFile.class);

//            WalletFile walletFile = new Gson().fromJson(keystore, WalletFile.class);
            credentials = Credentials.create(Wallet.decrypt(Md5Utils.md5(Md5Utils.md5(pwd)), walletFile));
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } catch (CipherException e) {
            Log.e(TAG, e.toString());
//            ToastUtils.showToast(R.string.load_wallet_by_official_wallet_keystore_input_tip);
            e.printStackTrace();
        }
        if (credentials != null) {
            return generateWallet(generateNewWalletName(), pwd, credentials.getEcKeyPair());
        }
        return null;
    }

    /**
     * 通过明文私钥导入钱包
     *
     * @param privateKey
     * @param pwd
     * @return
     */
    public static AccountWalletEntity loadWalletByPrivateKey(String privateKey, String walletName, String pwd) {
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        return generateWallet(TextUtils.isEmpty(walletName) ? generateNewWalletName() : walletName, pwd, ecKeyPair);
    }


    public static boolean isTooSimplePrivateKey(String privateKey) {

        if (Numeric.toBigInt(privateKey).intValue() < 100000000) {
            return true;
        } else {
            return true;
        }

    }

    private static boolean createParentDir(File file) {
        //判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            System.out.println("目标文件所在目录不存在，准备创建");
            if (!file.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在目录失败！");
                return false;
            }
        }
        return true;
    }

    /**
     * 修改钱包密码
     *
     * @param walletId
     * @param walletName
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public static AccountWalletEntity modifyPassword(long walletId, String walletName, String oldPassword, String newPassword) {
        AccountWalletEntity AccountWalletEntity = WalletDBUtils.Companion.queryAccountWallet(walletId);
        Credentials credentials = null;
        ECKeyPair keypair = null;
        try {
            credentials = WalletUtils.loadCredentials(Md5Utils.md5(Md5Utils.md5(oldPassword)), AccountWalletEntity.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            File destinationDirectory = new File(AppFilePath.Wallet_DIR, "keystore_" + walletName + ".json");
            WalletUtils.generateWalletFile(Md5Utils.md5(Md5Utils.md5(newPassword)), keypair, destinationDirectory, true);
            AccountWalletEntity.setPassword(Md5Utils.md5(newPassword));
            AccountWalletEntity.setKeystorePath(destinationDirectory.getAbsolutePath());
            WalletDBUtils.Companion.saveAccountWallet(AccountWalletEntity);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return AccountWalletEntity;
    }

    /**
     * 导出明文私钥
     *
     * @param walletId 钱包Id
     * @param Md5Pwd   钱包密码
     * @return
     */
    public static String derivePrivateKeyMd5Pwd(long walletId, String Md5Pwd) {
        AccountWalletEntity AccountWalletEntity = WalletDBUtils.Companion.queryAccountWallet(walletId);
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = WalletUtils.loadCredentials(Md5Utils.md5(Md5Pwd), AccountWalletEntity.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 导出明文私钥
     *
     * @param accountWalletEntity 账户信息
     * @param Md5Pwd              钱包密码
     * @return
     */
    public static String derivePrivateKeyMd5Pwd(AccountWalletEntity accountWalletEntity, String Md5Pwd) {
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = WalletUtils.loadCredentials(Md5Utils.md5(Md5Pwd), accountWalletEntity.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 导出明文私钥
     *
     * @param walletId 钱包Id
     * @param pwd      钱包密码
     * @return
     */
    public static String derivePrivateKey(long walletId, String pwd) {
        AccountWalletEntity AccountWalletEntity = WalletDBUtils.Companion.queryAccountWallet(walletId);
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = WalletUtils.loadCredentials(Md5Utils.md5(Md5Utils.md5(pwd)), AccountWalletEntity.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 导出明文私钥
     *
     * @param accountWalletEntity
     * @param pwd                 钱包密码
     * @return
     */
    public static String derivePrivateKey(AccountWalletEntity accountWalletEntity, String pwd) {
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = WalletUtils.loadCredentials(Md5Utils.md5(Md5Utils.md5(pwd)), accountWalletEntity.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 导出keystore文件
     *
     * @param walletId
     * @param pwd
     * @return
     */
    public static String deriveKeystore(long walletId, String pwd) {
        AccountWalletEntity AccountWalletEntity = WalletDBUtils.Companion.queryAccountWallet(walletId);
        String keystore = null;
        WalletFile walletFile;
        try {
            walletFile = objectMapper.readValue(new File(AccountWalletEntity.getKeystorePath()), WalletFile.class);
            keystore = objectMapper.writeValueAsString(walletFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    /**
     * 删除钱包
     *
     * @param walletId
     * @return
     */
    public static boolean deleteWallet(long walletId) {
        AccountWalletEntity AccountWalletEntity = WalletDBUtils.Companion.queryAccountWallet(walletId);
        if (deleteFile(AccountWalletEntity.getKeystorePath())) {
            WalletDBUtils.Companion.deleteAccountWallet(walletId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
//                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
//                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
//            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

}
