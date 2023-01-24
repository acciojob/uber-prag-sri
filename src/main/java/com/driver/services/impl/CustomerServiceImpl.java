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
		TripBooking bookedTrip= null;
		List<Driver> driverList= driverRepository2.findAll();

		boolean isDriverAvailable= false;

		for(Driver driver: driverList)
		{
			Cab cab= driver.getCab();
			if(cab.getAvailable())
			{
				isDriverAvailable = true;

				int bill= cab.getPerKmRate()*distanceInKm;
				Customer assignedCustomer = customerRepository2.findById(customerId).get();
				bookedTrip= new TripBooking(fromLocation,toLocation,distanceInKm,TripStatus.CONFIRMED,bill);

				Customer customer= customerRepository2.findById(customerId).get();
				bookedTrip.setCustomer(customer);
				bookedTrip.setDriver(driver);

				cab.setAvailable(false);

				List<TripBooking> tripBookingList= customer.getTripBookingList();
				if(tripBookingList==null)
					tripBookingList= new ArrayList<>();
				tripBookingList.add(bookedTrip);
				customer.setTripBookingList(tripBookingList);

				tripBookingList= driver.getTripBookingList();
				if(tripBookingList==null)
					tripBookingList= new ArrayList<>();
				tripBookingList.add(bookedTrip);
				driver.setTripBookingList(tripBookingList);

				driverRepository2.save(driver);
				customerRepository2.save(customer);

				break;
			}
		}
		if(isDriverAvailable==false)
		{
			throw new Exception("No cab available!");
		}

		return bookedTrip;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking bookedTrip= tripBookingRepository2.findById(tripId).get();
		bookedTrip.setStatus(TripStatus.CANCELED);
		bookedTrip.setBill(0);
		Driver assignedDriver= bookedTrip.getDriver();
		Cab assignedCab= assignedDriver.getCab();
		assignedCab.setAvailable(true);

		tripBookingRepository2.save(bookedTrip);

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

		customerRepository2.save(bookedTrip.getCustomer());
		driverRepository2.save(assignedDriver);
		tripBookingRepository2.save(bookedTrip);

	}
}
