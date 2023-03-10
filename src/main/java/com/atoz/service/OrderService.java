package com.atoz.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.atoz.exception.CartException;
import com.atoz.exception.CustomerException;
import com.atoz.exception.LoginException;
import com.atoz.exception.ProductException;
import com.atoz.model.Order;
@Service
public interface OrderService {
	public Order updateOrder(Order order, String  key) throws LoginException, CustomerException, ProductException;
	public Order removeOrder(Integer orderId, String key) throws LoginException, CustomerException;
	public Order viewOrder(Integer orderId);
	public List<Order> viewAllOrderByDate(LocalDate date);
	public List<Order> viewAllOrderByLocation(String location, String key) throws LoginException, CustomerException;
	public List<Order> viewAllOrderByUserId(Integer userid) throws CustomerException;
	public Order addOrder(Order order, String key) throws LoginException, CustomerException, CartException, ProductException;
}
