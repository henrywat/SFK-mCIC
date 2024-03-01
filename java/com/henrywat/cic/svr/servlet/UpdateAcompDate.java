package com.henrywat.cic.svr.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
import java.sql.PreparedStatement;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
//import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Calendar;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonArray;
//import com.henrywat.cic.svr.model.CaseList;

@WebServlet(name="UpdateAcompDate", urlPatterns={"/UpdateAcompDate"}, description = "Servlet to update Actural Completion Date of given id")

public class UpdateAcompDate extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    Connection conn = null;            
    PreparedStatement stmt = null;     
    String sql = null;

    public UpdateAcompDate() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	String logHead = timestamp.toString()+" - UpdateAcompDate -> ";
    	String csid = request.getParameter("id");
    	String aCompYear = request.getParameter("yr");
    	String aCompMonth = request.getParameter("mth");
    	String aCompDay = request.getParameter("day");
    	String aCompHour = request.getParameter("hr");
    	String aCompMinute = request.getParameter("min");
    	//UpdateAcompDate?id=97290&yr=2021&mth=4&day=31&hr=9&min=16
        
        response.setContentType("text/html;charset=UTF8");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Content-Type", "application/json; charset=UTF8");
        PrintWriter out = response.getWriter();
        String svr_response = "*Server: not logged in";

        
        try {
        	Calendar myCal = Calendar.getInstance();
            myCal.set(Calendar.YEAR, Integer.parseInt(aCompYear));
            myCal.set(Calendar.MONTH, Integer.parseInt(aCompMonth));
            myCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(aCompDay));
            myCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(aCompHour));
            myCal.set(Calendar.MINUTE, Integer.parseInt(aCompMinute));
        	java.util.Date utilDate = myCal.getTime();
        	java.sql.Timestamp sqlTime = new java.sql.Timestamp(utilDate.getTime());
        	java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
        	
        	sql = "update Northwind..CIC set ActCompDatetime=? where objectId=?";
        	
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection();
            stmt = conn.prepareStatement(sql);
      		stmt.setTimestamp(1, sqlTime);
      		stmt.setInt(2, Integer.parseInt(csid));
      		stmt.executeUpdate();
      		
      		sql="update emms.emms.COMPLAINT_STATISTICS set COMPLETION_DATE=? where COMP_CONT_NO LIKE '%05%2017%' AND [OBJECT_ID]=?";
      		stmt = conn.prepareStatement(sql);
      		stmt.setDate(1, sqlDate);
      		stmt.setInt(2, Integer.parseInt(csid));
      		stmt.executeUpdate();


            System.out.println(logHead+"UpdateAcompDate csid="+csid+", ACompDate="+utilDate.toString());
            

            svr_response = new String("Actual Comp Date is updated");
            
		} catch (NamingException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: internal error";
		} catch (SQLException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: database error";
        } finally {                                                       
            /*                                                             
             * close any jdbc instances here that weren't                  
             * explicitly closed during normal code path, so               
             * that we don't 'leak' resources...                           
             */                                                            

            if (stmt != null) {                                            
                try {                                                         
                    stmt.close();                                                
                } catch (SQLException sqlex) {                                
                    // ignore -- as we can't do anything about it here  
                		System.out.println(logHead+sqlex);
                }                                                             

                stmt = null;                                            
            }                                                        

            if (conn != null) {                                      
                try {                                                   
                    conn.close();                                          
                } catch (SQLException sqlex) {                          
                    // ignore -- as we can't do anything about it here     
                	System.out.println(logHead+sqlex);
                }                                                       

                conn = null;                                            
            }                                                        
        }      

        out.println(svr_response);

        out.close();
    }
}
