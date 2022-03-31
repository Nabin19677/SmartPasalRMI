package utils;

//j++;
//System.out.println(j);
//if(j == columnCount) {
//	System.out.println(product);
//	products.add(product);
//	j = 0;
//	product = new ArrayList<>();
//}

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.RandomStringUtils;


/**
 * Helpers.java
 * This class contains static methods for minor issues
 * @author anilk
 *
 */
public class Helpers {
	
	/**
	 * ArrayList from Result Set
	 * @param rs Instance of ResultSet
	 * @see ResultSet
	 * @return
	 */
	public static ArrayList<ArrayList<String>> getArrayListFromResultSet(ResultSet rs){
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();
			ArrayList<ArrayList<String>> products = new ArrayList<>();
			ArrayList<String> product = new ArrayList<>();
			while (rs.next()) {  
			   int i = 1;
			   while(i <= columnCount) {
			        product.add(rs.getString(i++));
			   }
			   products.add(product);
			   product = new ArrayList<>();
			}
			return products;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} 	
	}
	
	
	/**
	 * Generates random String for password of 8 characters long
	 * @return random password
	 */
	public static String generateRandomPassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
		String generatedPass = RandomStringUtils.random(8, characters );
		
		return generatedPass;
	}
}
