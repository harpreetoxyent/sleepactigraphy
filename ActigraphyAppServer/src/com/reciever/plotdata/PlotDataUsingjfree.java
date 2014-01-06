package com.reciever.plotdata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.reciever.constants.ActigraphyServerConstants;

public class PlotDataUsingjfree {
	
	String filename;
	public PlotDataUsingjfree(String name) {
		filename=name;
	}

	public void drawGraph(ArrayList<Integer> list1, ArrayList<Double> list2, String axis) {
		XYSeries series = new XYSeries(ActigraphyServerConstants.Plot_Title_Graph);
		for (int j = 0; j < list2.size(); j++) 
		{
			series.add((double) list1.get(j), (double) list2.get(j));
		}
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYLineChart("raw_"+axis.toLowerCase().charAt(0), // Title
				ActigraphyServerConstants.Plot_X_AXIS_LABEL, // x-axis Label
				ActigraphyServerConstants.Plot_Y_AXIS_LABEL, // y-axis Label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot Orientation
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		System.out.println("axis is ---- "+axis);
		String name =  ActigraphyServerConstants.Plot_Location_Save+filename.replace(':', '_');
		String last = "_"+axis+".jpg";
		System.out.println("last is "+last);
		name.concat(last);
		System.out.println("filename is ---- "+name);
		
		
		
		try {
			ChartUtilities.saveChartAsJPEG(new File(ActigraphyServerConstants.Plot_Location_Save+axis+".jpg"), chart,
					500, 300);
		} catch (IOException e) {
			System.err.println("Problem occurred creating chart.--"+e.getMessage());
			e.printStackTrace();
		}
	}
}