package com.cognizant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Date;

import org.junit.jupiter.api.Test;

import com.cognizant.accountservice.model.CustomerEntity;

class CustomerEntityTest {


	CustomerEntity entity = new CustomerEntity();
	@SuppressWarnings("deprecation")
	CustomerEntity entity2 = new CustomerEntity("Cust101", "Ram", "Cust", new Date(1998, 4, 3), "PAN456123",
			"Pune");

	@Test
	void setUserIdTest() {
		entity.setUserid("Cust101");
		assertEquals("Cust101", entity.getUserid());
	}

	@Test
	void setUserNameTest() {
		entity.setUsername("Cust101");
		assertEquals("Cust101", entity.getUsername());
	}

	@Test
	void setPasswordTest() {
		entity.setPassword("Cust101");
		assertEquals("Cust101", entity.getPassword());
	}

	@Test
	void setPanTest() {
		entity.setPan("PAN456123");
		assertEquals("PAN456123", entity.getPan());
	}

	@Test
	void setAddressTest() {
		entity.setAddress("Pune");
		assertEquals("Pune", entity.getAddress());
	}

	@SuppressWarnings("deprecation")
	@Test
	void setDateTest() {
		entity.setDateOfBirth(new Date(1998, 4, 3));
		assertEquals(new Date(1998, 4, 3), entity.getDateOfBirth());
	}

}
