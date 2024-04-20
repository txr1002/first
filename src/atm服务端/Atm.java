package atm服务端;


import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.sql.*;
//import MySQLdiver.MySQLConnection;


public class Atm {
	private static final int PORT = 2526;
    private static DBConnection db ;
    public static String clientsentence=null;
    
    public static void main(String args[]) {
    	Connection conn;
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        		
        { // 创建ServerSocket并监听指定端口
            System.out.println("Server is listening on port " + PORT);
    	    conn = DBConnection.DBConection();
    	    System.out.println("Database connection succeed");
            while (true) { // 无限循环等待客户端连接
                Socket clientSocket = serverSocket.accept(); // 接受新的客户端连接
                process(clientSocket,conn);
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public static void process(Socket clientSocket,Connection conn) 
    { 
        String userId = null;
        String password = null;
        //double balance = 0;
        DataOutputStream outToClient=null;
    	try{
    		BufferedReader inFromClient = new BufferedReader(
            new InputStreamReader(clientSocket.getInputStream()));
    		outToClient = new DataOutputStream(clientSocket.getOutputStream());
    		clientsentence = inFromClient.readLine();
    		System.out.println("Received from client:" + clientsentence);
    		
    		if (clientsentence != null) {
                if (clientsentence.startsWith("HELO")) {
                	System.out.println("进入helo分支");
                    String[] parts = clientsentence.split(" ");
                    userId = parts[1];
                    //System.out.println(userId);
                    String msg="500 AUTH REQUIRED!";
                    outToClient.writeBytes(msg + '\n');
                    outToClient.flush();
                } else if (clientsentence.startsWith("PASS")) {
                    String[] parts = clientsentence.split(" ");
                    password = parts[1];
                    String sql = "SELECT * FROM accounts WHERE id = ? AND pin = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                        preparedStatement.setString(1, userId);
                        preparedStatement.setString(2, password);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            outToClient.writeBytes("525 OK!" + '\n');
                        } else {
                            outToClient.writeBytes("401 ERROR!" + '\n');
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (clientsentence.startsWith("BALA")) {
                    String sql = "SELECT balance FROM accounts WHERE id = ? AND pin = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                        preparedStatement.setString(1, userId);
                        preparedStatement.setString(2, password);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            String bal = resultSet.getString(1);
                            outToClient.writeBytes("AMNT:" + bal + '\n');
                        } else {
                            outToClient.writeBytes("401 ERROR!" + '\n');
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (clientsentence.startsWith("WDRA ")) {
                    String[] parts = clientsentence.split(" ");
                    String withdrawal = parts[1];
                    String sql = "SELECT balance FROM accounts WHERE id = ?";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                        preparedStatement.setString(1, userId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if (resultSet.next()) {
                            double balance = resultSet.getDouble("balance");
                            double doubleWithdrawal = Double.parseDouble(withdrawal);
                            if (doubleWithdrawal <= balance) {
                                sql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
                                try (PreparedStatement preparedStatementUpdate = conn.prepareStatement(sql)) {
                                    preparedStatementUpdate.setDouble(1, doubleWithdrawal);
                                    preparedStatementUpdate.setString(2, userId);
                                    preparedStatementUpdate.executeUpdate();
                                    outToClient.writeBytes("525 OK!" + '\n');
                                }
                            } else {
                                outToClient.writeBytes("401 ERROR!" + '\n');
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if (clientsentence.startsWith("BYE")) {
                    outToClient.writeBytes("BYE" + '\n');
                } else {
                    outToClient.writeBytes("401 ERROR!" + '\n');
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
/*ResultSet rs;
if (clientsentence.startsWith("HELO")) // 收到了UserId，检查该ID是否在数据库
{
	String[] parts = clientsentence.split(" ");
    userId = parts[1];
    System.out.println(userId);
    outToClient = new DataOutputStream(clientSocket.getOutputStream());
    outToClient.writeBytes("500 AUTH REQUIRED!" + '\n');
}

else if (clientsentence.startsWith("PASS")) { // 收到了password，查看是否与userid匹配
    System.out.println("PASS if进入");
    String[] parts = clientsentence.split(" ");
    password = parts[1];
    System.out.println(userId+password);
    String sql = "select * from accounts where id = '" + userId + "' and pin = '" + password + "'";
    try {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery(); // 执行查询
        if (resultSet.next()) { // 查询结果不为空，则请求密码
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("525 OK!");
            outToClient.writeBytes("525 OK!" + '\n');

        } else { // 查询结果为空，则返回401
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("PASSWORD错误");
            outToClient.writeBytes("401 ERROR!" + '\n');
         
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}
else if (clientsentence.startsWith("BALA"))
    {
	System.out.println("查询BALA");
    System.out.println(userId+password);
    String sql = "select balance from accounts where id = '" + userId + "' and pin = '" + password + "'";
    try {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery(); // 执行查询
        if (resultSet.next()) { // 查询结果不为空，则请求密码
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("525 OK!");
            String bal=resultSet.getString(1);
            outToClient.writeBytes("AMNT:" + bal);

        } else { // 查询结果为空，则返回401
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("PASSWORD错误");
            outToClient.writeBytes("401 ERROR!" + '\n');
         
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    }
else if (clientsentence.startsWith("WDRA "))
{
	System.out.println("取款");
    String[] parts = clientsentence.split(" ");
    String withdrawal = parts[1];
    String sql = "select * from accounts where UserId = '" + userId + "'"; // 查询语句
    try {
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery(sql); // 查询结果
        resultSet.next();
        String balance = resultSet.getString("balance");
        double doubleWithdrawal = Double.parseDouble(withdrawal);
        double doubleBalance = Double.parseDouble(balance);
        if (doubleWithdrawal <= doubleBalance) // 余额充足
        {
            System.out.println("取款成功");
            sql = "update users set Balance = " + (doubleBalance - doubleWithdrawal) + "where UserId = " + userId;
            try {
                PreparedStatement preparedStatementUpdate = conn.prepareStatement(sql);
                
                preparedStatementUpdate.executeUpdate(sql);
                outToClient = new DataOutputStream(clientSocket.getOutputStream());
                outToClient.writeBytes("525 OK!" + '\n');
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else { // 余额不足
            System.out.println("余额不足");
            outToClient = new DataOutputStream(clientSocket.getOutputStream());
            outToClient.writeBytes("401 ERROR!" + '\n');
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

}
else if (clientsentence.startsWith("BYE")) // 结束
{
    try {
        outToClient = new DataOutputStream(clientSocket.getOutputStream());
        outToClient.writeBytes("BYE" + '\n');
        conn.close();
        clientSocket.close();
        //welcomeSocket.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
else {
    try{
    	outToClient = new DataOutputStream(clientSocket.getOutputStream());
    System.out.println("指令错误");
    outToClient.writeBytes("401 ERROR!" + '\n');
    }
    catch (IOException e) {
        // 在这里处理异常，比如打印错误信息或者关闭资源
        e.printStackTrace();
    }
}
	}
	catch(IOException e) {
        // 在这里处理异常，比如打印错误信息或者关闭资源
        e.printStackTrace();
    }
} 
	
}*/
