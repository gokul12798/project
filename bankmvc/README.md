# MFPE --> RETAIL BANKING SYSTEM

One of the largest and leading Retail Bank within the US, serving millions of customers across the country offering a range of financial products from Credit Cards, Savings & Checking accounts, Auto loans, small business & commercial accounts. The retail bank has historically been served by a large monolith system. 

This system has Customer information, Transaction information, Account information – Pretty much a ledger generating taxes & statements. The bank is looking for a solution that will provide resilience & scalability for future growth.

---

## Overview

---
The __Bank Portal__ displays the Login page to the customer and bank employee. It interacts with the Authorization microservice and provides authorization using JWT Authentication.If the credentials are not available in the in-memory H2 database, It provides with an error message showing Invalid Credentials.

If the uer credentials are correct, it generates a token for the particular session. This token is also provided with an expiration time.

Upon succesfully logging in, the user gets redirected to Home page which has options to either show the newtworth of the user or the sell any assets

If the user selects Transaction option, it redirects to transaction page and shows the details of the user.

---    
## Project Details

---

### Backend Microservices

1. Transaction Service
2. Rules Service

### Frontend Microservices

1. Bank Portal

### Configuration Microservices
   1. Account Service
   2. Customer Service

### Auth Microservie

1. Authentication Microservice

---
## Backend Microservices
---

 ### 1. Transaction Service

    It interacts with Customer and Account microservices using feign client to perform transactions like deposite,withdraw from a user.

The Transactions microservice will be responsible for performing all transactions within the Retal bank like Deposits, Withdrawal, Transfers. The service is responsible for checking business rules & propagating transaction contexts to Entities participating in the transaction. This service in turn will invoke behavior on the Account service.
Invoked whenever an user attempts to perform any transactions like deposit, withdraw,
transfer amounts, get the transaction history on his accounts and Transaction histroy information needs to be logged for good performance which will eventually help in getting loan.

---
 ### 2.Rules Service

    The Rules microservice will be responsible for interacting with a rules engine to evaluate certain rules that is applicable prior to performing transactions.

It invokes from transaction service while perfoming a withdrawal or transfer, System generated event fro applying servise charges on account that do not comply with the minimum balance criteria. It invoked on a monthly basis.

---
## Front-end Microservice
---
### 1. Bank Portal Microservice

    It displays all the features available for a Bank Employee and Customer and information about both the entities.

It has a MVC architecture and upon successful login, it provides option to user either login as a Customer or Bank Employee. It interacts with all other microservices available in the application to perform all the feature to both the type of entities(Bank Employee & Customer) and provides a user interface to access our RESTful web services via the web browser. 
It dynamically generate documentation from a Swagger-compliant API. These files are bundled by the Swagger UI project to display the API on the browser. Besides rendering documentation, Swagger UI allows other API developers or consumers to interact with the API’s resources without having any of the implementation logic in place.

---
## Configuration Microservice
---
 ### 1. Account Service

    The Account Microservice returns the customers account details form the bank's server. 

It is a middleware mirco service that allow customer to create a bank account, fetch accounts related to a csutomer as a summary info, fetch account statments for particular customer based on date range, reflect deposited amount of customer and also reflect withdrawing amount from customer's account.

---
 ### 2. Customer Service

    The Customer Microservice returns the details of customer. 

It also create customer , approves or reject the loan wiil be done by the Bank employee from the Banking Protal after inpuuting his info. And Customer can apply for Loan from Banking Protal.
It is a middleware microservice that contains an in-memory h2 databse which has details about the customer's profile. Once the user gets authorized, this microservice fetch the customer info which is present in the database.


---
## Auth Microservice
---
### 1.Authorization Microservice

    Authorization Microservice is to authenticate the users.

Authorization microservice gets the user credentials from the Customer portal microservice and validates the users. It creates a token using the Encryption Algorithm and a secret key which is valid for the entire session.


***

Upon successfully creating these microservices, the jar files for each and every microservice (except Customer Portal) using `mvn clean install` and add a **Dockerfile** which is used to create a docker image.

EC2 instances are created and each docker file is made to push into the ec2 instance using security key to get a public dns.

The Web portal is made to run on local server and it interacts with the other microservices which are uploaded into aws cloud using public IP adresses.

***

