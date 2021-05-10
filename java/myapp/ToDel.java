package myapp;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
public class ToDel extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ResultSet rs = null;
    String url = null;
    Connection conn = null;
    String[] userEmail = req.getParameterValues("Email");
    String[] location = req.getParameterValues("location");
    resp.setContentType("text/plain");
    resp.getWriter().println(userEmail[0]);
    resp.getWriter().println();
    try {
        Class.forName("com.mysql.jdbc.GoogleDriver");
        url = "jdbc:google:mysql://fair-scout-287500:us-west2:email-instance/EmailSchema"; 
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }
    try {
        try {
            resp.setContentType("text/plain");
            conn = DriverManager.getConnection(url, "root", "AdithyaGiri");
            String check = "SELECT EXISTS(SELECT * from Persons WHERE Emailid=\""+userEmail[0]+"\")";
            Statement checkStmt = conn.createStatement();
            rs = checkStmt.executeQuery(check);
            rs.next();
            String checkResult = rs.getString("EXISTS(SELECT * from Persons WHERE Emailid=\""+userEmail[0]+"\")");
            String stmt = null;
            if (checkResult.equals("1")) {
                stmt = "DELETE from Persons WHERE Emailid = \""+userEmail[0]+"\" and location = \""+location[0]+"\";";
            }
            Statement st = conn.createStatement();
            st.execute(stmt);
            resp.getWriter().println("-- done --");
            conn.close();
        } catch(Exception e) {
            conn.close();
            resp.getWriter().println(e.getMessage());
        }
    } catch (SQLException e) {
        resp.getWriter().println("SQLException: " + e.getMessage());
    }
  }
}