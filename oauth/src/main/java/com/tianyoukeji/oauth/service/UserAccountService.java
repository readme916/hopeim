package com.tianyoukeji.oauth.service;

import com.utopia.tokensart.common.grpc.AccountGrpc;
import com.utopia.tokensart.common.grpc.AccountOuterClass;
import com.utopia.tokensart.common.modules.base.init.GlobalType;
import com.utopia.tokensart.common.modules.base.models.*;
import com.utopia.tokensart.common.modules.base.repository.AccountRepository;
import com.utopia.tokensart.common.modules.base.repository.AccountTokenRepository;
import com.utopia.tokensart.common.modules.base.repository.AccountTypeRepository;
import com.utopia.tokensart.common.modules.base.repository.TokenRepository;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UserAccountService {

	@GrpcClient("grpcServer")
	private Channel grpcCenter;
	@Autowired
	private CallCredentials grpcCredentials;

	@Autowired
	private AccountTypeRepository accountTypeRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private AccountTokenRepository accountTokenRepository;

	public void generateBasicAccountByOrgOrUser(Org org, User user) {
		if (org == null && user == null) {
			throw new IllegalArgumentException("org 或 user 有一个不能为空");
		}
		AccountGrpc.AccountBlockingStub accountBlockingStub = AccountGrpc.newBlockingStub(grpcCenter)
				.withCallCredentials(grpcCredentials);

		AccountOuterClass.Response response = accountBlockingStub
				.register(AccountOuterClass.BlankRequest.newBuilder().build());

		AccountType accountType = accountTypeRepository.findByCode(GlobalType.AccountTypeCode.GENERALTOKEN);

		Account account = new Account();
		if (org != null) {
			account.setOwnerOrg(org);
		} else {
			account.setOwnerUser(user);
		}
		account.setAccountType(accountType);
		account.setName(accountType.getName());
		account.setState(Account.State.ENABLED);

		account.setEthAddress(response.getEthAddress());
		account.setPlatformUserNo(response.getPlatformUserNo());
		account = accountRepository.saveAndFlush(account);

		Token usdt = tokenRepository.findBySymbolAndIsVerify("USDT",true);
		Token eth = tokenRepository.findBySymbolAndIsVerify("ETH",true);
		Token tks = tokenRepository.findBySymbolAndIsVerify("TKS",true);
		AccountToken usdtAccountToken = new AccountToken();
		usdtAccountToken.setAccount(account);
		usdtAccountToken.setBalance(BigDecimal.ZERO);
		usdtAccountToken.setFreeze(BigDecimal.ZERO);
		if (org != null) {
			usdtAccountToken.setOwnerOrg(org);
		} else {
			usdtAccountToken.setOwnerUser(user);
		}
		usdtAccountToken.setTemporary(BigDecimal.ZERO);
		usdtAccountToken.setToken(usdt);
		accountTokenRepository.saveAndFlush(usdtAccountToken);

		AccountToken ethAccountToken = new AccountToken();
		ethAccountToken.setAccount(account);
		ethAccountToken.setBalance(BigDecimal.ZERO);
		ethAccountToken.setFreeze(BigDecimal.ZERO);
		if (org != null) {
			ethAccountToken.setOwnerOrg(org);
		} else {
			ethAccountToken.setOwnerUser(user);
		}
		ethAccountToken.setTemporary(BigDecimal.ZERO);
		ethAccountToken.setToken(eth);
		accountTokenRepository.saveAndFlush(ethAccountToken);

		AccountToken tksAccountToken = new AccountToken();
		tksAccountToken.setAccount(account);
		tksAccountToken.setBalance(BigDecimal.ZERO);
		tksAccountToken.setFreeze(BigDecimal.ZERO);
		if (org != null) {
			tksAccountToken.setOwnerOrg(org);
		} else {
			tksAccountToken.setOwnerUser(user);
		}
		tksAccountToken.setTemporary(BigDecimal.ZERO);
		tksAccountToken.setToken(tks);
		accountTokenRepository.saveAndFlush(tksAccountToken);
	}
}
