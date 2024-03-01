package com.henrywat.cic.svr.servlet;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

@WebServlet(name="DeleteCICPhoto", urlPatterns={"/DeleteCICPhoto"}, description = "Servlet to delete photo by insp type and case id")
public class DeleteCICPhoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    Connection conn = null;            
    PreparedStatement stmt = null;
    String sql = null;

    public DeleteCICPhoto() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	String logHead = timestamp.toString()+" - DeleteCICPhoto -> ";
    	String type = request.getParameter("type");
    	String photoId = request.getParameter("photoid");
    	PrintWriter out = response.getWriter();
    	String svr_response = "*Server: not logged in";
    	
    	int CSObjectId = 0;
    	String photoPath = "";
    	String targetTable = "";
    	if (type.equals("before")) targetTable = "emms.emms.BEFORE_RECTIFICATION_PHOTO";
    		else targetTable = "emms.emms.AFTER_RECTIFICATION_PHOTO";
        
        response.setContentType("text/html;charset=UTF8");
        response.setHeader("Cache-control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "-1");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Content-Type", "application/json; charset=UTF8");

		String basePath = "\\\\05emmsfile\\fileserver05";
        
        try {      
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection(); 

            sql = "select [PATH], COMPLAINT_STATISTICSOBJECT_ID from "+targetTable+" where [OBJECT_ID]="+photoId;
            //System.out.println(sql);
           
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
           
            while(rs.next()){
            	photoPath = basePath + rs.getString(1);
            	CSObjectId = rs.getInt(2);
            	//System.out.println(photoPath+" / "+CSObjectId);
            	
            	File f = new File(photoPath);
       		 	if(f.delete()) {
       		 		System.out.println(logHead+"deleted -> " + photoPath);
       		 		sql = "delete from "+targetTable+" where [OBJECT_ID]="+photoId;
       		 		//System.out.println(sql);
       		 		Statement stmt2 = conn.createStatement();
       		 		stmt2.executeUpdate(sql);
       		 		
       		 		
       		 		sql = "select [OBJECT_ID] from "+targetTable+" where COMPLAINT_STATISTICSOBJECT_ID="+CSObjectId+" order by COMPLAINT_STATISTICSIndex";
       		 		//System.out.println(sql);
       		 		PreparedStatement stmt3 = conn.prepareStatement(sql);
       		 		ResultSet rs2 = stmt3.executeQuery();
       		 		int tempIdx = 0;
       		 		while(rs2.next()){
       		 			sql = "update "+targetTable+" set COMPLAINT_STATISTICSIndex="+tempIdx+" where [OBJECT_ID]="+rs2.getInt(1);
       		 			tempIdx ++;
       		 			//System.out.println(sql);
       		 			stmt2.executeUpdate(sql);
       		 		}
       		 		stmt2.close();
       		 		stmt3.close();
       		 	} else {
       		 		throw new FileNotFoundException("not exist -> " + photoPath);
       		 	}
            }                                                                         
            rs.close();
            svr_response = "Server: Deleted";
        } catch (NamingException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: internal error";
		} catch (SQLException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: database error";
		} catch (FileNotFoundException e) {
			svr_response = "*Server: file not found";
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
                }                                                             

                stmt = null;                                            
            }                                                        

            if (conn != null) {                                      
                try {                                                   
                    conn.close();                                          
                } catch (SQLException sqlex) {                          
                    // ignore -- as we can't do anything about it here     
                }                                                       

                conn = null;                                            
            }                                                        
        }

 
        out.println(svr_response);
        //out.println("*fjeklw;jfkelw;");
        out.close();
    }
}
