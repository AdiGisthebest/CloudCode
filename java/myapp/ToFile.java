package myapp;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
public class ToFile extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String url = null;
    Connection conn = null;
    String[] userEmail = req.getParameterValues("Email");
    String[] priceRange = req.getParameterValues("Pricerange");
    String[] JSONStr = req.getParameterValues("JSONStr");
    String[] location = req.getParameterValues("location");
    String jString = JSONStr[0].replace("\"","\\\"");
    resp.setContentType("text/plain");
    resp.getWriter().println(userEmail[0]);
    resp.getWriter().println(priceRange[0]);
    resp.getWriter().println(jString);
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
            String stmt;
            stmt = "insert into Persons values(\""+userEmail[0]+"\",\""+jString+"\","+priceRange[0]+",\""+location[0]+"\")";
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