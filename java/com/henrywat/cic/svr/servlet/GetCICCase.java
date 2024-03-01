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
import com.henrywat.cic.svr.model.CaseList;

@WebServlet(name="GetCICCase", urlPatterns={"/GetCICCase"}, description = "Servlet to get the case detail by object_id")

public class GetCICCase extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    Connection conn = null;            
    PreparedStatement stmt = null;     
    String sql = null;

    public GetCICCase() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    		String logHead = timestamp.toString()+" - GetCICCase -> ";
    		String csid = request.getParameter("id").toString().trim();
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

        if (csid!=null && !csid.equals("")) {
        ArrayList<CaseList> compStatList = new ArrayList<CaseList>(); 
        
        try {      
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection();
            

            sql = "select CS.OBJECT_ID, CS.CASE_NO, " +
            		//"concat(convert(varchar, CI.caseRefDatetime , 103),' ',RIGHT(concat('0',datepart(hour, CI.caseRefDatetime)),2),':',RIGHT(concat('0',datepart(minute, CI.caseRefDatetime)),2)) as REF_DATE, " +
            		"CI.caseRefDateTime as datetime, " +
            		"TL.TEAM_ABB, CS.ROAD_NAME, CS.CASE_DETAIL, CI.caseType, "+
            		"CI.ActCompDatetime as actcompdatetime "+
            		"from emms.emms.COMPLAINT_STATISTICS CS "+
            		"LEFT OUTER JOIN emms.emms.LU_TEAM_LIST TL ON CS.TEAM_DISTRICT = TL.TEAM "+
            		"LEFT OUTER JOIN Northwind..CIC CI on CI.objectId = CS.[OBJECT_ID] " +
            		"WHERECS.COMP_CONT_NO LIKE '%05%2017%' AND CS.OBJECT_ID="+csid;
            //System.out.println(sql);
            		
            

            System.out.println(logHead+"Get Case object_id="+csid);
           
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            String temp;
           
            while(rs.next()){
            		int countInspPhoto =0;
            		int countCompPhoto = 0;
            	
            		// Counting Inspection Photo
            		sql = "SELECT COUNT(*) FROM EMMS.emms.BEFORE_RECTIFICATION_PHOTO WHERE COMPLAINT_STATISTICSOBJECT_ID="+rs.getInt(1);
            		//System.out.println(sql);
            		PreparedStatement stmt2 = conn.prepareStatement(sql);
            		ResultSet rs2 = stmt2.executeQuery();
            		if (rs2.next()) countInspPhoto = rs2.getInt(1);
            		//if (rs2.next()) compStat.setInspphoto(""+rs2.getInt(1));
            		rs2.close();
            		stmt2.close();
            		stmt2 = null;
            	
            		// Counting Completion Photo
            		sql = "SELECT COUNT(*) FROM EMMS.emms.AFTER_RECTIFICATION_PHOTO WHERE COMPLAINT_STATISTICSOBJECT_ID="+rs.getInt(1);
            		//System.out.println(sql);
            		PreparedStatement stmt3 = conn.prepareStatement(sql);
            		ResultSet rs3 = stmt3.executeQuery();
            		if (rs3.next()) countCompPhoto = rs3.getInt(1);
            		//if (rs3.next()) compStat.setCompphoto(""+rs3.getInt(1));
            		rs3.close();
            		stmt3.close();
            		stmt3 = null;
            		
            		/*
            		switch (filterPhoto) {
            			case "0": valid = true; break;
            			case "1": if (countInspPhoto == 0) valid = true; break;
            			case "2": if (countCompPhoto == 0) valid = true; break;
            			case "3": if (countInspPhoto == 0 || countCompPhoto == 0) valid = true; break;
            			case "4": if (countInspPhoto > 0 && countCompPhoto > 0) valid = true; break;
            		}*/
            		
            		if (true) {
            			CaseList compStat = new CaseList(); 
            			compStat.setId(rs.getInt(1));
            			compStat.setCaseno(rs.getString(2));
            			compStat.setCasedate(rs.getString(3));
            			temp =  rs.getString(4);
            			if (temp == null) compStat.setDistrict(""); else compStat.setDistrict(rs.getString(4));
            			compStat.setStreet(rs.getString(5));
            			compStat.setDetail(rs.getString(6));
            			compStat.setInspphoto(""+countInspPhoto);
            			compStat.setCompphoto(""+countCompPhoto);
            			compStat.setCaseType(rs.getString(7));
            			compStat.setColorFlag(0);
            			compStat.setAcompDate(rs.getString(8));
            			//compStat.setAcompDate("2021-4-1");
            			compStatList.add(compStat);
            		}
            		//System.out.println(compStat.getDetail());
            }                                                                         

            rs.close();    
            Gson gson = new GsonBuilder().create();
            JsonArray jsArray = gson.toJsonTree(compStatList).getAsJsonArray();
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

        /*
        Gson gson = new Gson();
        JsonArray arrayObj=new JsonArray();
        for(int i=0;i<compStatList.size();i++){

        	CaseList cs = compStatList.get(i);
            JsonElement countryObj = gson.toJsonTree(cs);   
            arrayObj.add(countryObj);
        }*/

        //create a new JSON object
        //JsonObject myObj = new JsonObject();
        //add property as success
        //myObj.addProperty("success", true);
        //add the countryList object
        //myObj.add("caseList", arrayObj);
        //convert the JSON to string and send back
        //out.println(myObj.toString());
        
        //System.out.println(jsArray.toString());
 
        
        }
        out.println(svr_response);
        out.close();
    }
}
