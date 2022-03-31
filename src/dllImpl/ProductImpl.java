package dllImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.mindrot.jbcrypt.BCrypt;

import bll.Category;
import bll.Product;
import dll.IProduct;
import utils.DbConnection;
import utils.Helpers;

/**
 * This class implements IProduct
 * @author anilk
 *
 */
public class ProductImpl extends UnicastRemoteObject implements IProduct {
	
	private Connection cn;
	
	/**
	 * Default ProductImpl constructor
	 * @throws RemoteException
	 */
	public ProductImpl() throws RemoteException{
		super();
		this.cn = DbConnection.getInstance().connection;
	}
	
	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public ArrayList<Product> getAllProductsOnStock() throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Product> products = new ArrayList<Product>();
		try {
            System.out.println("Get All Products By User method called");
            String sql = "SELECT productId, productName, productQuantity, productRate, categoryId  FROM product WHERE productQuantity > 0 ;";
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
            	products.add(new Product(rs.getString(1),rs.getString(2),rs.getInt(3), rs.getDouble(4), rs.getString(5)));
            }
            return products;
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}

	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public ArrayList<Product> getAllProducts() throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Product> products = new ArrayList<Product>();
		
		try {
            System.out.println("Get All Products By User method called");
            String sql = "SELECT  productId, productName, productQuantity, productRate, categoryId FROM product;";
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
            	products.add(new Product(rs.getString(1),rs.getString(2),rs.getInt(3), rs.getDouble(4), rs.getString(5)));
            }
            return products;
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}
	
	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public ArrayList<Category> getAllCategory() throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Category> categories = new ArrayList<Category>();
		
		try {
            System.out.println("Get All Products By User method called");
            String sql = "SELECT * FROM category";
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
            	categories.add(new Category(rs.getString(1), rs.getString(2)));
            }
            
            return categories;
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}
	
	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public boolean saveProduct(Product product) {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			System.out.println("product Register method called");
			String sql = "INSERT INTO `product`(`productName`, `productQuantity`, `productRate`, `categoryId`) VALUES (?,?,?,?)";
			System.out.print("here");
			// PreparedStatement transactionPs = cn.prepareStatement();
			ps = cn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(1, product.getProductName());
			ps.setInt(2, product.getQuantity());
			ps.setDouble(3, product.getRate());
			ps.setInt(4, Integer.parseInt(product.getCategoryId()));
			int is = ps.executeUpdate();
			System.out.println(is);

			return false;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return false;
		}

	}
	
	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public boolean saveCategory(Category category) {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			System.out.println("Save Cateogry method called");
			String sql = "INSERT INTO `category`(`categoryName`) VALUES (?)";
			ps = cn.prepareStatement(sql);
			ps.setString(1, category.getCategoryName());
			int is = ps.executeUpdate();
			System.out.println(is);

			return false;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return false;
		}

	}

	/**
	 * {@inheritDoc}
	 * @see bll.IProduct
	 */
	@Override
	public boolean deleteProduct(String productId) throws RemoteException {
		try {
			cn.setAutoCommit(false);

			String setForeignKeyCheckString = "SET FOREIGN_KEY_CHECKS = ?";
			PreparedStatement setForeignKeyPs = cn.prepareStatement(setForeignKeyCheckString);
			setForeignKeyPs.setInt(1, 0);
			setForeignKeyPs.execute();
			
			String deleteProductString = "DELETE FROM product \r\n"
					+ "WHERE productId = ? ; \r\n";
			PreparedStatement ps = cn.prepareStatement(deleteProductString);
			ps.setInt(1, Integer.parseInt(productId));

			int deletedProduct = ps.executeUpdate();

			setForeignKeyPs.setInt(1, 1);
			setForeignKeyPs.execute();

			if (deletedProduct > 0) {
				cn.commit();
				cn.setAutoCommit(true);
				return true;
			} else {
				System.out.println("rollback 2 product");
				cn.rollback();
				return false;
			}
		} catch (Exception e) {
			System.out.println("exception" + e);
			try {
				cn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}
			return false;
		}
	}

}
