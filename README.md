# SFK-mCIC @2014
<p>KLE Urban Call Center Android application developed on Android Studio under contract with the Highways Department of the Hong Kong SAR.</p>

## Technologies:
- Android Studio / Android SDK v28
- MS-SQL2012 / T-SQL
- Tomcat 8 / Struts2 / JSP / Java Servlet / JQuery / Jasper Report

---

## Objective:

<p>Shorten the communication time between 1823 center, urban call center and on-site civil agents. Once a case is received from the 1823, agents are notified with details through the android app once the case is entered tho web application. Then, the urban call center can reply to the 1823 as soon as possible after the agents replied and took photos of the scene. Just a click, a report in PDF is generated and ready to send.</p>

<p>During typhoon season, cases can exceed a thousand each day. Through this application, cases can be processed quickly and communication errors is eliminated.</p>

<p>I developed this appliction for my own interest.</p>

---

### Login Page:
<p>Once logged in, the device ID is registered to google cloud service in order to provide push notification once new case received.</p>

![Login page](/screens/login.png)

---

### Case List Page:
<p>This page displays the list of cases which is associated. Users is able to browse cases by selected desire district, case states and created in last <em>n</em> days.</p>

![Case List page](/screens/case_list.png)

---

### Case Detail Page:
<p>Display the number, creation date, district, streen name, detail of the selected case. Also agents are able to upload photos of before and after rectification.</p>
<p>Each action taken changes the case status and notifies the ubran call center.</p>

![Case Detail page](/screens/case_detail.png)

---

### PDF Report

<p>The system-generated PDF is used for reporting to 1823.</p>

![PDF 1st page](/screens/PDF_report/PDF_1.png)
![PDF 2st page](/screens/PDF_report/PDF_2.png)
![PDF 3st page](/screens/PDF_report/PDF_3.png)
