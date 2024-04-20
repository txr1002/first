package atm服务端;

import java.sql.*;


 public class DBConnection {
  public static Connection DBConection(){
	  Connection conn=null;
	  	 String JDBC_URL = "jdbc:mysql://localhost:3306/accounts?serverTimezone=UTC";
	     String USER = "root";
	     String PASSWORD = "0427";
	    try {
	    	conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
	    	System.out.println("Database connection succeed");
	    }
	    catch (SQLException e) {
            System.err.println("Database connection failed");
            e.printStackTrace();
            }
	    return conn;
}
}

