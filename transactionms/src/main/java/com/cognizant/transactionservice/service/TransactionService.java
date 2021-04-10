package com.cognizant.transactionservice.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cognizant.transactionservice.exception.MinimumBalanceException;
import com.cognizant.transactionservice.feign.AccountFeign;
import com.cognizant.transactionservice.feign.RulesFeign;
import com.cognizant.transactionservice.models.Account;
import com.cognizant.transactionservice.models.RulesInput;
import com.cognizant.transactionservice.models.Transaction;
import com.cognizant.transactionservice.repository.TransactionRepository;
import com.cognizant.transactionservice.util.AccountInput;
import com.cognizant.transactionservice.util.TransactionInput;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionService implements TransactionServiceInterface {

	@Autowired
	private AccountFeign accountFeign;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private RulesFeign ruleFeign;

	/*
	 * Method for making a transfer
	 */
	@Override
	public boolean makeTransfer(String token, TransactionInput transactionInput) throws MinimumBalanceException {

		Account sourceAccount = null;
		Account targetAccount = null;

		long sourceAccountNumber = transactionInput.getSourceAccount().getAccountId();
		sourceAccount = accountFeign.getAccount(token, sourceAccountNumber);
		Boolean check = (Boolean) ruleFeign.evaluate(new RulesInput(sourceAccount.getAccountId(),
				sourceAccount.getCurrentBalance(), transactionInput.getAmount())).getBody();
		if (check.booleanValue() == false)
			throw new MinimumBalanceException("Minimum Balance 1000 should be maintained");

		long targetAccountNumber = transactionInput.getTargetAccount().getAccountId();
		targetAccount = accountFeign.getAccount(token, targetAccountNumber);

		if (sourceAccount != null && targetAccount != null) {
			if (isAmountAvailable(transactionInput.getAmount(), sourceAccount.getCurrentBalance())) {

				Transaction sourcetransaction = new Transaction();

				sourcetransaction.setAmount(transactionInput.getAmount());
				sourcetransaction.setSourceAccountId(sourceAccount.getAccountId());
				sourcetransaction.setSourceOwnerName(sourceAccount.getOwnerName());
				sourcetransaction.setTargetAccountId(targetAccount.getAccountId());
				sourcetransaction.setTargetOwnerName(targetAccount.getOwnerName());
				sourcetransaction.setInitiationDate(LocalDateTime.now());
				sourcetransaction.setReference("transfer");
				transactionRepository.save(sourcetransaction);
				return true;
			}
		}
		return false;
	}

	/*
	 * To check whether the amount is available
	 */
	private boolean isAmountAvailable(double amount, double accountBalance) {
		log.info("Method to check whether the amount is available");
		return (accountBalance - amount) > 0;
	}

	/*
	 * Method for making a withdraw
	 */
	@SuppressWarnings("unused")
	@Override
	public boolean makeWithdraw(String token, AccountInput accountInput) {
		log.info("Method to make a Withdraw");
		Account sourceAccount = null;

		long accNumber = accountInput.getAccountId();
		sourceAccount = accountFeign.getAccount(token, accNumber);

		Boolean check = (Boolean) ruleFeign.evaluate(new RulesInput(accountInput.getAccountId(),
				sourceAccount.getCurrentBalance(), accountInput.getAmount())).getBody();
		if (check.booleanValue() == false)
			throw new MinimumBalanceException("Minimum Balance 1000 should be maintained");

		if (sourceAccount != null) {
			Transaction transaction = new Transaction();
			transaction.setSourceAccountId(sourceAccount.getAccountId());
			transaction.setSourceOwnerName(sourceAccount.getOwnerName());
			transaction.setTargetAccountId(sourceAccount.getAccountId());
			transaction.setTargetOwnerName(sourceAccount.getOwnerName());
			transaction.setInitiationDate(LocalDateTime.now());
			transaction.setReference("withdrawn");
			transaction.setAmount(accountInput.getAmount());
			transactionRepository.save(transaction);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean makeServiceCharges(String token, AccountInput accountInput) {
		log.info("Method to make a service charges");
		Account sourceAccount = null;

		long accNumber = accountInput.getAccountId();
		sourceAccount = accountFeign.getAccount(token, accNumber);
		if (sourceAccount != null) {
			Transaction transaction = new Transaction();
			transaction.setSourceAccountId(sourceAccount.getAccountId());
			transaction.setSourceOwnerName(sourceAccount.getOwnerName());
			transaction.setTargetAccountId(sourceAccount.getAccountId());
			transaction.setTargetOwnerName(sourceAccount.getOwnerName());
			transaction.setInitiationDate(LocalDateTime.now());
			transaction.setReference("service charge");
			transaction.setAmount(accountInput.getAmount());
			transactionRepository.save(transaction);
			return true;
		}

		return false;

	}

	/*
	 * Method for making a deposit
	 */
	@Override
	public boolean makeDeposit(String token, AccountInput accountInput) {
		log.info("Method to make a deposit");
		Account sourceAccount = null;

		long accNumber = accountInput.getAccountId();
		sourceAccount = accountFeign.getAccount(token, accNumber);
		if (sourceAccount != null) {
			Transaction transaction = new Transaction();
			transaction.setSourceAccountId(sourceAccount.getAccountId());
			transaction.setSourceOwnerName(sourceAccount.getOwnerName());
			transaction.setTargetAccountId(sourceAccount.getAccountId());
			transaction.setTargetOwnerName(sourceAccount.getOwnerName());
			transaction.setInitiationDate(LocalDateTime.now());
			transaction.setReference("deposit");
			transaction.setAmount(accountInput.getAmount());
			transactionRepository.save(transaction);
			return true;
		}
		return false;
	}
}
