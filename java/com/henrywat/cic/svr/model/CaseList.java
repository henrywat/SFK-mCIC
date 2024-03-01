package com.henrywat.cic.svr.model;

public class CaseList {

	private int id;
	private String caseno;
	private String district;
	private String street;
	private String detail;
	private String inspphoto;
	private String compphoto;
	private String casedate;
	private String acompdate;
	private String casetype;
	private int colorflag;
	
	public CaseList() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCaseno() {
		return caseno;
	}

	public void setCaseno(String caseno) {
		this.caseno = caseno;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getInspphoto() {
		return inspphoto;
	}

	public void setInspphoto(String inspphoto) {
		this.inspphoto = inspphoto;
	}

	public String getCompphoto() {
		return compphoto;
	}

	public void setCompphoto(String compphoto) {
		this.compphoto = compphoto;
	}

	public String getCasedate() {
		return casedate;
	}

	public void setCasedate(String casedate) {
		this.casedate = casedate;
	}
	
	public String getCaseType() {
		return this.casetype;
	}
	
	public void setCaseType(String casetype) {
		this.casetype = casetype;
	}
	
	public int getFlag() {
		return this.colorflag;
	}
	
	public void setColorFlag(int colorflag) {
		this.colorflag = colorflag;
	}
	
	public String getAcompDate() {
		return acompdate;
	}

	public void setAcompDate(String acompdate) {
		this.acompdate = acompdate;
	}
	
}
