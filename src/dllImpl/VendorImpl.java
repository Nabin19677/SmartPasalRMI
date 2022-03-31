package dllImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.mindrot.jbcrypt.BCrypt;

import bll.Product;
import bll.Vendor;
import dll.IVendor;
import utils.DbConnection;
import utils.Helpers;

/**
 * This class implements IVendor
 * @author anilk
 *
 */
public class VendorImpl extends UnicastRemoteObject implements IVendor {
	private Connection cn;

	/**
	 * Default VendorImpl constructor
	 * @throws RemoteException
	 */
	public VendorImpl() throws RemoteException {
		super();
		this.cn = DbConnection.getInstance().connection;
	}

	/**
	 * {@inheritDoc}
	 * @see dll.IVendor
	 */
	@Override
	public boolean VendorReg(Vendor vendor) throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			System.out.println("Vendor Register method called");
			String sql = "INSERT INTO `vendors`(`vendorName`, `address`, `email`, `phone`) VALUES (?,?,?,?)";
			
			ps = cn.prepareStatement(sql);
			
			ps.setString(1, vendor.getName());
			ps.setString(2, vendor.getAddress());
			ps.setString(3, vendor.getEmail());
			ps.setString(4, vendor.getPhone());
			
			
			int is = ps.executeUpdate();
			System.out.println(is);

			return true;
		} catch (Exception e) {
			System.out.println("exception" + e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 * @see dll.IVendor
	 */
	@Override
	public ArrayList<Vendor> VendorList() throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ArrayList<Vendor> vendor = new ArrayList<Vendor>();
		
		try {
            System.out.println("Get All Vendors method called");
            String sql = "SELECT * FROM vendors";
            ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while(rs.next()) {
            	vendor.add(new Vendor(rs.getString(1),rs.getString(2),rs.getString(3), rs.getString(4), rs.getString(5)));
            }
            return vendor;
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}

	/**
	 * {@inheritDoc}
	 * @see dll.IVendor
	 */
	@Override
	public boolean deleteVendor(String vendorId) throws RemoteException {
		try {
			cn.setAutoCommit(false);

			String setForeignKeyCheckString = "SET FOREIGN_KEY_CHECKS = ?";
			PreparedStatement setForeignKeyPs = cn.prepareStatement(setForeignKeyCheckString);
			setForeignKeyPs.setInt(1, 0);
			setForeignKeyPs.execute();
			
			String deleteVendorString = "DELETE FROM vendors \r\n"
					+ "WHERE id = ? ; \r\n";
			PreparedStatement ps = cn.prepareStatement(deleteVendorString);
			ps.setInt(1, Integer.parseInt(vendorId));

			int deletedVendor = ps.executeUpdate();

			setForeignKeyPs.setInt(1, 1);
			setForeignKeyPs.execute();

			if (deletedVendor > 0) {
				cn.commit();
				cn.setAutoCommit(true);
				return true;
			} else {
				System.out.println("rollback 2 vendor");
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
