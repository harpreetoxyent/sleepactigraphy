package com.reciever.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class DisplayDataReceived
 */

public class DisplayDataReceived extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DisplayDataReceived() {
        super();
        System.out.println("starting...");
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	
	    PrintWriter out = response.getWriter();
	    int size=request.getContentLength();
	    InputStream in=request.getInputStream();
	    BufferedReader br = null;
		FileWriter writer = null;
		
	    try{ 
	        FileOutputStream fop = null;
	        File file;
	        file = new File("C://data.txt");
	        fop = new FileOutputStream(file);

	        // if file doesnt exists, then create it
	        if (!file.exists()) {
	            file.createNewFile();
	        }


	        // writing file created
	        
	        try {
				br = new BufferedReader(new InputStreamReader(
						request.getInputStream()));
				writer = new FileWriter("C://data.txt");
				String line;	
				while ((line = br.readLine()) != null) {
						writer.append(line+'\n');
					}		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
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
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			System.out.println("Done");
	    } catch (IOException e) {
	    System.out.println(e.getMessage());
	    }
	    out.println("file saved");
	}

}
