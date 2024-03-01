package com.henrywat.cic.svr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@WebServlet(name="SendGCMToken", urlPatterns={"/SendGCMToken"}, description = "Servlet to get user GCM token")

public class SendGCMToken extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public SendGCMToken() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }
	
protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	String logHead = timestamp.toString()+" - SendGCMToken -> ";
    	String uid = request.getParameter("uid").trim();
    	String token = request.getParameter("token").trim();
    	System.out.println(logHead+"uid="+uid+" token="+token.substring(1, 6));
    	
    	if( (uid != null && !uid.equals(""))
    			&& (token != null && !token.equals("")) ){
    	
    	
    	//System.out.println("GetCICPhotos - type="+type+", id="+id);
    	PrintWriter out = response.getWriter();
    	
    	Connection conn = null;            
        PreparedStatement stmt = null;     
        String sql = null;
        String status = "fail";
        
    	try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection(); 

            sql = "update Northwind..mCIC_user_gcm set device_id='"+token+"' where uid="+uid+";";
            	
            //System.out.println(sql);
            stmt = conn.prepareStatement(sql);
            
            if (stmt.executeUpdate()<1) {
            		sql = "insert into Northwind..mCIC_user_gcm(uid,device_id) values('"+uid+"','"+token+"');";
            		//System.out.println(sql);
            		PreparedStatement stmt2 = conn.prepareStatement(sql);
           
            		if (stmt2.executeUpdate()>0){
            			status = "success";
            			System.out.println(logHead+"Token registration success, uid="+uid);
            		} else {
            			System.out.println(logHead+"Token registration fail, uid="+uid);
            		}
            		//rs.close();
            		stmt2.close();
            } else {
            		System.out.println(logHead+"Token update success, uid="+uid);
            		status = "success";
            }
            
            out.print(status);
            stmt.close();                                                             
            stmt = null;                                                              


            conn.close();                                                             
            conn = null;
            
    	} catch(Exception e){System.out.println(logHead+e);}
    	finally {
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
    	out.close();
    	}
    }
}
