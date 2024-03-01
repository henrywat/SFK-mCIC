package com.henrywat.cic.svr.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONObject;

//import com.google.android.gcm.server.Sender;
import com.henrywat.cic.svr.Exception.*;


@WebServlet(name="ReceiveCICPhoto", urlPatterns={"/ReceiveCICPhoto"}, description = "Servlet to receive photo by request code and case id")
public class ReceiveCICPhoto extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	Connection conn = null;            
    PreparedStatement stmt = null;     
    String sql = null;
    
    private static final String SERVIDOR_SMTP = "mail.hostedexchange.asia";
    private static final int PORTA_SERVIDOR_SMTP = 587;
    private static final String CONTA_PADRAO = "complaint@sfk05hy2017.com.hk";
    private static final String SENHA_CONTA_PADRAO = "0517@comp";
    
    //final String gmail_username = "complaint.sfk@gmail.com";
    final String gmail_username = "complaint.sfk@yahoo.com";
    //final String gmail_password = "0517@2153sfk";
    final String gmail_password = "pnxiczqocdbcimag";

	public ReceiveCICPhoto() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8"); 
		response.setContentType ("image/jpeg;charset=utf-8");

		//boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
		PrintWriter out = response.getWriter();
		
		String id = request.getParameter("id");
		String requestCode = request.getParameter("requestCode");
		String image = request.getParameter("image");
		String caseNo = "";
		String notifyTitle = "";
		String svr_response = "*Server: not logged in";
		String adminMessage = id+"/"+requestCode;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	String logHead = timestamp.toString()+" - ReceiveCICPhoto -> csid="+id+" -> ";
		System.out.println(logHead+"requestCode = "+requestCode);
		
		String basePath = "\\\\05emmsfile\\fileserver05";
		String subPath = "\\";
		try {
			byte[] data;
			
			try {
				data = Base64.decodeBase64(image);
			} catch (Exception e) {
				throw new Base64Exception("broken image");
			} finally {
				adminMessage += " broken image";
			}
			
        		Context ctx = (Context) new InitialContext().lookup("java:comp/env");
        		conn = ((DataSource) ctx.lookup( "jdbc/CIC" )).getConnection();
        		//sql = "select cast(datepart(yyyy,CASE_REF_DATE) as varchar(4)) + " +
            //		 "'\\'+cast(datepart(yyyy,CASE_REF_DATE) as varchar(4))+'-'+LEFT(CONVERT(CHAR(20), CASE_REF_DATE, 101), 2) + " +
            //		 "'\\' + CASE_NO, CASE_NO from emms.emms.COMPLAINT_STATISTICS where [OBJECT_ID]=" + id;
        		sql = "select cast(datepart(yyyy,CASE_REF_DATE) as varchar(4)), " + 
        				"cast(datepart(yyyy,CASE_REF_DATE) as varchar(4))+'-'+LEFT(CONVERT(CHAR(20), CASE_REF_DATE, 101), 2), " + 
        				"CASE_NO " + 
        				"from emms.emms.COMPLAINT_STATISTICS " + 
        				"where [OBJECT_ID]="+id;
        	 
        		stmt = conn.prepareStatement(sql);
        		//System.out.println(logHead+"sql: " + sql);
        		
            ResultSet rs = stmt.executeQuery();
        	 	while(rs.next()) {
        	 		subPath += rs.getString(1);
        	 		File f = new File(basePath+subPath);
        	 		if(!f.exists()) {
        	 			System.out.println(logHead+"Create Year Directory: " + f.mkdir() + " "+basePath+subPath);
        	 		} else {
        	 			//System.out.println(logHead+"Year Directory Exists: " + f.mkdir() + " "+basePath+subPath);
        	 		}
        	 		
        	 		subPath += "\\";
        	 		subPath += rs.getString(2);
        	 		f = new File(basePath+subPath);
        	 		if(!f.exists()) {
        	 			System.out.println(logHead+"Create Month Directory: " + f.mkdir() + " "+basePath+subPath);
        	 		} else {
        	 			//System.out.println(logHead+"Month Directory Exists: " + f.mkdir() + " "+basePath+subPath);
        	 		}
        	 		
        	 		caseNo = rs.getString(3);
        	 		subPath += "\\";
        	 		subPath += caseNo;
        	 		f = new File(basePath+subPath);
        	 		if(!f.exists()) {
        	 			System.out.println(logHead+"Create Case Directory: " + f.mkdir() + " "+basePath+subPath);
        	 		} else {
        	 			//System.out.println(logHead+"Case Directory Exists: " + f.mkdir() + " "+basePath+subPath);
        	 		}
        	 	}
        	 
        	 	int maxPhotoIndex = -1;
        	 	int maxPhotoNumber = -1;
        	 	if (requestCode.equals("100")) {
        	 		sql = "select PHOTO_ID, COMPLAINT_STATISTICSIndex from emms.emms.BEFORE_RECTIFICATION_PHOTO where COMPLAINT_STATISTICSOBJECT_ID="+id+" order by COMPLAINT_STATISTICSIndex;";
        	 		notifyTitle = "Before Photo Uploaded";
        	 	} else {
        	 		sql = "select PHOTO_ID, COMPLAINT_STATISTICSIndex from emms.emms.AFTER_RECTIFICATION_PHOTO where COMPLAINT_STATISTICSOBJECT_ID="+id+" order by COMPLAINT_STATISTICSIndex;";
        	 		notifyTitle = "After Photo Uploaded";
        	 	}
        	 
        	 	stmt = conn.prepareStatement(sql);
        	 	rs = stmt.executeQuery();
        	 
        	 	int itmp = 0;
        	 	String stmp;
        	 	while (rs.next()) {
        	 		stmp = rs.getString(1);
        	 		stmp = stmp.substring(stmp.length()-4);
        	 		itmp = Integer.parseInt(stmp);
        	 		if (maxPhotoNumber< itmp) maxPhotoNumber = itmp;
        		 
        	 		itmp = rs.getInt(2);
        	 		if (maxPhotoIndex<itmp) maxPhotoIndex = itmp;
        	 	}
        	 	//System.out.println(logHead+"Max photo Index: "+ maxPhotoIndex + " \\ Max Photo Number = "+ maxPhotoNumber);
        	 

        	 	if (maxPhotoIndex<0) maxPhotoIndex=0; else maxPhotoIndex++;
        	 	if (maxPhotoNumber<0) maxPhotoNumber=0; else maxPhotoNumber++;
        	 
        	 	String newPhotoName;
    	 
        	 	if (requestCode.equals("100")) {
        	 		newPhotoName = "BR-"+String.format("%04d", maxPhotoNumber);
        	 		sql = "insert into emms.emms.BEFORE_RECTIFICATION_PHOTO([PATH], [DESCRIPTION], PHOTO_ID, COMPLAINT_STATISTICSOBJECT_ID, COMPLAINT_STATISTICSIndex) " +
        				 "values('"+subPath+"\\"+newPhotoName+".jpg','','"+newPhotoName+"',"+id+","+maxPhotoIndex+");";
        	 	} else {
        	 		newPhotoName = "AR-"+String.format("%04d", maxPhotoNumber);
        	 		sql = "insert into emms.emms.AFTER_RECTIFICATION_PHOTO([PATH], [DESCRIPTION], PHOTO_ID, COMPLAINT_STATISTICSOBJECT_ID, COMPLAINT_STATISTICSIndex) " +
        	 			"values('"+subPath+"\\"+newPhotoName+".jpg','','"+newPhotoName+"',"+id+","+maxPhotoIndex+");";
        	 	}
        	 
        	 	//System.out.println(sql);
        	 	//System.out.println(logHead+"New photo Index: "+ maxPhotoIndex + " \\ New Photo Name = "+ newPhotoName);
        	 
        	 	subPath = subPath + "\\"+newPhotoName+".jpg";
        	 	System.out.println(logHead+"Location: "+basePath+subPath);
   			try {
   				OutputStream stream = new FileOutputStream(basePath+subPath);
   				//OutputStream stream = new FileOutputStream("c:\\temp\\abc.jpg");
   				stream.write(data);
   				stream.flush();
   				stream.close();
   			} catch (IOException e) {
   				System.out.println(logHead+e);
   				throw new FileServerException("save image error");
   			} finally {
   				adminMessage += " broken image";
   			}
        	 
        		stmt = conn.prepareStatement(sql);
        		System.out.println(logHead+"Insert DB:"+stmt.execute());
        	 
        	 /*
        	 try {
        	 	String to = "complaint@sfk05hy2017.com.hk";
        	 	String from = "sys_admin@sfk05hy2013.com.hk";
        	 	String host = "mail.hostedexchange.asia";
        	 	String port = "587";
        	 	//String password = "0517@2153sfk";
             Properties properties = System.getProperties();
             properties.setProperty("mail.smtp.host", host);
             properties.setProperty("mail.smtp.port", port);
             //properties.setProperty("mail.smtp.auth", "true");
             //properties.setProperty("mail.smtp.starttls.enable", "true");
        
             Session session = Session.getDefaultInstance(properties);
        	 	 MimeMessage message = new MimeMessage(session);
        	 	 message.setFrom(new InternetAddress(from));
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
             if (requestCode.equals("100"))
            	 message.setSubject(caseNo + " : Before Rect. Photo Uploaded by mobile CIC.");
             else
            	 message.setSubject(caseNo + ": After Rect. Photo Uploaded by mobile CIC ");
             message.setContent("http://www.emms.sfk05hy2017.com.hk/emmsweb/complaintStatistics.jsf?cs="+id+"&autoFocus=true","text/html" );
             System.out.println(logHead+"Sending email to "+to+" smtp:"+host+":"+port);
             Transport.send(message);
        	 } catch (AddressException e) {
     			// TODO Auto-generated catch block
     			//e.printStackTrace();
     			System.out.println(logHead+e);
     		} catch (MessagingException e) {
     			// TODO Auto-generated catch block
     			//e.printStackTrace();
     			System.out.println(logHead+e);
     		}*/
        	 
        		String mailFrom = "sys_admin@sfk05hy2017.com.hk";
        		String mailTo = "complaint@sfk05hy2017.com.hk";
        		String mailSubject = "";
        		String mailMessage = "";
        		if (requestCode.equals("100"))
        			mailSubject = caseNo + " : Before Rect. Photo Uploaded by mobile CIC. (DO NOT REPLY)";
        		else
        			mailSubject = caseNo + ": After Rect. Photo Uploaded by mobile CIC.  (DO NOT REPLY)";
        	  
        		mailMessage = "http://www.emms.sfk05hy2017.com.hk/emmsweb/complaintStatistics.jsf?cs="+id+"&autoFocus=true";
    			System.out.println(logHead+"Sending photo upload Gmail, csid=<"+id+">");
    			//sendEmail(mailFrom, mailTo, mailSubject, mailMessage);
    			sendGmail(mailSubject, mailMessage);
             
            sql = "select u.uid, g.device_id from Northwind..mCIC_user u, Northwind..mCIC_user_gcm g " + 
             		"where g.uid = u.uid " + 
             		"and (u.username like 'cic%' or u.username like 'henry%')";
            //String GOOGLE_SERVER_KEY = "AIzaSyA_cdge37PdDCBczpplW7DtpGUun8rxd1w";
            String authKey = "AAAAOep89X0:APA91bFaR8NLW2sH0l86Dg52Um61YP7sEikdqUJV3UdtT96in-pySTI-A750AItR4iVKXIQ0qJfjl6gHqC2P4lvuOkz-htYQb-9KNN2owDQZscdtk-TEwGu8LSxibUBcsCIlbCU4N38j";
            String FMCurl = "https://fcm.googleapis.com/fcm/send";
             
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
            	//Sender sender = new Sender(GOOGLE_SERVER_KEY);
            	//com.google.android.gcm.server.Message mess = new com.google.android.gcm.server.Message.Builder()//.timeToLive(120) default 4 weeks
            	//		.delayWhileIdle(false)
                //        .addData("title", notifyTitle)
                //        .addData("message", caseNo)
                //        .addData("csid", ""+id)
                //        .build();
                 /*try {
                	 System.out.println(logHead+notifyTitle+"-> uid<"+rs.getInt(1)+" -> result:"+sender.send(mess, rs.getString(2), 1));
                	 //System.out.println(logHead+sender.send(mess, rs.getString(2), 1));
                 } catch (IOException e) {
                	 System.out.println(logHead+e);
                	 throw new MailException("send GCM error");
                 }*/
            	try {
                	URL url = new URL(FMCurl);
                	
                	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        	        
                	conn.setUseCaches(false);
                	conn.setDoInput(true);
                	conn.setDoOutput(true);

                	conn.setRequestMethod("POST");
                	conn.setRequestProperty("Authorization", "key=" + authKey);
                	conn.setRequestProperty("Content-Type", "application/json");
                	conn.setRequestProperty("Accept-Charset", "UTF-8");
                	
                	JSONObject dataNotify = new JSONObject();
                	dataNotify.put("to", rs.getString(2));
                
                	JSONObject dataPayload = new JSONObject();
                	dataPayload.put("csid", id); // Notification title
                	dataPayload.put("title", notifyTitle); // Notification title
                	dataPayload.put("body", caseNo); // Notification body
                	dataPayload.put("sound","default");
                
                	dataNotify.put("data", dataPayload);
                        	
                	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                	wr.write(dataNotify.toString());
                	wr.flush();
                	wr.close();
                	
                	int responseCode = conn.getResponseCode();
                	System.out.println(logHead + "uid=,"+rs.getInt(1) + " Response Code : " + responseCode);
                	
                	conn.disconnect();
                
                	//BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                	//String inputLine;

                	//while ((inputLine = in.readLine()) != null) {
                	//    response.append(inputLine);
                	//}
                	//in.close();
                } catch (Exception e) {
                	System.out.println(logHead+ e.getMessage());
                }
            }
            rs.close();
            
            
            svr_response = "Server: upload success";
		} catch (SQLException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: database error";
		} catch (NamingException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: internal error";
		} catch (MessagingException e) {
			System.out.println(logHead+e);
			svr_response = "*Server: send email error";
		} catch (Base64Exception e) {
			System.out.println(logHead+e);
			svr_response = "*Server:" +e.getMessage();
		} catch (FileServerException e) {
			System.out.println(logHead+e);
			svr_response = "*Server:" +e.getMessage();
		//} catch (MailException e) {
		//	System.out.println(logHead+e);
		//	svr_response = "*Server:" +e.getMessage();
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
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
		System.out.println("Calling ReceiveCICPhoto servlet -- doGet");
		throw new ServletException("GET method used with " +
				getClass( ).getName( )+": POST method required.");
	}
	
	public void sendGmail(String subject, String messageContent) throws MessagingException {
		
		Properties prop = new Properties();
        //prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.host", "smtp.mail.yahoo.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        
        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(gmail_username, gmail_password);
                    }
                });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(gmail_username));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(CONTA_PADRAO)
        );
        message.setSubject(subject);
        message.setText(messageContent);

        Transport.send(message);
	}
	
	public void sendEmail(String from, String to, String subject, String messageContent) throws MessagingException {
        final Session session = Session.getInstance(this.getEmailProperties(), new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CONTA_PADRAO, SENHA_CONTA_PADRAO);
            }

        });

        //try {
            final Message message = new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.setText(messageContent);
            message.setSentDate(new Date());
            Transport.send(message);
        //} catch (final MessagingException ex) {
        //    LOGGER.log(Level.WARNING, "Erro ao enviar mensagem: " + ex.getMessage(), ex);
        //}
    }
	
	public Properties getEmailProperties() {
        final Properties config = new Properties();
        config.put("mail.smtp.auth", "true");
        config.put("mail.smtp.starttls.enable", "true");
        config.put("mail.smtp.host", SERVIDOR_SMTP);
        config.put("mail.smtp.port", PORTA_SERVIDOR_SMTP);
        config.put("mail.debug", "true");
        return config;
    }

}
