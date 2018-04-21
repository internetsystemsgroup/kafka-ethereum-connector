/*
    MIT License
    Copyright 2018 Internet Systems Group, Inc.

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
    associated documentation files (the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute,
    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
    is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or
    substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
    BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.internetsystemsgroup.web3j;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class Web3jTest {

    private Web3j web3j;

    @Before
    public void setUp() {
        Properties props = new Properties();

        try {
            props.load(Web3jTest.class.getResourceAsStream("/test.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String endpoint;

        endpoint = props.getProperty("endpoint");

        web3j = Web3j.build(new HttpService(endpoint));
    }

    @Test
    public void getTransactionReturnsCorrectFieldsForNewContract() throws Exception {
        String txHash = "0xcfaa78eaa09097bfa9e54cfe1128ea70910cc1c3715038f7e10fbe52dc960605";

        Transaction tx = web3j.ethGetTransactionByHash(txHash).send().getResult();

        assertThat(tx.getHash(), equalTo(txHash));
        assertThat(tx.getBlockNumber(), equalTo(BigInteger.valueOf(4713145)));
        assertThat(tx.getTransactionIndex(), equalTo(BigInteger.valueOf(71)));
        assertThat(tx.getFrom(), equalTo("0xcb1732e0f7fda99499a7812f85a5b015816c03d7"));
        assertThat(tx.getTo(), equalTo(null)); // The contract address is provided in the transactionReceipt
        assertThat(tx.getValue(), equalTo(BigInteger.valueOf(0L)));
        assertThat(tx.getGas(), equalTo(BigInteger.valueOf(1564609)));
        assertThat(tx.getGasPrice(), equalTo(new BigInteger("10000000000")));  //represents 0.00000001 Ether or (10 Gwei)
        // The actual cost/fee is provided in the transactionReceipt

        // Verify binary bytecode.  See getContractMetedataSucceedsForVerifiedContract() below.
        assertThat(tx.getInput(), equalTo(sampleCode()));

        assertThat(tx.getRaw(), equalTo(null));
        assertThat(tx.getCreates(), equalTo(null));

        // Raw values
        assertThat(tx.getBlockNumberRaw(), equalTo("0x47eab9"));
        assertThat(tx.getTransactionIndexRaw(), equalTo("0x47"));

    }

    @Test
    public void getTransactionReceiptReturnsCorrectFieldsForNewContract() throws Exception {
        String txHash = "0xcfaa78eaa09097bfa9e54cfe1128ea70910cc1c3715038f7e10fbe52dc960605";

        TransactionReceipt txr = web3j.ethGetTransactionReceipt(txHash).send().getResult();

        assertThat(txr.getTransactionHash(), equalTo(txHash));
        assertThat(txr.getContractAddress(), equalTo("0x85e076361cc813a908ff672f9bad1541474402b2"));
        assertThat(txr.getGasUsed(), equalTo(BigInteger.valueOf(1303841L)));

        String code = web3j.ethGetCode(txHash, DefaultBlockParameter.valueOf("latest")).send().getCode();
        assertThat(code, equalTo(null));
    }

    /**
     * The application binary interface (ABI) is available from etherscan.io for the contract using
     * this endpoint:
     *  https://api.etherscan.io/api?module=contract&action=getabi&address=0x85e076361cc813a908ff672f9bad1541474402b2
     * The contract family (i.e. ERC) could be inferred from the ABI, but not the name.
     */
    @Ignore
    public void getABIReturnsCorrectDataForVerifiedContract() {}

    /**
     *  The code for the contract is available in binary from the blockchain.  To determine the
     *  contract and token type, we must analyze the code or use a web service that has this
     *  information.  Currently, etherscan.io appears to be the best source of verified source
     *  code for contracts.  But, it does not appear to have a JSON API to retrieve the code.
     *  The verified source code for this contract is available here:
     *   https://etherscan.io/address/0x85e076361cc813a908ff672f9bad1541474402b2#code
     *
     *  The source code may be available on the swarm network, but as of this writing, the
     *  URI provided by etherscan.io was not available on the following swarm endpoint:
     *   http://swarm-gateways.net/bzzr://29e02f44486515e954da7d6a7fba6f89e35bad050ab096a8876849129f89c11a
     *
     *  Etherscan.io also provides a list of all verified contracts here:
     *   https://etherscan.io/contractsVerified
     *
     *  There is also an EVM bytecode decompiler available here:
     *   https://github.com/comaeio/porosity
     */
    @Ignore
    public void getContractMetedataSucceedsForVerifiedContract() {}

    @Test
    public void getTransactionReturnsCorrectFieldsForSendToContract() throws Exception {
        String txHash = "0xd278abb69579e462b4f1641e93c50d8dcd54377310e17f8e2cd83dc576fd77e1";

        Transaction tx = web3j.ethGetTransactionByHash(txHash).send().getResult();

        assertThat(tx.getHash(), equalTo(txHash));
        assertThat(tx.getBlockNumber(), equalTo(BigInteger.valueOf(5470634)));
        assertThat(tx.getTransactionIndex(), equalTo(BigInteger.valueOf(110)));
        assertThat(tx.getFrom(), equalTo("0x68878074df2914e86bac6d03a48dd5ade15c86ce"));
        assertThat(tx.getTo(), equalTo("0x85e076361cc813a908ff672f9bad1541474402b2")); //null
        assertThat(tx.getValue(), equalTo(BigInteger.valueOf(0L)));
        assertThat(tx.getGas(), equalTo(BigInteger.valueOf(37647)));
        assertThat(tx.getGasPrice(), equalTo(new BigInteger("2000000000")));  //represents 0.00000002 Ether or (2 Gwei)

        // Function: transfer(address _to, uint256 _value)
        final String expectedInput =
                "0xa9059cbb" +                                                       //MethodID
                        "00000000000000000000000081edfb689faf522d78ca23c2e2a9b351d4a8c150" + //[0]
                        "00000000000000000000000000000000000000000000000000000000001bcdc9";  //[1]

        assertThat(tx.getInput(), equalTo(expectedInput));
        assertThat(tx.getRaw(), equalTo(null));
        assertThat(tx.getCreates(), equalTo(null));
    }

    /**
     *
     * @return Binary EVM bytecode for the contract at address 0x85e076361cc813a908ff672f9bad1541474402b2
     */
    private String sampleCode() {
        return
            "0x6060604052341561000f57600080fd5b6040516020806112bf833981016040" +
            "52808051906020019091905050600260ff16600a0a64174876e8000260008083" +
            "73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffff" +
            "ffffffffffffffffffffff168152602001908152602001600020819055508073" +
            "ffffffffffffffffffffffffffffffffffffffff1660007fddf252ad1be2c89b" +
            "69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef600260ff16600a0a" +
            "64174876e800026040518082815260200191505060405180910390a3506111d5" +
            "806100ea6000396000f3006060604052600436106100af576000357c01000000" +
            "00000000000000000000000000000000000000000000000000900463ffffffff" +
            "16806306fdde03146100b4578063095ea7b31461014257806318160ddd146101" +
            "9c57806323b872dd146101c5578063313ce5671461023e578063661884631461" +
            "026d57806370a08231146102c757806395d89b4114610314578063a9059cbb14" +
            "6103a2578063d73dd623146103fc578063dd62ed3e14610456575b600080fd5b" +
            "34156100bf57600080fd5b6100c76104c2565b60405180806020018281038252" +
            "83818151815260200191508051906020019080838360005b8381101561010757" +
            "80820151818401526020810190506100ec565b50505050905090810190601f16" +
            "80156101345780820380516001836020036101000a031916815260200191505b" +
            "509250505060405180910390f35b341561014d57600080fd5b61018260048080" +
            "3573ffffffffffffffffffffffffffffffffffffffff16906020019091908035" +
            "9060200190919050506104fb565b604051808215151515815260200191505060" +
            "405180910390f35b34156101a757600080fd5b6101af6105ed565b6040518082" +
            "815260200191505060405180910390f35b34156101d057600080fd5b61022460" +
            "0480803573ffffffffffffffffffffffffffffffffffffffff16906020019091" +
            "90803573ffffffffffffffffffffffffffffffffffffffff1690602001909190" +
            "80359060200190919050506105ff565b60405180821515151581526020019150" +
            "5060405180910390f35b341561024957600080fd5b6102516109b9565b604051" +
            "808260ff1660ff16815260200191505060405180910390f35b34156102785760" +
            "0080fd5b6102ad600480803573ffffffffffffffffffffffffffffffffffffff" +
            "ff169060200190919080359060200190919050506109be565b60405180821515" +
            "1515815260200191505060405180910390f35b34156102d257600080fd5b6102" +
            "fe600480803573ffffffffffffffffffffffffffffffffffffffff1690602001" +
            "9091905050610c4f565b6040518082815260200191505060405180910390f35b" +
            "341561031f57600080fd5b610327610c97565b60405180806020018281038252" +
            "83818151815260200191508051906020019080838360005b8381101561036757" +
            "808201518184015260208101905061034c565b50505050905090810190601f16" +
            "80156103945780820380516001836020036101000a031916815260200191505b" +
            "509250505060405180910390f35b34156103ad57600080fd5b6103e260048080" +
            "3573ffffffffffffffffffffffffffffffffffffffff16906020019091908035" +
            "906020019091905050610cd0565b604051808215151515815260200191505060" +
            "405180910390f35b341561040757600080fd5b61043c600480803573ffffffff" +
            "ffffffffffffffffffffffffffffffff16906020019091908035906020019091" +
            "905050610eef565b604051808215151515815260200191505060405180910390" +
            "f35b341561046157600080fd5b6104ac600480803573ffffffffffffffffffff" +
            "ffffffffffffffffffff1690602001909190803573ffffffffffffffffffffff" +
            "ffffffffffffffffff169060200190919050506110eb565b6040518082815260" +
            "200191505060405180910390f35b604080519081016040528060078152602001" +
            "7f54656c636f696e000000000000000000000000000000000000000000000000" +
            "0081525081565b600081600160003373ffffffffffffffffffffffffffffffff" +
            "ffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001" +
            "90815260200160002060008573ffffffffffffffffffffffffffffffffffffff" +
            "ff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152" +
            "602001600020819055508273ffffffffffffffffffffffffffffffffffffffff" +
            "163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d" +
            "5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b92584604051808281" +
            "5260200191505060405180910390a36001905092915050565b600260ff16600a" +
            "0a64174876e8000281565b60008073ffffffffffffffffffffffffffffffffff" +
            "ffffff168373ffffffffffffffffffffffffffffffffffffffff161415151561" +
            "063c57600080fd5b6000808573ffffffffffffffffffffffffffffffffffffff" +
            "ff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152" +
            "60200160002054821115151561068957600080fd5b600160008573ffffffffff" +
            "ffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffff" +
            "ffffffffff16815260200190815260200160002060003373ffffffffffffffff" +
            "ffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffff" +
            "ffff16815260200190815260200160002054821115151561071457600080fd5b" +
            "610765826000808773ffffffffffffffffffffffffffffffffffffffff1673ff" +
            "ffffffffffffffffffffffffffffffffffffff16815260200190815260200160" +
            "00205461117290919063ffffffff16565b6000808673ffffffffffffffffffff" +
            "ffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff" +
            "168152602001908152602001600020819055506107f8826000808673ffffffff" +
            "ffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffff" +
            "ffffffffffff1681526020019081526020016000205461118b90919063ffffff" +
            "ff16565b6000808573ffffffffffffffffffffffffffffffffffffffff1673ff" +
            "ffffffffffffffffffffffffffffffffffffff16815260200190815260200160" +
            "0020819055506108c982600160008773ffffffffffffffffffffffffffffffff" +
            "ffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001" +
            "90815260200160002060003373ffffffffffffffffffffffffffffffffffffff" +
            "ff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152" +
            "6020016000205461117290919063ffffffff16565b600160008673ffffffffff" +
            "ffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffff" +
            "ffffffffff16815260200190815260200160002060003373ffffffffffffffff" +
            "ffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffff" +
            "ffff168152602001908152602001600020819055508273ffffffffffffffffff" +
            "ffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffff" +
            "ffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4d" +
            "f523b3ef846040518082815260200191505060405180910390a3600190509392" +
            "505050565b600281565b600080600160003373ffffffffffffffffffffffffff" +
            "ffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152" +
            "60200190815260200160002060008573ffffffffffffffffffffffffffffffff" +
            "ffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001" +
            "90815260200160002054905080831115610acf576000600160003373ffffffff" +
            "ffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffff" +
            "ffffffffffff16815260200190815260200160002060008673ffffffffffffff" +
            "ffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffff" +
            "ffffff16815260200190815260200160002081905550610b63565b610ae28382" +
            "61117290919063ffffffff16565b600160003373ffffffffffffffffffffffff" +
            "ffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681" +
            "5260200190815260200160002060008673ffffffffffffffffffffffffffffff" +
            "ffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020" +
            "01908152602001600020819055505b8373ffffffffffffffffffffffffffffff" +
            "ffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5b" +
            "e1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9256001" +
            "60003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffff" +
            "ffffffffffffffffffffffffffff168152602001908152602001600020600088" +
            "73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffff" +
            "ffffffffffffffffffffff168152602001908152602001600020546040518082" +
            "815260200191505060405180910390a3600191505092915050565b6000806000" +
            "8373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffff" +
            "ffffffffffffffffffffffff1681526020019081526020016000205490509190" +
            "50565b6040805190810160405280600381526020017f54454c00000000000000" +
            "0000000000000000000000000000000000000000000081525081565b60008073" +
            "ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffff" +
            "ffffffffffffffffffffff1614151515610d0d57600080fd5b6000803373ffff" +
            "ffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffff" +
            "ffffffffffffffff168152602001908152602001600020548211151515610d5a" +
            "57600080fd5b610dab826000803373ffffffffffffffffffffffffffffffffff" +
            "ffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190" +
            "81526020016000205461117290919063ffffffff16565b6000803373ffffffff" +
            "ffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffff" +
            "ffffffffffff16815260200190815260200160002081905550610e3e82600080" +
            "8673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffff" +
            "ffffffffffffffffffffffff1681526020019081526020016000205461118b90" +
            "919063ffffffff16565b6000808573ffffffffffffffffffffffffffffffffff" +
            "ffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190" +
            "8152602001600020819055508273ffffffffffffffffffffffffffffffffffff" +
            "ffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1b" +
            "e2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef8460405180" +
            "82815260200191505060405180910390a36001905092915050565b6000610f80" +
            "82600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffff" +
            "ffffffffffffffffffffffffffffffffff168152602001908152602001600020" +
            "60008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffff" +
            "ffffffffffffffffffffffffffff168152602001908152602001600020546111" +
            "8b90919063ffffffff16565b600160003373ffffffffffffffffffffffffffff" +
            "ffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260" +
            "200190815260200160002060008573ffffffffffffffffffffffffffffffffff" +
            "ffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190" +
            "8152602001600020819055508273ffffffffffffffffffffffffffffffffffff" +
            "ffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5eb" +
            "ec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b9256001600033" +
            "73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffff" +
            "ffffffffffffffffffffff16815260200190815260200160002060008773ffff" +
            "ffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffff" +
            "ffffffffffffffff168152602001908152602001600020546040518082815260" +
            "200191505060405180910390a36001905092915050565b6000600160008473ff" +
            "ffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffff" +
            "ffffffffffffffffff16815260200190815260200160002060008373ffffffff" +
            "ffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffff" +
            "ffffffffffff16815260200190815260200160002054905092915050565b6000" +
            "82821115151561118057fe5b818303905092915050565b600080828401905083" +
            "811015151561119f57fe5b80915050929150505600a165627a7a7230582029e0" +
            "2f44486515e954da7d6a7fba6f89e35bad050ab096a8876849129f89c11a0029" +
            "0000000000000000000000008322c7e7c14b57ff85947f28381421692a1cf267";
    }
}
