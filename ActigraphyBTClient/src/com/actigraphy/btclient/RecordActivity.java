package com.actigraphy.btclient;

import com.actigraphy.btclient.BluetoothClient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.shaker.R;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RecordActivity extends Activity implements SensorEventListener {
	private final static String STORETEXT = "storetext.txt";
	Button start, stop;
	double sum = 0.0;
	List<Double> sumValue = new ArrayList<Double>();
	private GraphicalView mChart;
	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
	private XYSeries mCurrentSeries;
	private XYSeriesRenderer mCurrentRenderer;
	TextView timerVal;
	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final float NOISE = (float) 2.0;
	long startTime = 0;

	private void initChart() {
		System.out.println("init chart constructor");
		mCurrentSeries = new XYSeries("Sleep Pattern");
		mDataset.addSeries(mCurrentSeries);
		mCurrentRenderer = new XYSeriesRenderer();
		mRenderer.addSeriesRenderer(mCurrentRenderer);
	}

	String dataCollected = null;

	private void addSampleData(List<Double> list) throws IOException {
		System.out.println("add sample data");
		for (int i = 0; i < list.size(); i++)
			mCurrentSeries.add(i, list.get(i));

		if (list.size() % 10 == 0) {
			System.out.println("sending data");
			BluetoothClient bClient = new BluetoothClient(list);
		}
	}

	private void drawGraph() throws IOException {
		System.out.println("drawgraph");
		sumValue.add(sum);
		LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
		if (mChart == null) {
			initChart();
			addSampleData(sumValue);
			mChart = ChartFactory.getCubeLineChartView(this, mDataset,
					mRenderer, 0.3f);
			layout.addView(mChart);
		} else {
			addSampleData(sumValue);
			mChart.repaint();
		}
	}

	// runs without a timer by reposting this handler at the end of the runnable
	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			timerVal.setText(String.format("%d:%02d", minutes, seconds));
			try {
				drawGraph();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timerHandler.postDelayed(this, 3000);

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mInitialized = false;
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		start = (Button) findViewById(R.id.start);
		stop = (Button) findViewById(R.id.stop);
		start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startProgram();
			}
			
		});
		stop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
			//	getBaseContext().stopService(RecordActivity.class);
				//this.stopService(new Intent(this, RecordActivity.class));
				finish();
				System.exit(0);
				//return;
			}
			
		});
	}

	/*private OnClickListener onClickListener = new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
	             switch(v.getId()){
	                 case R.id.start:
	                	 startProgram();
	                 case R.id.stop:
	                	// finish();
	                      //DO something
	                 break;
	              }

	    }
	};
*/	
	public void startProgram()
	{
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

		// Declare the timer
		timerVal = (TextView) findViewById(R.id.timer);
		startTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
	}
	protected void onResume() {

		super.onResume();
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		// timerHandler.removeCallbacks(timerRunnable);

	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub

		TextView tvX = (TextView) findViewById(R.id.x_axis);
		TextView tvY = (TextView) findViewById(R.id.y_axis);
		TextView tvZ = (TextView) findViewById(R.id.z_axis);

		// ImageView iv = (ImageView) findViewById(R.id.image);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE)
				deltaX = (float) 0.0;
			if (deltaY < NOISE)
				deltaY = (float) 0.0;
			if (deltaZ < NOISE)
				deltaZ = (float) 0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText(Float.toString(x));
			tvY.setText(Float.toString(y));
			tvZ.setText(Float.toString(y));
			sum = Math.abs(x) + Math.abs(y) + Math.abs(z);
			// sum = Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ);
		}
	}

}
