package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer= customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		TripBooking bookedTrip= new TripBooking();
		Customer assignedCustomer= customerRepository2.findById(customerId).get();
		bookedTrip.setCustomer(assignedCustomer);
		bookedTrip.setFromLocation(fromLocation);
		bookedTrip.setToLocation(toLocation);
		bookedTrip.setDistanceInKm(distanceInKm);
		bookedTrip.setStatus(TripStatus.CONFIRMED);

		int assignedDriverId= Integer.MAX_VALUE;
		Driver assignedDriver= null;
		Cab assignedCab= null;
		for(Driver driver: driverRepository2.findAll())
		{
			if(driver.getDriverId()<assignedDriverId)
			{
				Cab cab= driver.getCab();
				if(cab.getAvailable())
				{
					assignedDriver= driver;
					assignedCab= driver.getCab();
					assignedDriverId= driver.getDriverId();
				}
			}
		}
		if(assignedDriverId==Integer.MAX_VALUE)
			throw new Exception("No value present");

		bookedTrip.setDriver(assignedDriver);
		assignedCab.setAvailable(false);
		assignedDriver.setCab(assignedCab);
		tripBookingRepository2.save(bookedTrip);
		driverRepository2.save(assignedDriver);
		customerRepository2.save(assignedCustomer);

		return bookedTrip;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking bookedTrip= tripBookingRepository2.findById(tripId).get();
		bookedTrip.setStatus(TripStatus.CANCELED);
		Driver assignedDriver= bookedTrip.getDriver();
		Cab assignedCab= assignedDriver.getCab();
		assignedCab.setAvailable(true);
		assignedDriver.setCab(assignedCab);

		tripBookingRepository2.save(bookedTrip);
		driverRepository2.save(assignedDriver);
		customerRepository2.save(bookedTrip.getCustomer());

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking bookedTrip= tripBookingRepository2.findById(tripId).get();
		bookedTrip.setStatus(TripStatus.COMPLETED);
		Driver assignedDriver= bookedTrip.getDriver();
		Cab assignedCab= assignedDriver.getCab();
		assignedCab.setAvailable(true);
		assignedDriver.setCab(assignedCab);

		tripBookingRepository2.save(bookedTrip);
		driverRepository2.save(assignedDriver);

	}
}
