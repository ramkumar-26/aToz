package com.atoz.serviceImplementation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atoz.exception.AddressException;
import com.atoz.exception.CartException;
import com.atoz.exception.CustomerException;
import com.atoz.exception.LoginException;
import com.atoz.exception.OrderException;
import com.atoz.exception.ProductException;
import com.atoz.model.Address;
import com.atoz.model.Cart;
import com.atoz.model.Customer;
import com.atoz.model.Order;
import com.atoz.model.Product;
import com.atoz.model.ProductDto;
import com.atoz.repository.AddressDao;
import com.atoz.repository.CustomerDao;
import com.atoz.repository.OrderDao;
import com.atoz.repository.ProductDao;
import com.atoz.service.OrderService;
@Service
public class OrderServiceImpl implements OrderService{
	@Autowired	
	OrderDao orderRepo;
	
	@Autowired
	ProductDao prodRepo;
	
	@Autowired
	CustomerDao customerRepo;
	
	@Autowired
	Validation valid;
	

	@Override
	public Order addOrder(Order order, String key) throws LoginException, CustomerException, CartException, ProductException {
		 
		
		
		//validating customer and getting the customer object
		Customer customer = valid.validateLogin(key);
		
		
		//getting product list from cart and add it to order product list and empty cart
		Cart custCart = customer.getCart();
		
		List<ProductDto> productList = custCart.getProducts();
		
		
		if(productList.isEmpty()) {
			throw new OrderException("Your Cart is Empty! Please Add Products to your cart before Ordering");
		}else {
			order.setProductList(productList);
			order.setCustomer(customer);
//			if(customer.getAddress()==null) {
//				throw new AddressException("Add Address before Order!");
//			}
			
			customer.getCart().setProducts(new ArrayList<>());
		}
		
		Order new_order = orderRepo.save(order);
		
		customer = customerRepo.save(customer);		
		if(customer==null) {
			throw new CustomerException("Order: Error while emptying the cart!");
		}
		
		if(new_order!=null) {
			
			for(ProductDto prod:productList) {
				
				Integer prodId = prod.getProductId();
				Optional<Product> eachProduct = prodRepo.findById(prodId);
				if(eachProduct.isEmpty()) {
					throw new ProductException("Can't able find Product!");
				}else {
					
					Product product = eachProduct.get();
					
					if(product.getQuantity()<prod.getQuantity()) {
						throw new OrderException("Order quantity of product "+product.getProductId()+" exceeds available quanity("+product.getQuantity()+")");
					}
					product.setQuantity(product.getQuantity()-prod.getQuantity());
					prodRepo.save(product);
				}
				
		}
			
			
			return new_order;
		}else {
			throw new OrderException("Error Creating Order!");
		}
		
		
	}

	@Override
	public Order updateOrder(Order orderUpdate, String key) throws LoginException, CustomerException, ProductException {
		//just validation
		Customer customer = valid.validateLogin(key);
		//System.out.print(orderUpdate.toString());
		if(orderUpdate.getProductList()==null) {
			throw new OrderException("Order doesn't contain any product to update");
		}
		
		List<ProductDto> productList = orderUpdate.getProductList();
		
		for(ProductDto prod:productList) {
			
			Integer prodId = prod.getProductId();
			Optional<Product> eachProduct = prodRepo.findById(prodId);
			if(eachProduct.isEmpty()) {
				throw new ProductException("Can't able find Product!");
			}else {
				
				Product product = eachProduct.get();
				if(product.getQuantity()<prod.getQuantity()) {
					throw new OrderException("Order quantity of product "+product.getProductId()+" exceeds available quanity("+product.getQuantity()+")");
				}
				product.setQuantity(product.getQuantity()-prod.getQuantity());
				prodRepo.save(product);
			}
			
		}
		Optional<Order> existingOrder = orderRepo.findById(orderUpdate.getOrderId());
		if(existingOrder.isPresent()) {
			Order order = existingOrder.get();
			order.setOrderDate(orderUpdate.getOrderDate());
			order.setOrderStatus(orderUpdate.getOrderStatus());
			order.setProductList(orderUpdate.getProductList());
			order = orderRepo.save(order);
			
			return order;
		}else {
			throw new OrderException("Order is not available to update!");
		}

	}

	@Override
	public Order removeOrder(Integer orderID, String key) throws LoginException, CustomerException {
		//login validation
		Customer customer = valid.validateLogin(key);
//	    List<Order> customer_order_list	 = customer.getOrder();
//	    boolean flag = false;
//	    if(customer_order_list.isEmpty()) {
//	    	throw new OrderException("No Orders found to remove");
//	    }
//		for(Order od : customer_order_list) {
//			if(od.getOrderId()==orderID) {
//				flag = true;
//			}
//		}
//		if(flag)
//		{
		Order deleted_order = orderRepo.findById(orderID).orElseThrow(()-> new OrderException("Order Not Found!"));
		orderRepo.delete(deleted_order);
		return deleted_order;
//		}else {
//			throw new OrderException("Current User Doesn't have this order with orderid "+orderID);
//		}
		
		
		
		
	}

	@Override
	public Order viewOrder(Integer orderId) {

		Order order = orderRepo.findById(orderId).orElseThrow(()-> new OrderException("Order Not Availbale to view"));
		
		return order;
	}

	@Override
	public List<Order> viewAllOrderByDate(LocalDate date) {
		// TODO Auto-generated method stub
		List<Order> orderList = orderRepo.findByOrderDate(date);
		if(orderList.isEmpty()) {
			throw new OrderException("No Order for the mentioned date!");
		}else {
			return orderList;
		}
		
	}

	@Override
	public List<Order> viewAllOrderByLocation(String location, String key) throws LoginException, CustomerException {
		// TODO Auto-generated method stub
		Customer customer = valid.validateLogin(key);
		List<Order> orderList = orderRepo.findAll();
		if(orderList.isEmpty()){
			throw new OrderException("No Orders Available in Database!");
		}else {
			
			List<Order> newOrderList = new ArrayList<>();
			for(Order order: orderList) {
				if(order.getCustomer().getAddress().getCity()==location)
					newOrderList.add(order);
			}
			if(newOrderList.isEmpty()) {
				throw new OrderException("No Order for the mentioned location!");
			}else {
				return newOrderList;
			}
			
		}
		
	}

	@Override
	public List<Order> viewAllOrderByUserId(Integer userid) throws CustomerException {
		Optional<Customer> cust = customerRepo.findById(userid);
		if(cust.isPresent()) {
			Customer customer = cust.get();
			List<Order> orderList = customer.getOrder();
			return orderList;
		}else {
			throw new CustomerException("No user found!");
		}
		
	}

	
	

}
