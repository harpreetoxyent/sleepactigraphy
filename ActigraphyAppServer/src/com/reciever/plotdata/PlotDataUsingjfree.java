package com.reciever.plotdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.plot.*;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTInline;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlToken;

public class PlotDataUsingjfree {
	ArrayList<Integer> list1 = new ArrayList<Integer>();
	ArrayList<Double> listX = new ArrayList<Double>();
	ArrayList<Double> listY = new ArrayList<Double>();
	ArrayList<Double> listZ = new ArrayList<Double>();
	String filename;
	public PlotDataUsingjfree(String name) {
		filename=name;
	}

	public void drawGraph(ArrayList<Integer> list1, ArrayList<Double> list2, String axis) {
		XYSeries series = new XYSeries("Activity vs Time graph");
		for (int j = 0; j < list2.size(); j++) {
			series.add((double) list1.get(j), (double) list2.get(j));
		}
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);

		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart("Actigraphy", // Title
				"Epoch -->", // x-axis Label
				"Acceleometer Reading -->", // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		try {
			ChartUtilities.saveChartAsJPEG(new File( "C:\\SubjectID_"+filename.replace(':', '_')+"_"+axis+".jpg"), chart,
					500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.--"+e.getMessage());
		}
	}
}