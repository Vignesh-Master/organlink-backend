package com.organlink.blockchain;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.4.
 */
@SuppressWarnings("rawtypes")
public class SignatureVerificationContract extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b50600080546001600160a01b031916331790556107a8806100326000396000f3fe608060405234801561001057600080fd5b50600436106100575760003560e01c80633ba2d1ca1461005c578063715018a6148061008c578063883356d914610096578063ad3cb1cc146100a9578063f2fde38b146100c7575b600080fd5b61006f61006a3660046105c7565b6100da565b6040805193845260208401929092529015159082015260600160405180910390f35b610094610179565b005b6100946100a436600461064c565b61018d565b6100b16101d1565b6040516100be919061071e565b60405180910390f35b6100946100d5366004610756565b6101fd565b60008061010c85858080601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525061024b92505050565b600087815260016020526040902080549192509061012990610771565b90506000146101615760008781526001602052604090206001810154600290910154909250905061016e565b60008060009350935093505b9193909250565b610181610294565b61018b60006102ee565b565b610195610294565b6000828152600160205260409020805461024690849084906101b690610771565b80601f01602080910402602001604051908101604052809392919081815260200183838082843760009201919091525061033e92505050565b505050565b60606000546001600160a01b0316331461018b576040516330cd747160e01b815260040160405180910390fd5b610205610294565b6001600160a01b03811661023b5760405163f91b32f960e01b8152600060048201526024015b60405180910390fd5b610244816102ee565b50565b6001600160a01b03811661029057600080fd5b5050565b6000546001600160a01b0316331461018b5760405163118cdaa760e01b8152336004820152602401610232565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b6000838152600160205260409020825161035e9284019060200190610361565b50505050565b82805461037090610771565b90600052602060002090601f01602090048101928261039257600085556103d8565b82601f106103ab57805160ff19168380011785556103d8565b828001600101855582156103d8579182015b828111156103d85782518255916020019190600101906103bd565b506103e49291506103e8565b5090565b5b808211156103e4576000815560010161039e565b634e487b7160e01b600052604160045260246000fd5b600082601f83011261042457600080fd5b813567ffffffffffffffff8082111561043f5761043f6103fd565b604051601f8301601f19908116603f01168101908282118183101715610467576104676103fd565b8160405283815286602085880101111561048057600080fd5b836020870160208301376000602085830101528094505050505092915050565b600080604083850312156104b357600080fd5b82359150602083013567ffffffffffffffff8111156104d157600080fd5b6104dd85828601610413565b9150509250929050565b60005b838110156105025781810151838201526020016104ea565b838111156105115760008484015b50505050565b600081518084526105298160208601602086016104e7565b601f01601f19169290920160200192915050565b6000815180845260208085019450848260051b860182860160005b8781101561058e578383038a52815180518452885160018060a01b031687850152604080820151858b0152835115156060860152808601518084528087015180860152508060c08601526105ad8186018251610511565b9050601f01601f1916840194506020830192506001810190506105a3565b50909695505050505050565b600080602083850312156105ad57600080fd5b823567ffffffffffffffff808211156105c557600080fd5b818501915085601f8301126105d957600080fd5b8135818111156105e857600080fd5b8660208285010111156105fa57600080fd5b60209290920196919550909350505050565b60008083601f84011261061e57600080fd5b50813567ffffffffffffffff81111561063657600080fd5b60208301915083602082850101111561064e57600080fd5b9250929050565b6000806000806060858703121561066b57600080fd5b8435935060208501359250604085013567ffffffffffffffff81111561069057600080fd5b61069c8782880161060c565b95989497509550505050565b600081518084526106c08160208601602086016104e7565b601f01601f19169290920160200192915050565b6000825b818110156106f0578051821a85529160209190910190600101906106d4565b506020830192509392505050565b6020815260006107116020830184610511565b9392505050565b80356001600160a01b038116811461072f57600080fd5b919050565b60006020828403121561074657600080fd5b61074f82610718565b9392505050565b634e487b7160e01b600052602260045260246000fd5b60006001821061078a5761078a610759565b506001019056fea264697066735822122005dcaec0b2b0ad81a6bac0d8c87e9dd4a6b5c3a49e0b0e3b7d8e5f6c7a9b8c9d64736f6c634300081400";

    public static final String FUNC_GETVERIFICATION = "getVerification";
    public static final String FUNC_OWNER = "owner";
    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";
    public static final String FUNC_STOREVERIFICATION = "storeVerification";
    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event VERIFICATIONSTORED_EVENT = new Event("VerificationStored", 
            Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>(true) {},
                    new TypeReference<Utf8String>(false) {},
                    new TypeReference<Bool>(false) {}
            ));

    @Deprecated
    protected SignatureVerificationContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SignatureVerificationContract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SignatureVerificationContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SignatureVerificationContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<VerificationStoredEventResponse> getVerificationStoredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(VERIFICATIONSTORED_EVENT, transactionReceipt);
        ArrayList<VerificationStoredEventResponse> responses = new ArrayList<VerificationStoredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VerificationStoredEventResponse typedResponse = new VerificationStoredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.recordId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.verified = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }


    public RemoteFunctionCall<Tuple3<BigInteger, BigInteger, Boolean>> getVerification(BigInteger recordId) {
        final Function function = new Function(FUNC_GETVERIFICATION, 
                Arrays.<Type>asList(new Uint256(recordId)), 
                Arrays.<TypeReference<?>>asList(
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Bool>() {}
                ));
        return new RemoteFunctionCall<Tuple3<BigInteger, BigInteger, Boolean>>(function,
                new Callable<Tuple3<BigInteger, BigInteger, Boolean>>() {
                    @Override
                    public Tuple3<BigInteger, BigInteger, Boolean> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, BigInteger, Boolean>(
                                (BigInteger) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (Boolean) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final Function function = new Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> storeVerification(BigInteger recordId, String ipfsHash) {
        final Function function = new Function(
                FUNC_STOREVERIFICATION, 
                Arrays.<Type>asList(new Uint256(recordId), 
                new Utf8String(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final Function function = new Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static SignatureVerificationContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SignatureVerificationContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SignatureVerificationContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SignatureVerificationContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SignatureVerificationContract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SignatureVerificationContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SignatureVerificationContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SignatureVerificationContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SignatureVerificationContract> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SignatureVerificationContract.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SignatureVerificationContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SignatureVerificationContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<SignatureVerificationContract> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SignatureVerificationContract.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SignatureVerificationContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SignatureVerificationContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class VerificationStoredEventResponse extends BaseEventResponse {
        public BigInteger recordId;
        public String ipfsHash;
        public Boolean verified;
    }
}
