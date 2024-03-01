package com.henrywat.cic.svr.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
//import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.henrywat.cic.svr.model.PhotoList;

@WebServlet(name="GetCICPhotoList", urlPatterns={"/GetCICPhotoList"}, description = "Servlet to get the photo list of 'insp type' with case id")

public class GetCICPhotoList extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public GetCICPhotoList() {
		// TODO Auto-generated constructor stub
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	String logHead = timestamp.toString()+" - GetCICPhotoList -> ";
    	String type = request.getParameter("type");
    	String id = request.getParameter("id");
    	
    	if( (id != null && !id.equals("") && Integer.parseInt(id)>0)
    			&& (type.equals("insp") || type.equals("comp")) ){
    	
    	
    		//System.out.println("GetCICPhotos - type="+type+", id="+id);
    		PrintWriter out = response.getWriter();
    		String svr_response = "*Server: not logged in";
    	    	
    	ArrayList<PhotoList> photoList = new ArrayList<PhotoList>();
    	Connection conn = null;            
        PreparedStatement stmt = null;     
        String sql = null;
        
    	try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection(); 

            if (type.equals("insp"))
            	sql = "SELECT COMPLAINT_STATISTICSOBJECT_ID, OBJECT_ID FROM emms.emms.BEFORE_RECTIFICATION_PHOTO WHERE COMPLAINT_STATISTICSOBJECT_ID="+id+" ORDER BY COMPLAINT_STATISTICSIndex";
            else
            	sql = "SELECT COMPLAINT_STATISTICSOBJECT_ID, OBJECT_ID FROM emms.emms.AFTER_RECTIFICATION_PHOTO WHERE COMPLAINT_STATISTICSOBJECT_ID="+id+" ORDER BY COMPLAINT_STATISTICSIndex";
            //System.out.println(sql);
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(); 
           
            while(rs.next()){
            	PhotoList pl = new PhotoList();
            	pl.setId(rs.getString(1));
            	pl.setPhotoId(rs.getString(2));
            	photoList.add(pl);
            	//System.out.println(compStat.getDetail());
            }                                                                         

            rs.close();                                                               
            
        	Gson gson = new GsonBuilder().create();
            JsonArray jsArray = gson.toJsonTree(photoList).getAsJsonArray();
            svr_response = new String(jsArray.toString());
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
}
