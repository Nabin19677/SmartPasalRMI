package dllImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.RandomStringUtils;
import org.mindrot.jbcrypt.BCrypt;

import bll.Auth;
import dll.IAuthentication;
import services.SendGridEmailService;
import utils.DbConnection;
import utils.Helpers;

/**
 * This class implements IAuthentication
 * {@inheritDoc}
 *
 */
public class AuthenticationImpl extends UnicastRemoteObject implements IAuthentication{
	private Connection cn;
	
	/**
	 * Default AuthenticationImpl constructor
	 * @throws RemoteException
	 */
    public AuthenticationImpl() throws RemoteException{
        super();
        this.cn = DbConnection.getInstance().connection;
    }

   
    /**
     * {@inheritDoc}
     * This is implementation of login from IAuthentication 
     * @see dll.IAuthentication
     */
	@Override
	public Boolean login(Auth auth) throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
            System.out.println("Login method called");
            String sql = "SELECT * FROM user WHERE username =?";
            System.out.print("here");
            ps = cn.prepareStatement(sql);
            ps.setString(1, auth.getUsername());
            rs = ps.executeQuery();
            while (rs.next()) {
                // Read values using column name
                String hashedPassword = rs.getString("password");
              
                if (BCrypt.checkpw(auth.getPassword(), hashedPassword)) {
                	System.out.println("It matches");
                	return true;
                }
                else {
                	System.out.println("It does not match");
                	return false;
                }
            }
            return false;
        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
	}


    /**
     * {@inheritDoc}
     * This is implementation of passwordReset from IAuthentication 
     * @see dll.IAuthentication
     */
	@Override
	public Boolean passwordReset(String email, String userId) throws RemoteException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String generatedPass = Helpers.generateRandomPassword(); 
        
		try {
			System.out.println("Forget Password Method called");
			
	        String checkUser = "SELECT * FROM user WHERE email = ? AND userId = ?";
	        System.out.print("here");
	        ps = cn.prepareStatement(checkUser);
	        ps.setString(1, email);
	        ps.setString(2, userId);
	        rs = ps.executeQuery();
	        
	        if (rs.next() == false) return false;

	            
	            
	          
            System.out.println("Password Reset method called");

            String newPassword = BCrypt.hashpw(generatedPass, BCrypt.gensalt(12));
            String sql = "UPDATE user \r\n" + 
            		"SET password = ? \r\n" + 
            		"WHERE email = ? AND userId = ? ;";
            System.out.print("here");
            ps = cn.prepareStatement(sql);
            
            ps.setString(1,newPassword);
            ps.setString(2, email);
            ps.setString(3, userId);
            
            ps.executeUpdate();

            SendGridEmailService.sendEmail("Password Reset", email, "Your new password is : " + generatedPass);
            return true;

        } catch (Exception e) {
            System.out.println("exception" + e);
            return null;
        }
		
	}
}
