package com.reciever.database;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

//import au.com.bytecode.opencsv.CSVReader;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.reciever.plotdata.PlotDataUsingjfree;

//import com.oxyent.actigraphy.pojo.SubjectInfo;

public class ConnectionManager {

	private static MongoClient mongoClient = null;
 
	private static DB getMongoDB(String host, int port, String userName,
			String password, String databaseName) {
		DB db = null;
		try {
			if (mongoClient == null) {
				mongoClient = new MongoClient(host, port);
			}
			System.out.println("Inside mongo client is null");
			System.out.println("New Mongo created with [" + host + "] and ["
					+ port + "]");
			db = mongoClient.getDB(databaseName);
			boolean auth = db.authenticate(userName, password.toCharArray());
			if (auth) {
				return db;
			} else {
				db = null;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		System.out.println("returning db..");

		return db;
	}

	public DB getDatabaseAccess(String hostName, int port, String userName,
			String password, String databaseName) {
		DB mongoDB = null;
		try {
			mongoDB = getMongoDB(hostName, port, userName, password,
					databaseName);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		System.out.println("database access granted" + mongoDB);
		return mongoDB;
	}

	public void saveDataInMongo(DBCollection collection, String data) {
		BasicDBObject document = new BasicDBObject();

		// header variables
		String subjectID = null;
		// read headers
		int index = data.indexOf("_");
		System.out.println("index is ----- "+index);
		subjectID = data.substring(index+1, index+1+17);
		data = data.substring(0, index);
		
		 // Instantiate a Date object
	       Date date = new Date();
	        
		System.out.println("length of data is " + data.length());
		System.out.println("subjectID is" + subjectID);
	/*	System.out.println("data is" + data);*/
		document.put("subjectID", subjectID);
		document.put("time", date);
		document.put("epoch duration", "30 sec");
		BasicDBObject epochData = new BasicDBObject();
		String indValues;
		
		int st = 0, end =0;
		for (int i = 0; i < 10; i++) {
			st = data.indexOf(".", end);
			end = data.indexOf(".", st+1);
			
			if(end!=-1)
				indValues = data.substring(st-1, end - 2);			
			else
				indValues = data.substring(st-1);
			epochData.put("" + i , indValues);
		}
		
		document.put("epoch1", epochData);
		collection.insert(document);
		getDataFromMongo(collection, subjectID);
	}
	ArrayList<Integer> list1=new ArrayList<Integer>();
    ArrayList<Double> list2=new ArrayList<Double>();
    
  public void addDataToListFromJSON(int count, String data){
	  JSONObject obj = null;
		try {
			
			obj = new JSONObject(data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    JSONObject success = null;
		try {
			
			success = obj.getJSONObject("epoch1");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
	    
			for(int i=0;i<10;i++)
			{
				list1.add((count*10) + i);
				list2.add(Double.parseDouble(success.getString(String.valueOf(i))));
			}
			
			for(int i=0;i<10;i++)
				System.out.println(list2.get(i));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	public void getDataFromMongo(DBCollection collection, String subjectID) {
		 
		BasicDBObject findData = new BasicDBObject();
		findData.put("subjectID", subjectID);
		DBCursor cursor = collection.find(findData);
		int count = 0;
		String data = "";
		while(cursor.hasNext()) {
			data = cursor.next().toString();
		    System.out.println("from mongo db data is "+data);
		    addDataToListFromJSON(count,data);
		    count++;
		}
		System.out.println("count is "+count+" -------------------");
		
		
		// send list1 and list2 to plotData program
	    
		PlotDataUsingjfree pd = new PlotDataUsingjfree(list1,list2, subjectID);
	    pd.drawGraph();
	}
}