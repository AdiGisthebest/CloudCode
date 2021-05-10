package myapp;
import java.util.Date;
import java.net.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.LinkedList;
import org.json.*;
import org.apache.commons.io.IOUtils;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ListIterator;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import org.apache.commons.lang3.time.DateUtils;
class Getdata {
    LinkedList<String> filterHome =  new LinkedList<>();
    LinkedList<String> zpList = new LinkedList<>();
    String username;
    String email = null;
    public Getdata(String Email) {
        email = Email;
    }
    public LinkedList<String> read(String baths, String beds, String username, String price, String minprice, String city, String state) {
        LinkedList<String> addressList = new LinkedList<>();
        String projurl;
        String params;
        int num = 7;
        for(int k = 1; k <= num; k++) {
            boolean breakbool = false;
            try {
                if(k == 1) {
                    projurl = "https://www.parsehub.com/api/v2/projects/teOFUzvTOKdo/run";
                    city = city.replace(' ', '-');
                    params = "api_key=tkgYeW-gNUAi&start_url=" + URLEncoder.encode("https://homefinder.com/for-sale/"+state+ "/"+city+"?minPrice = "+minprice+"&maxPrice="+price+"&beds="+beds+"&baths="+baths+"&propertyTypes=SFH",StandardCharsets.UTF_8.toString());
                } else {
                    projurl = "https://www.parsehub.com/api/v2/projects/teOFUzvTOKdo/run";
                    params = "api_key=tkgYeW-gNUAi&start_url=" + URLEncoder.encode("https://homefinder.com/for-sale/"+state+ "/"+city+"?minPrice = "+minprice+"&maxPrice="+price+"&beds="+beds+"&baths="+baths+"&propertyTypes=SFH&page=" + k,StandardCharsets.UTF_8.toString());
                }
                byte[] postData = params.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                URL url = new URL(projurl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                try( DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    wr.write(postData);
                }
                String str = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                System.out.println(str + "llllllllllllllllllllllllllllllllllllll");
                JSONObject obj = new JSONObject(str);
                String runToken = obj.getString("run_token");
                projurl = "https://www.parsehub.com/api/v2/runs/" + runToken + "?api_key=tkgYeW-gNUAi";
                String stat = "";
                connection.disconnect();
                while (!stat.equals("complete")) {
                    url = new URL(projurl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-Type", "application/json");
                    str = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
                    JSONObject status = new JSONObject(str);
                    stat = status.getString("status");
                    System.out.println(stat + runToken);
                    connection.disconnect();
                    TimeUnit.SECONDS.sleep(3);
                }
                projurl = "https://www.parsehub.com/api/v2/runs/" + runToken + "/data?api_key=tkgYeW-gNUAi";
                url = new URL(projurl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "gzip");
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "text/plain,charset=UTF-8");
                connection.connect();
                InputStreamReader reader = new InputStreamReader(new GZIPInputStream(connection.getInputStream()));
                BufferedReader read = new BufferedReader(reader);
                StringBuffer add = new StringBuffer();
                String line = read.readLine();
                while (line != null) {
                    add.append(line);
                    line = read.readLine();
                }
                read.close();
                str = add.toString();
                System.out.println(str);
                JSONObject res = new JSONObject(str);
                JSONArray JArr = res.getJSONArray("homes");
                if(JArr.length() == 0) {
                    break;
                }
                for (int i = 0; i < JArr.length(); i++) {
                    String[] arr = JArr.getJSONObject(i).getString("home").split("\n");
                    String homeStr = arr[0] + arr[1];
                    String[] homeArr = homeStr.split("");
                    try {
                        Integer.parseInt(homeArr[0]);
                        addressList.add(homeStr);
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                    }
                    System.out.println(homeStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(breakbool) {
                break;
            }
        }
        return addressList;
    }
    public void filter(ListIterator<String> iter,double filterrent) {
         while(iter.hasNext()) {
            String address = iter.next();
            try {
                String[] concat = address.split(" ");
                StringBuilder build = new StringBuilder();
                for (int i = 0; i < concat.length - 1; i++) {
                    build.append(concat[i] + "+");
                }
                build.append(concat[concat.length - 1]);
                String projurl = "http://www.zillow.com/webservice/GetSearchResults.htm?zws-id=X1-ZWz17iezcu3wgb_5cfwc&address=" + build.toString() + "&citystatezip=" + concat[concat.length - 1] + "&rentzestimate=true";
                URL url = new URL(projurl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                DocumentBuilderFactory dbc = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbc.newDocumentBuilder();
                Document doc = db.parse(connection.getInputStream());
                doc.getDocumentElement().normalize();
                Element elem = doc.getDocumentElement();
                if (!((Element) elem.getElementsByTagName("message").item(0)).getElementsByTagName("text").item(0).getTextContent().equals("Request successfully processed")) {
                    //System.out.println("FAILED");
                    System.out.println(((Element) elem.getElementsByTagName("message").item(0)).getElementsByTagName("text").item(0).getTextContent());
                } else {
                    int zest = Integer.parseInt(((Element) ((Element) ((Element) ((Element) elem.getElementsByTagName("response").item(0)).getElementsByTagName("results").item(0)).getElementsByTagName("result").item(0)).getElementsByTagName("zestimate").item(0)).getElementsByTagName("amount").item(0).getTextContent());
                    int rentzest = Integer.parseInt(((Element) ((Element) ((Element) ((Element) elem.getElementsByTagName("response").item(0)).getElementsByTagName("results").item(0)).getElementsByTagName("result").item(0)).getElementsByTagName("rentzestimate").item(0)).getElementsByTagName("amount").item(0).getTextContent());
                    String zpid = ((Element) ((Element) ((Element) elem.getElementsByTagName("response").item(0)).getElementsByTagName("results").item(0)).getElementsByTagName("result").item(0)).getElementsByTagName("zpid").item(0).getTextContent();
                    System.out.println(address + "SUCCESS");
                    if (rentzest > filterrent * zest) {
                        System.out.println("hello");
                        zpList.add(zpid);
                        filterHome.add(address);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        ResultSet rs = null;
        String url = null;
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
            url = "jdbc:google:mysql://fair-scout-287500:us-west2:email-instance/EmailSchema";
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            conn = DriverManager.getConnection(url, "root", "AdithyaGiri");
            String check;
            if (email == null) {
                check = "SELECT * FROM Persons ORDER BY pricerange";
            } else {
                check = "SELECT * FROM Persons WHERE Emailid = \""+email+"\"";
            }
            Statement checkStmt = conn.createStatement();
            rs = checkStmt.executeQuery(check);
        } catch(SQLException e) {
            e.printStackTrace();
        }
        try {
            while (rs.next()) {
                try {
                    JSONArray jarr = new JSONArray();
                    JSONObject jsonobj = new JSONObject();
                    JSONObject jobj = new JSONObject(rs.getString("JSONStr"));
                    String username = jobj.getString("username");
                    ResultSet resSet = null;
                    boolean ifExists = true;
                    JSONArray jarray = new JSONArray();
                    Date date = DateUtils.addHours(new Date(), -7);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String dString = dateFormat.format(date);
                    System.out.println("USERNAME: " + username);
                    this.filter(this.read(jobj.getString("baths"), jobj.getString("beds"), username, jobj.getString("price"), jobj.getString("minprice"), jobj.getString("city"), jobj.getString("state")).listIterator(), ((double) Integer.parseInt(jobj.getString("filterrent"))) / 10000);
                    String check = "SELECT EXISTS (SELECT * FROM Output WHERE email = \"" + username + "\") as result;";
                    Statement checkStmt = conn.createStatement();
                    resSet = checkStmt.executeQuery(check);
                    resSet.next();
                    if (resSet.getString("result").equals("0")) {
                        ifExists = false;
                    } else {
                        try {
                            check = "SELECT * FROM Output WHERE email = \"" + username + "\";";
                            checkStmt = conn.createStatement();
                            resSet = checkStmt.executeQuery(check);
                            resSet.next();
                        } catch (SQLException e) {
                            System.out.println(e);
                        }
                        jarr = new JSONArray(resSet.getString("JSONStr"));
                        ifExists = true;
                    }
                    ListIterator iter = filterHome.listIterator();
                    ListIterator zpIter = zpList.listIterator();
                    while (iter.hasNext()) {
                        JSONObject add = new JSONObject();
                        add.put("zpid",zpIter.next());
                        add.put("home",iter.next());
                        jarray.put(add);
                    }
                    jsonobj.put("Day", dString);
                    jsonobj.put("homes", jarray);
                    jarr.put(jsonobj);
                    while (jarr.length() > 7) {
                        jarr.remove(0);
                    }
                    String jString = jarr.toString().replace("\"", "\\\"");
                    if (ifExists) {
                        check = "UPDATE Output SET JSONStr = \"" + jString + "\" WHERE email = \"" + username + "\"";
                        checkStmt = conn.createStatement();
                        checkStmt.execute(check);
                    } else {
                        check = "INSERT INTO Output VALUES(\"" + username + "\",\"" + jString + "\")";
                        checkStmt = conn.createStatement();
                        checkStmt.execute(check);
                    }
                    filterHome = new LinkedList<>();
                    zpList = new LinkedList<>();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    filterHome = new LinkedList<>();
                    zpList = new LinkedList<>();
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}