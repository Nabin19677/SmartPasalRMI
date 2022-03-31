package dllImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import bll.Product;
import bll.Transaction;
import dll.ITransaction;
import utils.DbConnection;
import utils.Helpers;


/**
 * This class implements ITransaction
 * @author anilk
 *
 */
public class TransactionImpl extends UnicastRemoteObject implements ITransaction {
	private Connection cn ;

	/**
	 * Default TransactionImpl constructor
	 * @throws RemoteException
	 */
	public TransactionImpl() throws RemoteException {
		super();
		this.cn = DbConnection.getInstance().connection;
	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public boolean checkout(ArrayList<Product> items, double totalAmount, double paid)
			throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			System.out.println("Checkout method called");
			
			String transactionString = "INSERT INTO transactions (transactionId, total, payment) VALUES(?,?,?)";
			
			String backupTransactionString = "INSERT INTO backup_transactions (transactionId, total, payment) VALUES(?,?,?)";
			
			String soldItemsString = "INSERT INTO sale" + "  (saleId, productId, quantity, transactionId) VALUES "
					+ " (?, ?, ?, ?);";
			
			String backupSoldItemsString = "INSERT INTO backup_sale VALUES(?, ?, ?, ?);";
			
			String updateProductString = "UPDATE product SET productQuantity = (productQuantity - ?) WHERE productId = ? ; ";
			
			cn.setAutoCommit(false);
			
			PreparedStatement transactionPs = cn.prepareStatement(transactionString,
					PreparedStatement.RETURN_GENERATED_KEYS);
			transactionPs.setString(1, null);
			transactionPs.setDouble(2, totalAmount);
			transactionPs.setDouble(3, paid);

			int updatedRow = transactionPs.executeUpdate();
			rs = transactionPs.getGeneratedKeys();
			int transactionId;
			if (rs.next()) {
				transactionId = rs.getInt(1); // got transactionId
				
				PreparedStatement backupTransactionPs = cn.prepareStatement(backupTransactionString);
				backupTransactionPs.setInt(1, transactionId);
				backupTransactionPs.setDouble(2, totalAmount);
				backupTransactionPs.setDouble(3, paid);
				
				PreparedStatement soldItemsStatement = cn.prepareStatement(soldItemsString);
				
				PreparedStatement backupSoldItemsStatement = cn.prepareStatement(backupSoldItemsString);
				
				PreparedStatement updateProductStatement = cn.prepareStatement(updateProductString);
				
				for (Product item : items) {
					//Sold Items
					soldItemsStatement.setString(1, null);
					soldItemsStatement.setInt(2, Integer.parseInt(item.getProductId()));
					soldItemsStatement.setInt(3, item.getQuantity());
					soldItemsStatement.setInt(4, transactionId);
					
					//Backup Items
					backupSoldItemsStatement.setString(1, null);
					backupSoldItemsStatement.setInt(2, Integer.parseInt(item.getProductId()));
					backupSoldItemsStatement.setInt(3, item.getQuantity());
					backupSoldItemsStatement.setInt(4, transactionId);
					
					System.out.println(backupSoldItemsStatement.toString());
					//Updating / restocking products
					updateProductStatement.setInt(1, item.getQuantity());
					updateProductStatement.setInt(2, Integer.parseInt(item.getProductId()));
					
					//Adding batches
					soldItemsStatement.addBatch();
					backupSoldItemsStatement.addBatch();
					updateProductStatement.addBatch();
				}
				//Executing batches
				soldItemsStatement.executeBatch();
				updateProductStatement.executeBatch();


				//executing backups
				backupTransactionPs.execute();
				backupSoldItemsStatement.executeBatch();
			}
			cn.commit();
			cn.setAutoCommit(true);
			return true;
		} catch (Exception e) {
			if (cn != null)
				try {
					cn.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public boolean addStock(ArrayList<Product> items) throws RemoteException {
		System.out.println(items);
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			cn.setAutoCommit(false);
			this.addProductsToStock(items);
			cn.commit();
			cn.setAutoCommit(true);
			return true;
		} catch (Exception e) {
			if (cn != null)
				try {
					cn.rollback();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			return false;
		}

	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public ArrayList<Product> getTransactionDetailById(Integer transId) {
		
		ArrayList<Product> products = new ArrayList<Product>();
		
		try {
			System.out.println("Get All Transactions method called");
			String sql = "SELECT sale.productId, product.productName, sale.quantity\r\n" + "FROM ((sale\r\n"
					+ "INNER JOIN transactions ON sale.transactionId = transactions.transactionId)\r\n"
					+ "INNER JOIN product ON sale.productId = product.productId)" + "WHERE sale.transactionId = ?;";
			PreparedStatement ps = cn.prepareStatement(sql);
			ps.setInt(1, transId);
			ResultSet rs = ps.executeQuery();
            while(rs.next()) {
            	products.add(new Product(rs.getString(1),rs.getString(2),rs.getInt(3)));
            }
            return products;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public ArrayList<Transaction> getAllTransaction() {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		try {
			System.out.println("Get All Transactions method called");
			String sql = "SELECT * FROM transactions";
			PreparedStatement ps = cn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				transactions.add(new Transaction(rs.getString(1),rs.getString(2), rs.getString(4)));
			}
			return transactions;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public ArrayList<Transaction> getAllTransactionOnDate(LocalDateTime localDateTime) {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();
		try {
			String queryString = "SELECT * FROM transactions WHERE DATE(createdOn) = ? ;";
			PreparedStatement ps = cn.prepareStatement(queryString);
			ps.setDate(1, java.sql.Date.valueOf(localDateTime.toLocalDate().plusDays(1)));
			ResultSet rs = ps.executeQuery();

			while(rs.next()) {
				transactions.add(new Transaction(rs.getString(1),rs.getString(2), rs.getString(4)));
			}
			return transactions;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	@Override
	public boolean deleteTransaction(Integer transId) throws RemoteException {
		try {
			cn.setAutoCommit(false);
			ArrayList<Product> details = this.getTransactionDetailById(transId);

			String setForeignKeyCheckString = "SET FOREIGN_KEY_CHECKS = ?";
			PreparedStatement setForeignKeyPs = cn.prepareStatement(setForeignKeyCheckString);
			setForeignKeyPs.setInt(1, 0);
			setForeignKeyPs.execute();
			String deleteTransactionString = "DELETE sale.*, transactions.* \r\n" + "FROM sale \r\n"
					+ "INNER JOIN transactions \r\n" + "  ON sale.transactionId = transactions.transactionId \r\n"
					+ "WHERE sale.transactionId = ? ; \r\n";
			PreparedStatement ps = cn.prepareStatement(deleteTransactionString);
			ps.setInt(1, transId);

			int deletedTransaction = ps.executeUpdate();

			setForeignKeyPs.setInt(1, 1);
			setForeignKeyPs.execute();

			if (deletedTransaction > 0) {
				this.addProductsToStock(details);
				cn.commit();
				cn.setAutoCommit(true);
				return true;
			} else {
				System.out.println("rollback 2");
				cn.rollback();
				return false;
			}
		} catch (Exception e) {
			System.out.println("exception" + e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			return false;
		}

	}

	/**
	 * {@inheritDoc}
	 * @see dll.ITransaction
	 */
	private void addProductsToStock(ArrayList<Product> items) {
		PreparedStatement updateProductStatement;
		try {
			System.out.println("1");
			String updateProductString = "UPDATE product SET productQuantity = (productQuantity + ?) WHERE productId = ? ; ";
			updateProductStatement = cn.prepareStatement(updateProductString);

			System.out.println("2");
			for (Product item : items) {
				System.out.print(item);
				updateProductStatement.setInt(1, item.getQuantity());// item quantity
				updateProductStatement.setInt(2, Integer.parseInt(item.getProductId()));
				;// item id
				updateProductStatement.addBatch();
			}

			updateProductStatement.executeBatch();
			System.out.println("3");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				cn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

}
