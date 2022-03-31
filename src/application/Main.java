package application;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import dll.IAuthentication;
import dll.IDashboard;
import dll.IProduct;
import dll.ITransaction;
import dll.IVendor;
import dllImpl.AuthenticationImpl;
import dllImpl.DashboardImpl;
import dllImpl.ProductImpl;
import dllImpl.TransactionImpl;
import dllImpl.VendorImpl;

/**
 * Main class of RMI server project
 * @author anilk
 *
 */
public class Main {

	/**
	 * entry point main method for rmi server
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Attempting to start the RMI Server...");
		try {
			IAuthentication auth = new AuthenticationImpl();
			IProduct product = new ProductImpl();
			ITransaction transaction = new TransactionImpl();
			IVendor vendor = new VendorImpl();
			IDashboard dashboard = new DashboardImpl();
			
			Registry registry = LocateRegistry.createRegistry(1099); // Creating registry for binding to different
																		// implementation
			registry.rebind("Auth", auth); // Binding Authentication impl
			registry.rebind("Product", product);// Binding Products impl
			registry.rebind("Transaction", transaction);// Binding Transactions impl
			registry.rebind("Vendor", vendor);
			registry.rebind("Dashboard", dashboard);// Binding Dashboard impl

			System.out.print("Server Started");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
