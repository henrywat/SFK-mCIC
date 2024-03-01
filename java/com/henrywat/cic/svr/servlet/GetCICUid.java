package com.henrywat.cic.svr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

@WebServlet(name="GetCICUid", urlPatterns={"/GetCICUid"}, description = "Servlet to get the uid of a user with pw")

public class GetCICUid extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public GetCICUid() {
		// TODO Auto-generated constructor stub
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	String logHead = timestamp.toString()+" - GetCICUid -> ";
    	String username = request.getParameter("u").trim().toLowerCase();
    	String password = request.getParameter("p").trim();
    	
    	if( (username != null && !username.equals(""))
    			&& (password != null && !password.equals("")) ){
    	
    	
    	//System.out.println("GetCICPhotos - type="+type+", id="+id);
    	PrintWriter out = response.getWriter();
    	
    	Connection conn = null;            
        PreparedStatement stmt = null;     
        String sql = null;
        
    	try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection(); 

            sql = "select uid from Northwind..mCIC_user where username='"+username+"' and password='"+password+"'";
            	
            //System.out.println(sql);
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(); 
           
            if (rs.next()){
            		out.print(rs.getInt(1));
            		System.out.println(logHead+"Login success username="+username);
            } else {
            		out.print("-1");
            		System.out.println(logHead+"Login fail username="+username);
            }

            rs.close();                                                               
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
