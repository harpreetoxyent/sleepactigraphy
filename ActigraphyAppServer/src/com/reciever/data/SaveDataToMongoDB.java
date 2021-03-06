package com.reciever.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.reciever.database.ConnectionManager;
import com.reciever.constants.ActigraphyServerConstants;
/**
 * Servlet implementation class SaveDataToMongoDB
 */
public class SaveDataToMongoDB extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */

	ConnectionManager cn = null;
	DB db = null;

	public SaveDataToMongoDB() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println("Servlet Init Method");
		cn = new ConnectionManager();
		db = cn.getDatabaseAccess(ActigraphyServerConstants.Mongo_Server_Address, 
				ActigraphyServerConstants.Mongo_Server_Port, 
				ActigraphyServerConstants.Mongo_UserName, 
				ActigraphyServerConstants.Mongo_Password,
				ActigraphyServerConstants.Mongo_Database_Name);
		System.out.println("Servlet Init Method ended db=" + db);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		int size = request.getContentLength();
		InputStream in = request.getInputStream();
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(
					request.getInputStream()));

			String line;
			while ((line = br.readLine()) != null) {
				if(line.charAt(0)=='_')
					System.out.println("at server... client pressed stop");
				try {

					DBCollection collection = db.getCollection(ActigraphyServerConstants.Mongo_Collection_Name);
					if (collection == null) {
						System.out.println("Error!");
					} else {
						System.out.println("saving data in mongo db");
						cn.saveDataInMongo(collection, line);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Done");
	}

}
