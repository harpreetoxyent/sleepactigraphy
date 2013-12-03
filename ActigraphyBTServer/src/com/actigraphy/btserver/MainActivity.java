package com.actigraphy.btserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.bluetoothserver.R;

//import com.example.sleep.RecordActivity.SendFile;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
	private final String IP_ADDRESS_APP_SERVER="192.168.1.4";
	private final String PORT_APP_SERVER = "8085";
	//private final String PROJECT_NAME_SERVER = "/ActigraphyAppServer/";
	private final String PROJECT_NAME_SERVER = "/RecieveDataFromSleepApp/";
	final int LENGTHOFARRAY = 15;
	TextView info;
	BluetoothAdapter mBluetoothAdapter;
	private static final int REQUEST_ENABLE_BT = 1;
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public String dataReceived = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		info = (TextView) findViewById(R.id.showInfo);
		System.out.println("\nstarting bluetooth server");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			System.out.println("no bluetooth device");
		}
		if (!mBluetoothAdapter.isEnabled()) {

			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);

			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

		}

		if (mBluetoothAdapter.isEnabled()) {
			System.out.println("bluetooth enabled");
		}
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
		AcceptThread at = new AcceptThread();
		at.start();
		// at.cancel();
	}

	private class AcceptThread extends Thread {
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			System.out.println("inside accept thread");
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {

				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						"BluetoothClient", MY_UUID);
				System.out.println("connecting to client app");
			} catch (IOException e) {
			}
			mmServerSocket = tmp;
		}

		public void run() {
			System.out.println("\ninside run");
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			System.out.println("\nlistening: ");
			while (true) 
			{
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					System.out.println("\nException in sockte.accept(): "
							+ e.getMessage());
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// System.out.println("\nsocket: obtnd:"+socket.toString());
					// Do work to manage the connection (in a separate thread)
					try {
						manageConnectedSocket(socket);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//break;
				}
			}
		}

	}

	public void manageConnectedSocket(BluetoothSocket socket)
			throws InterruptedException {
		System.out.println("\ninside manage connection..");
		ConnectedThread ct = new ConnectedThread(socket);
		ct.start();
		//ct.cancel();
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				// System.out.println("\n getting input and output streams of socket");
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				System.out.println("\n exception=" + e.getMessage());
				e.printStackTrace();
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		Handler mHandler;

		public void run() {
			System.out.println("\n Server thread running..");
			byte[] buffer = new byte[6 * LENGTHOFARRAY + 17]; // buffer store
																// for the
																// stream
			int bytes; // bytes returned from read()
			// Keep listening to the InputStream until an exception occurs
			try {
				// Read from the InputStream
				bytes = mmInStream.read(buffer, 0, 6 * LENGTHOFARRAY + 17);
				dataReceived = new String(buffer);
				System.out.println("data received " + dataReceived);
				sendData();
			} catch (IOException e) {
				System.out.println("Exception happened " + e.getMessage());
				e.printStackTrace();
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
		}
	}

	public void sendData() {
		new SendFile().execute();
	}

	class SendFile extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... urls) {
			
			String URL = "http://"+IP_ADDRESS_APP_SERVER+":"+PORT_APP_SERVER+PROJECT_NAME_SERVER+"/SaveDataToMongoDB";

			HttpClient httpclient = new DefaultHttpClient();
			HttpClient http = AndroidHttpClient.newInstance("Sleep");
			HttpPost method = new HttpPost(URL);
			/*
			 * w method.setEntity(new FileEntity(new File(existingFileName),
			 * "application/text"));
			 */
			try {
				System.out.println("inside send file asyn task...sending: "
						+ dataReceived);
				method.setEntity(new StringEntity(dataReceived.toString()));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				HttpResponse response = http.execute(method);
				Log.v("response code", response.getStatusLine().getStatusCode()
						+ "");
				System.out.println("data sent...");

			} catch (ClientProtocolException e) {

				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
