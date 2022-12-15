package com.planview.replicator;

/**
 * Field names are precusor-ed with src or dst as column in spreadsheet
 */
public class Access {
	public String Url;
	public String BoardName;
	public String ApiKey;

	public Access(){

	}
	
	public Access( String url, String boardname, String apikey) {
		Url = url;
		BoardName = boardname;
		ApiKey = apikey;
	}
}
