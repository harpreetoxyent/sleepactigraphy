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
	Button start, stop;
	double sum = 0.0;
	List<Double> sumValue = new ArrayList<Double>();
	List<Double> xAxis = new ArrayList<Double>();
	List<Double> yAxis = new ArrayList<Double>();
	List<Double> zAxis = new ArrayList<Double>();
	TextView timerVal;
	int time = 0;
	private double mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final float NOISE = (float) 2.0;
	long startTime = 0;

	private void addValue() throws IOException {
		System.out.println("adding data to list");
	//	sumValue.add(sum);
		xAxis.add(mLastX);
		yAxis.add(mLastY);
		zAxis.add(mLastZ);
		if(xAxis.size()%10==0)
		{
			System.out.println("Sending data to server...");
			sendData();
			xAxis.clear();
			yAxis.clear();
			zAxis.clear();
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
			//time = seconds + minutes * 60;
			timerVal.setText(String.format("%d:%02d", minutes, seconds));
			try {
				addValue();
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
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startProgram();
			}

		});
		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				System.exit(0);
				return;
			}

		});
	}

	public void startProgram() {
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
	void sendData(){
		BluetoothClient bClient = new BluetoothClient(xAxis, yAxis, zAxis);
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
			double deltaX = Math.abs(mLastX - x);
			double deltaY = Math.abs(mLastY - y);
			double deltaZ = Math.abs(mLastZ - z);
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
