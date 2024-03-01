package com.henrywat.cic.svr.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

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
import com.henrywat.cic.svr.model.TeamList;

@WebServlet(name="GetCICTeamList", urlPatterns={"/GetCICTeamList"}, description = "Servlet to get the list of available team to a user")

public class GetCICTeamList extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    Connection conn = null;            
    PreparedStatement stmt = null;     
    String sql = null;

    public GetCICTeamList() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	String uid = request.getParameter("uid");

    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	String logHead = timestamp.toString()+" - GetCICTeamList -> ";
    	System.out.println(logHead+"uid="+uid);
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

        if (uid!=null) {
        ArrayList<TeamList> teamList = new ArrayList<TeamList>(); 
        
        try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection();
            

            sql = "select idx, team from Northwind..mcic_user_team where uid="+uid+" order by idx"; 
            //System.out.println("Get Team List uid="+uid);
           
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
           
            while(rs.next()) {
            TeamList team = new TeamList();
            team.setIdx(rs.getInt(1));
            team.setTeam(rs.getString(2));
            teamList.add(team);
            //System.out.println("idx:"+team.getIdx()+" team:"+team.getTeam());
            }

            rs.close();    
            Gson gson = new GsonBuilder().create();
            JsonArray jsArray = gson.toJsonTree(teamList).getAsJsonArray();
            //System.out.println(jsArray.toString());
     
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
        }
        out.close();
    }
}
