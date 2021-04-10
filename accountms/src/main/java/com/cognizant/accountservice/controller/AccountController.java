package com.cognizant.accountservice.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cognizant.accountservice.exceptionhandling.MinimumBalanceException;
import com.cognizant.accountservice.feignclient.TransactionFeign;
import com.cognizant.accountservice.model.Account;
import com.cognizant.accountservice.model.AccountCreationStatus;
import com.cognizant.accountservice.model.AccountInput;
import com.cognizant.accountservice.model.Transaction;
import com.cognizant.accountservice.model.TransactionInput;
import com.cognizant.accountservice.service.AccountServiceImpl;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class AccountController {

	@Autowired
	private AccountServiceImpl accountServiceImpl; // Account Service Methods Implementation

	@Autowired
	private TransactionFeign transactionFeign; // Transaction Module Implementation With Feign Client

	/*
	 * To get the Account Details
	 * 
	 * Pass the AccountInput Entity along with token
	 */
	@GetMapping("/getAccount/{accountId}")
	public ResponseEntity<Account> getAccount(@RequestHeader("Authorization") String token,
			@PathVariable long accountId) {
		accountServiceImpl.hasPermission(token);
		Account accountReturnObject = accountServiceImpl.getAccount(accountId);
		log.info("Account Details returned sucessfully");
		return new ResponseEntity<>(accountReturnObject, HttpStatus.OK);
	}

	/*
	 * To create the Account Details
	 * 
	 * This controller is called by Customer-Module for creating Account
	 */
	@PostMapping("/createAccount/{customerId}")
	public ResponseEntity<?> createAccount(@RequestHeader("Authorization") String token,
			@PathVariable String customerId, @Valid @RequestBody Account account) {
		accountServiceImpl.hasEmployeePermission(token);
		AccountCreationStatus returnObjAccountCreationStatus = accountServiceImpl.createAccount(customerId, account);
		if (returnObjAccountCreationStatus == null)
			return new ResponseEntity<>("Customer Creation Unsucessful", HttpStatus.NOT_ACCEPTABLE);
		log.info("Account Created Sucessfully");
		return new ResponseEntity<>(returnObjAccountCreationStatus, HttpStatus.CREATED);
	}

	/*
	 * To get the account Details
	 * 
	 * Pass the CustomerID along with token
	 */
	@GetMapping("/getAccounts/{customerId}")
	public ResponseEntity<List<Account>> getCustomerAccount(@RequestHeader("Authorization") String token,
			@PathVariable String customerId) {
		accountServiceImpl.hasPermission(token);
		log.info("Account list returned");
		return new ResponseEntity<>(accountServiceImpl.getCustomerAccount(token, customerId), HttpStatus.OK);
	}

	/*
	 * To Deposit cash in the account
	 * 
	 * This controller is called by Account-Module to Transaction-Module for
	 * Depositing amount in the Account
	 */
	@PostMapping("/deposit")
	public ResponseEntity<Account> deposit(@RequestHeader("Authorization") String token,
			@RequestBody AccountInput accInput) {
		accountServiceImpl.hasPermission(token);
		transactionFeign.makeDeposit(token, accInput);
		Account newUpdateAccBal = accountServiceImpl.updateDepositBalance(accInput);
		List<Transaction> list = transactionFeign.getTransactionsByAccId(token, accInput.getAccountId());
		newUpdateAccBal.setTransactions(list);
		log.info("Amount Deposited");
		return new ResponseEntity<>(newUpdateAccBal, HttpStatus.OK);
	}

	/*
	 * To Withdraw cash in the account
	 * 
	 * This controller is called by Account-Module to Transaction-Module for
	 * withdrawing in the Account
	 */
	@PostMapping("/withdraw")
	public ResponseEntity<Account> withdraw(@RequestHeader("Authorization") String token,
			@RequestBody AccountInput accInput) {
		accountServiceImpl.hasPermission(token);
		try {
			transactionFeign.makeWithdraw(token, accInput);

		} catch (Exception e) {
			throw new MinimumBalanceException("Minimum Balance 1000 should be maintaind");
		}
		Account newUpdateAccBal = accountServiceImpl.updateBalance(accInput);
		List<Transaction> list = transactionFeign.getTransactionsByAccId(token, accInput.getAccountId());
		newUpdateAccBal.setTransactions(list);
		log.info("Withdraw was sucessful");
		return new ResponseEntity<>(newUpdateAccBal, HttpStatus.OK);
	}

	/*
	 * To get service charge on not maintaining minimum balance account
	 * 
	 */
	@PostMapping("/servicecharge")
	public ResponseEntity<Account> servicecharge(@RequestHeader("Authorization") String token,
			@RequestBody AccountInput accInput) {
		accountServiceImpl.hasPermission(token);
		try {
			transactionFeign.makeServiceCharges(token, accInput);

		} catch (Exception e) {
			throw new MinimumBalanceException("Minimum Balance 1000 should be maintained");
		}
		Account newUpdateAccBal = accountServiceImpl.updateBalance(accInput);
		List<Transaction> list = transactionFeign.getTransactionsByAccId(token, accInput.getAccountId());
		newUpdateAccBal.setTransactions(list);
		log.info("Service amount charged sucessfully");
		return new ResponseEntity<>(newUpdateAccBal, HttpStatus.OK);
	}

	/*
	 * Transaction of cash between the accounts
	 * 
	 * This controller is called by Account-Module to Transaction-Module for
	 * Transaction in the another Account
	 */
	@PostMapping("/transaction")
	public ResponseEntity<String> transaction(@RequestHeader("Authorization") String token,
			@RequestBody TransactionInput transInput) {
		accountServiceImpl.hasPermission(token);
		boolean status = true;
		try {
			status = transactionFeign.makeTransfer(token, transInput);

		} catch (Exception e) {
			throw new MinimumBalanceException("Minimum Balance 1000 should be maintained");
		}
		if (status == false) {
			return new ResponseEntity<>("Transaction Failed", HttpStatus.NOT_IMPLEMENTED);
		}
		Account updatedSourceAccBal = accountServiceImpl.updateBalance(transInput.getSourceAccount());
		List<Transaction> sourcelist = transactionFeign.getTransactionsByAccId(token,
				transInput.getSourceAccount().getAccountId());
		updatedSourceAccBal.setTransactions(sourcelist);

		Account updatedTargetAccBal = accountServiceImpl.updateDepositBalance(transInput.getTargetAccount());
		List<Transaction> targetlist = transactionFeign.getTransactionsByAccId(token,
				transInput.getTargetAccount().getAccountId());
		updatedTargetAccBal.setTransactions(targetlist);
		return new ResponseEntity<>(
				"Transaction made Successfully From Source AccountId" + transInput.getSourceAccount().getAccountId()
						+ " To Target AccountId " + transInput.getTargetAccount().getAccountId() + " ",
				HttpStatus.OK);
	}

	/*
	 * Checking balance in the account
	 * 
	 * This controller is called by Customer-Module for checking balance in the
	 * Account
	 */
	@PostMapping("/checkBalance")
	public ResponseEntity<Account> checkAccountBalance(@RequestHeader("Authorization") String token,
			@Valid @RequestBody AccountInput accountInput) {
		accountServiceImpl.hasPermission(token);
		Account account = accountServiceImpl.getAccount(accountInput.getAccountId());
		return new ResponseEntity<>(account, HttpStatus.OK);
	}

	@GetMapping("/find")
	public ResponseEntity<List<Account>> getAllAccount(@RequestHeader("Authorization") String token) {
		accountServiceImpl.hasPermission(token);
		List<Account> account = accountServiceImpl.getAllAccounts();
		return new ResponseEntity<>(account, HttpStatus.OK);
	}

}
