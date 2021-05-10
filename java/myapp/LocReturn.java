package myapp;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import org.json.*;
public class LocReturn extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ResultSet rs = null;
    String url = null;
    Connection conn = null;
    String[] userEmail = req.getParameterValues("Email");
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
            String check = "SELECT EXISTS(SELECT * from Persons WHERE Emailid=\""+userEmail[0]+"\") as result";
            Statement checkStmt = conn.createStatement();
            rs = checkStmt.executeQuery(check);
            rs.next();
            String checkResult = rs.getString("result");
            String stmt;
            if (checkResult.equals("1")) {
                stmt = "Select * from Persons where Emailid = \""+userEmail[0]+"\"";
                Statement st = conn.createStatement();
                rs = st.executeQuery(stmt);
                JSONArray arr = new JSONArray();
                while(rs.next()) {
                    arr.put(new JSONObject(rs.getString("JSONStr")));
                }
                resp.getWriter().println(arr.toString());
            } else {
                resp.getWriter().println("No results found");
            }
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