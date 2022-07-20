package onekey.rekallutils.repository.struct;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Arrays;

public class NFTStakeItemStruct  extends StaticStruct   {

    public  String nftAddress = "";
    public  BigInteger tokenId = BigInteger.ZERO;

    public void setNftAddress(String nftAddress){
        this.nftAddress = nftAddress;
    }

    public String getNftAddress(){
        return  nftAddress;
    }

    public void setTokenId(BigInteger tokenId){
        this.tokenId = tokenId;
    }

    public BigInteger getTokenId(){
        return  tokenId;
    }



   public NFTStakeItemStruct(Address nftAddress,Uint256 tokenId){
        super(Arrays.asList(nftAddress,tokenId ));
        this.nftAddress = nftAddress.getValue();
        this.tokenId = tokenId.getValue();
    }
}
