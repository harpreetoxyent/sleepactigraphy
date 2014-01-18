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

	public void saveDataInMongo(DBCollection collection, String data) 
	{
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
		} 
		else 
		{
			index = data.indexOf(",_");
			subjectID = data.substring(index + 2, index + 19);
			System.out.println("Inside else loop of receiving data at server subject id while closing app--------"
					+ subjectID);			
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
			BasicDBObject epochDataX = new BasicDBObject();
			BasicDBObject epochDataY = new BasicDBObject();
			BasicDBObject epochDataZ = new BasicDBObject();
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
				epochDataX.put("" + i, indValues);
				System.out.println("inserting in list x====" + indValues);
			}
			document.put("X-Axis", epochDataX);
			// epochData.clear();

			st = 0;
			end = 0;
			for (int i = 0; i < 10; i++) {
				st = yAxis.indexOf(".", end);
				end = yAxis.indexOf(".", st + 1);

				if (end != -1)
					indValues = yAxis.substring(st - 1, end - 2);
				else
					indValues = yAxis.substring(st - 1);
				epochDataY.put("" + i, indValues);
				System.out.println("inserting in list y====" + indValues);
			}
			document.put("Y-Axis", epochDataY);
			// epochData.clear();

			st = 0;
			end = 0;
			for (int i = 0; i < 10; i++) {
				st = zAxis.indexOf(".", end);
				end = zAxis.indexOf(".", st + 1);

				if (end != -1)
					indValues = zAxis.substring(st - 1, end - 2);
				else
					indValues = zAxis.substring(st - 1);
				epochDataZ.put("" + i, indValues);
			}
			document.put("Z-Axis", epochDataZ);
			// epochData.clear();

			collection.insert(document);
		}
		getDataFromMongo(collection, subjectID);
	}

	ArrayList<Integer> list1 = new ArrayList<Integer>();
	ArrayList<Double> listX = new ArrayList<Double>();
	ArrayList<Double> listY = new ArrayList<Double>();
	ArrayList<Double> listZ = new ArrayList<Double>();
	ArrayList<Double> listSum = new ArrayList<Double>();

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
				if(x.has(String.valueOf(i)))
				{
					list1.add((count * 10) + i);
					listX.add(Double.parseDouble(x.getString(String.valueOf(i))));
					System.out.println("adding in x...value..."
							+ Double.parseDouble(x.getString(String.valueOf(i))));

				}
				if(y.has(String.valueOf(i)))
				{
					listY.add(Double.parseDouble(y.getString(String.valueOf(i))));
					System.out.println("adding in y...value..."
							+ Double.parseDouble(y.getString(String.valueOf(i))));
				}
				if(z.has(String.valueOf(i)))
				{
					listZ.add(Double.parseDouble(z.getString(String.valueOf(i))));
					System.out.println("adding in z...value..."
							+ Double.parseDouble(z.getString(String.valueOf(i))));
					
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getDataFromMongo(DBCollection collection, String subjectID) {
		System.out.println("collection is " + collection + " subjectID is "
				+ subjectID);
		BasicDBObject findData = new BasicDBObject();
		findData.put("subjectID", subjectID.trim());
		DBCursor cursor = collection.find(findData);
		System.out.println("cursor size " + cursor.size());
		int count = 0;
		String data = "";
		while (cursor.hasNext()) 
		{
			data = cursor.next().toString();
			System.out.println("from mongo db data is " + data+"--list1 size="+list1.size());
			addDataToListFromJSON(count, data);
			for (int i = 0; i < 10; i++) 
			{
				if(list1.size() > count * 10 + i)
				{
					System.out.println(list1.get(count * 10 + i) + " "
						+ listX.get(count * 10 + i) + " "
						+ listY.get(count * 10 + i) + " "
						+ listZ.get(count * 10 + i));
				}
			}

			count++;
		}
		System.out.println("count is " + count + " -------------------");

		// send list1 and list2 to plotData program
		if (STOP == true) {

			for (int i = 0; i < count * 10; i++) {
				listSum.add(listX.get(i) + listY.get(i) + listZ.get(i));
			}

			// pd.drawGraph(list1, listSum, "Sleep_graph");
			insertAggregatedDataInMongoDB(collection, subjectID, count);
			removeDuplicatesFromMongoDB(collection, subjectID);
			determinePS(listSum, subjectID);
		}

	}

	ArrayList<Double> sleep = new ArrayList<Double>();
	double PS = 0.0;
	int currEpochIndex = 0;

	public void determinePS(ArrayList<Double> list, String subjectID) {
		ArrayList<Double> meanList = new ArrayList<Double>();
		ArrayList<Double> listNAT = new ArrayList<Double>();
		ArrayList<Double> ListSDList = new ArrayList<Double>();
		ArrayList<Double> logList = new ArrayList<Double>();
		// ArrayList<Double> sumList = new ArrayList<Double>();
		boolean calculatePS = false;
		if (list.size() > 10) {
			calculatePS = true;
			while (currEpochIndex < 6) {
				currEpochIndex++;
				sleep.add(0.0);
			}
		}
		int checkAllItems = list.size()-6;
		while (checkAllItems > 11 && calculatePS == true) {
			checkAllItems--;
			int count = currEpochIndex+5;
			while (count > currEpochIndex - 6) {
				meanList.add(listSum.get(count));
				count--;
			}

			count = currEpochIndex +5;
			while (count > currEpochIndex - 6) {
				listNAT.add(listSum.get(count));
				count--;
			}
			
			count = currEpochIndex;
			while (count > currEpochIndex - 6) {
				ListSDList.add(listSum.get(count));
				count--;
			}
			
		
			PS = 7.601 - (0.065 * meanOfEpochs(meanList))
					- (1.08 * calculateNAT(listNAT))
					- (0.056 * findSDofLastSixMins(ListSDList))
					- (0.703 * LogOfNATValue(calculateNAT(listNAT)+1.0));
			System.out.println("----PS is -----" + PS);
			sleep.add(PS);
			currEpochIndex++;
		}
		PlotDataUsingjfree pd = new PlotDataUsingjfree(subjectID);
		pd.drawGraph(list1, listX, "X_AXIS");
		pd.drawGraph(list1, listY, "Y_AXIS");
		pd.drawGraph(list1, listZ, "Z_AXIS");
		pd.drawGraph(list1, sleep, "Sleep_graph");
	}

	public double meanOfEpochs(ArrayList<Double> list) {
		double sum = 0.0;
		int len = list.size();
		for (int i = 0; i < len; i++)
			sum += list.get(i);
		return (sum / list.size());
	}

	double threshold = 0.2;

	public int calculateNAT(ArrayList<Double> list) {
		int count = 0;

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) >= threshold)
				count++;
		}
		return count;
	}

	public double findSDofLastSixMins(ArrayList<Double> list) {
		double sd = 0.0;
		int sum = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) >= threshold)
				sum++;
		}
		double mean = sum / list.size();
		double sumForSD = 0.0;
		for (int i = 0; i < list.size(); i++)
			sumForSD += Math.sqrt(list.get(i) - mean);
		sd = Math.sqrt(sumForSD / list.size());
		return sd;
	}

	public double LogOfNATValue(double value) {
			return Math.log(value);
	}

	public void insertAggregatedDataInMongoDB(DBCollection collection,
			String subjectID, int count) {
		BasicDBObject document = new BasicDBObject();
		// Instantiate a Date object
		Date date = new Date();
		document.put("Aggregate", "true");
		document.put("subjectID", subjectID);
		document.put("time", date);
		document.put("epoch duration", "30 sec");
		BasicDBObject epochData = new BasicDBObject();

		System.out.println("in aggregae data--- size of list--X: "
				+ listX.size());
		for (int i = 0; i < count * 10; i++) {
			epochData.put("" + i, listX.get(i));
		}
		document.put("X-Axis", epochData);
		epochData.clear();
		System.out.println("in aggregae data--- size of list--Y: "
				+ listY.size());
		for (int i = 0; i < count * 10; i++) {
			epochData.put("" + i, listY.get(i));
		}
		document.put("Y-Axis", epochData);
		epochData.clear();
		System.out.println("in aggregae data--- size of list--Z: "
				+ listZ.size());
		for (int i = 0; i < count * 10; i++) {
			epochData.put("" + i, listZ.get(i));
		}
		document.put("Z-Axis", epochData);
		// epochData.clear();
		collection.insert(document);

	}

	public void removeDuplicatesFromMongoDB(DBCollection collection,
			String subjectID) {
		BasicDBObject findData = new BasicDBObject();
		findData.put("Aggregate", new BasicDBObject("$ne", "true"));
		DBCursor cursor = collection.find(findData);
		System.out.println("cursor size " + cursor.size());
		while (cursor.hasNext()) {
			collection.remove(cursor.next());
		}

	}
}