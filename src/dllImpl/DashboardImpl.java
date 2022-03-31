package dllImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import bll.Product;
import dll.IDashboard;
import utils.DbConnection;
import utils.Helpers;

/**
 * This class implements IDashboard
 * 
 * @author anilk
 *
 */
public class DashboardImpl extends UnicastRemoteObject implements IDashboard{
	private Connection cn;
	
	/**
	 * Default DashboardImpl constructor
	 * @throws RemoteException
	 */
    public DashboardImpl() throws RemoteException{
        super();
        this.cn = DbConnection.getInstance().connection;
    }

    /**
     * {@inheritDoc}
     * Implementation of getQuickDetails from IDashboard
     * @see dll.IDashboard
     */
	@Override
	public ArrayList<String> getQuickDetails() throws RemoteException {
		ArrayList<String> quickDetails = new ArrayList<>();
		try {
            System.out.println("Get quick details method called");
            String totalSales = "SELECT COUNT(transactionId) FROM transactions;";
            String totalProducts = "SELECT COUNT(productId) FROM product;";
            String highestTransaction = "SELECT MAX(total) FROM transactions;";
            PreparedStatement salesPs = cn.prepareStatement(totalSales);
            PreparedStatement productPs = cn.prepareStatement(totalProducts);
            PreparedStatement highestTransactionPs = cn.prepareStatement(highestTransaction);
            ResultSet sales = salesPs.executeQuery();
            ResultSet products = productPs.executeQuery();
            ResultSet highest = highestTransactionPs.executeQuery();
            if(sales.next()) quickDetails.add(sales.getString(1));
            if(products.next()) quickDetails.add(products.getString(1));
            if(highest.next()) quickDetails.add(highest.getString(1));
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
		return quickDetails;
	}

    /**
     * {@inheritDoc}
     * Implementation of getSalePieChartData from IDashboard
     * @see dll.IDashboard
     */
	@Override
	public ArrayList<ArrayList<String>> getSalePieChartData() throws RemoteException {
		try {
            System.out.println("Get quick details method called");
            String salesData = "SELECT s.productId, p.productName, SUM(s.quantity) \r\n"
            		+ "FROM sale s \r\n"
            		+ "INNER JOIN product p \r\n"
            		+ "ON s.productId = p.productId \r\n"
            		+ "GROUP BY p.productName \r\n"
            		+ "ORDER BY SUM(s.quantity) DESC \r\n"
            		+ "LIMIT 5";
            PreparedStatement salesPs = cn.prepareStatement(salesData);
            ResultSet sales = salesPs.executeQuery();
            
            return Helpers.getArrayListFromResultSet(sales);

        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}
	
    /**
     * {@inheritDoc}
     * Implementation of getSaleLineChartData from IDashboard
     * @see dll.IDashboard
     */
	@Override
	public ArrayList<ArrayList<String>> getSaleLineChartData() throws RemoteException {
		try {
            System.out.println("Get quick details method called");
            String salesData = "SELECT DATE_FORMAT(createdOn, '%d') date, SUM(total) \r\n"
            		+ "FROM transactions GROUP BY DATE_FORMAT(createdOn, '%d')";
            PreparedStatement salesPs = cn.prepareStatement(salesData);
            ResultSet sales = salesPs.executeQuery();
            
            return Helpers.getArrayListFromResultSet(sales);

        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}

    /**
     * {@inheritDoc}
     * Implementation of getGoingOutOfStockData from IDashboard
     * @see dll.IDashboard
     */
	@Override
	public ArrayList<Product> getGoingOutOfStockData() throws RemoteException {
		ArrayList<Product> products = new ArrayList<Product>();
		
		try {
            System.out.println("Get quick details method called");
            String productData = "SELECT productId, productName, productQuantity  \r\n"
            		+ "FROM product \r\n"
            		+ "WHERE productQuantity <= 5 \r\n"
            		+ "LIMIT 5";
            PreparedStatement productPs = cn.prepareStatement(productData);
            ResultSet productResult = productPs.executeQuery();
            
            while(productResult.next()) {
            	products.add(new Product(productResult.getString(1),productResult.getString(2),productResult.getInt(3)));
            }
            return products;

        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}
    
    
}
