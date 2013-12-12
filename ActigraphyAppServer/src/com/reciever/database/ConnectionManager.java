package com.reciever.database;

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

	boolean STOP = false;

	public void saveDataInMongo(DBCollection collection, String data) {
		BasicDBObject document = new BasicDBObject();
		String subjectID = null;

		int index = 0;
		if (data.charAt(0) == '_' || data.startsWith("_")) {
			System.out
					.println("In connection manager....Client has finished sending data-----------------------");
			subjectID = data.substring(1);
			System.out.println("subject id while closing app--------"
					+ subjectID);
			STOP = true;
		} else {
			index = data.indexOf(",_");
			subjectID = data.substring(index + 2, index + 19);
			data = data.substring(0, index - 1);
		}

		if (STOP == false) {
			// Instantiate a Date object
			Date date = new Date();
			System.out.println("length of data is " + data.length());
			System.out.println("subjectID is" + subjectID);
			/* System.out.println("data is" + data); */
			document.put("subjectID", subjectID);
			document.put("time", date);
			document.put("epoch duration", "30 sec");
			BasicDBObject epochData = new BasicDBObject();
			int startPos = data.indexOf(",");
			int endPos = 0;
			String xAxis = data.substring(0, startPos);
			System.out.println("xaxis data == " + xAxis);
			endPos = startPos;
			startPos = data.indexOf(",", endPos + 1);
			String yAxis = data.substring(endPos + 1, startPos);
			System.out.println("yaxis data == " + yAxis);
			endPos = startPos;
			// startPos = data.indexOf(",", endPos+1);
			String zAxis = data.substring(endPos + 1);
			System.out.println("zaxis data == " + zAxis);
			String indValues;

			int st = 0, end = 0;
			for (int i = 0; i < 10; i++) {
				st = xAxis.indexOf(".", end);
				end = xAxis.indexOf(".", st + 1);

				if (end != -1)
					indValues = xAxis.substring(st - 1, end - 2);
				else
					indValues = xAxis.substring(st - 1);
				epochData.put("" + i, indValues);
			}
			document.put("X-Axis", epochData);
			epochData.clear();

			st = 0;
			end = 0;
			for (int i = 0; i < 10; i++) {
				st = yAxis.indexOf(".", end);
				end = yAxis.indexOf(".", st + 1);

				if (end != -1)
					indValues = yAxis.substring(st - 1, end - 2);
				else
					indValues = yAxis.substring(st - 1);
				epochData.put("" + i, indValues);
			}
			document.put("Y-Axis", epochData);
			epochData.clear();

			st = 0;
			end = 0;
			for (int i = 0; i < 10; i++) {
				st = zAxis.indexOf(".", end);
				end = zAxis.indexOf(".", st + 1);

				if (end != -1)
					indValues = zAxis.substring(st - 1, end - 2);
				else
					indValues = zAxis.substring(st - 1);
				epochData.put("" + i, indValues);
			}
			document.put("Z-Axis", epochData);
			// epochData.clear();

			collection.insert(document);
		}
		getDataFromMongo(collection, subjectID);
	}

	ArrayList<Integer> list1 = new ArrayList<Integer>();
	ArrayList<Double> listX = new ArrayList<Double>();
	ArrayList<Double> listY = new ArrayList<Double>();
	ArrayList<Double> listZ = new ArrayList<Double>();

	
	public void addDataToListFromJSON(int count, String data) {
		JSONObject obj = null;
		try {

			obj = new JSONObject(data);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject x = null, y = null, z = null;
		try {

			x = obj.getJSONObject("X-Axis");
			y = obj.getJSONObject("Y-Axis");
			z = obj.getJSONObject("Z-Axis");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

			for (int i = 0; i < 10; i++) {
				list1.add((count * 10) + i);
				listX.add(Double.parseDouble(x.getString(String.valueOf(i))));
				listY.add(Double.parseDouble(y.getString(String.valueOf(i))));
				listZ.add(Double.parseDouble(z.getString(String.valueOf(i))));
			}
			

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getDataFromMongo(DBCollection collection, String subjectID) {
System.out.println("collection is "+collection+" subjectID is "+subjectID);
		BasicDBObject findData = new BasicDBObject();
		findData.put("subjectID", subjectID.trim());
		DBCursor cursor = collection.find(findData);
		System.out.println("cursor size "+cursor.size());
		int count = 0;
		String data = "";
		while (cursor.hasNext()) {
			data = cursor.next().toString();
			System.out.println("from mongo db data is " + data);
			addDataToListFromJSON(count, data);
			
			/*for (int i = 0; i < 10; i++) {
				System.out.println(list1.get(count*10+i)+" "+listX.get(count*10+i)+" "+listY.get(count*10+i)+" "+listZ.get(count*10+i));
			}*/
			count++;
		}
		System.out.println("count is " + count + " -------------------");

		// send list1 and list2 to plotData program
		if (STOP == true) {
			PlotDataUsingjfree pd = new PlotDataUsingjfree(subjectID);
			pd.drawGraph(list1, listX, "X_AXIS");
			pd.drawGraph(list1, listY, "Y_AXIS");
			pd.drawGraph(list1, listZ, "Z_AXIS");
			insertAggregatedDataInMongoDB(collection, subjectID, count);
		}
	
	}
	
	public void insertAggregatedDataInMongoDB(DBCollection collection, String subjectID, int count)
	{
		BasicDBObject document = new BasicDBObject();
		// Instantiate a Date object
		Date date = new Date();
		document.put("subjectID", subjectID);
		document.put("time", date);
		document.put("epoch duration", "30 sec");
		BasicDBObject epochData = new BasicDBObject();

		System.out.println("in aggregae data--- size of list--X: "+listX.size() );
		for (int i = 0; i < count*10; i++) {
			epochData.put("" + i, listX.get(i));
		}
		document.put("X-Axis", epochData);
		epochData.clear();
		System.out.println("in aggregae data--- size of list--Y: "+listY.size() );
		for (int i = 0; i < listY.size(); i++) {
			epochData.put("" + i, listY.get(i));
		}
		document.put("Y-Axis", epochData);
		epochData.clear();
		System.out.println("in aggregae data--- size of list--Z: "+listZ.size() );
		for (int i = 0; i < listZ.size(); i++) {
			epochData.put("" + i, listZ.get(i));
		}
		document.put("Z-Axis", epochData);
		//epochData.clear();
		collection.insert(document);
	
	}
}