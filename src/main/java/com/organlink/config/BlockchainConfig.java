package com.organlink.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

/**
 * Blockchain configuration for Ethereum integration
 * Configures Web3j client and credentials for Sepolia testnet
 */
@Configuration
public class BlockchainConfig {

    private static final Logger log = LoggerFactory.getLogger(BlockchainConfig.class);

    @Value("${blockchain.ethereum.network-url}")
    private String networkUrl;

    @Value("${blockchain.ethereum.private-key}")
    private String privateKey;

    @Value("${blockchain.ethereum.gas-price}")
    private BigInteger gasPrice;

    @Value("${blockchain.ethereum.gas-limit}")
    private BigInteger gasLimit;

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(networkUrl));
    }

    @Bean
    public Credentials credentials() {
        String key = privateKey != null ? privateKey.trim() : "";
        if (key.startsWith("0x") || key.startsWith("0X")) {
            key = key.substring(2);
        }
        // Validate hex private key (64 hex chars)
        boolean looksHex = key.matches("[0-9a-fA-F]{64}");
        if (!looksHex || key.equalsIgnoreCase("CHANGE_ME")) {
            try {
                log.warn("[OrganLink] ETH_PRIVATE_KEY is not set or invalid. Generating ephemeral dev credentials. " +
                        "Set environment variable ETH_PRIVATE_KEY to a 64-hex private key for real transactions.");
                ECKeyPair pair = Keys.createEcKeyPair();
                Credentials creds = Credentials.create(pair);
                log.warn("[OrganLink] Using ephemeral address {} (no funds). Blockchain writes will fail until a valid key is provided.", creds.getAddress());
                return creds;
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create fallback Ethereum credentials: " + e.getMessage(), e);
            }
        }
        return Credentials.create(key);
    }

    @Bean
    public StaticGasProvider gasProvider() {
        return new StaticGasProvider(gasPrice, gasLimit);
    }

    @Bean
    public DefaultGasProvider defaultGasProvider() {
        return new DefaultGasProvider();
    }
}
