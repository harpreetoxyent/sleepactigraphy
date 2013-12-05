package com.actigraphy.btclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

public class BluetoothClient {
	private final String deviceName = "Harpreet Xebia Note3";
	private static final int REQUEST_BLU = 1;
	private static String BLU_UNIQUE_ID = null;
	IntentFilter filter;
	BroadcastReceiver mReceiver;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice targetDevice;
	byte[] buffer = new byte[1024];
	int lengthOfData = 0;
	double doubleValue;
	DecimalFormat df;
	String totalTime;
	String newDouble;
	byte[] byteArray;
	List<Double> list = new ArrayList<Double>();

	public void prepareData(List<Double> data) {
		for (double value : data) {
			value = value + 0.0001;
			doubleValue = value;
			df = new DecimalFormat("#.####");
			newDouble = df.format(doubleValue);
			System.out.println("newDouble" + newDouble);
			byteArray = (newDouble.replace(",", "")).getBytes();
			for (int j = 0; j < byteArray.length; j++) {
				buffer[lengthOfData + j] = byteArray[j];
			}
			lengthOfData = lengthOfData + byteArray.length;
			System.out.println("length of this epoch " + byteArray.length
					+ " &&&total length gets " + lengthOfData);
		}
		byteArray = ",".getBytes();
		System.out.println("lengthOfData  " + lengthOfData);

		// insert a string
		for (int j = 0; j < byteArray.length; j++) {
			buffer[lengthOfData + j] = byteArray[j];

		}
		lengthOfData = lengthOfData + byteArray.length;

	}

	public BluetoothClient(List<Double> x, List<Double> y, List<Double> z) {
		list = x;
		prepareData(x);
		prepareData(y);
		prepareData(z);
		byteArray = "_".getBytes();

		// insert a string
		for (int j = 0; j < byteArray.length; j++) {
			buffer[lengthOfData + j] = byteArray[j];

		}

		lengthOfData = lengthOfData + byteArray.length;
		System.out.println("lengthOfData  " + lengthOfData);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			// Inform user that we're done.
			System.out.println("\n Bluetooth NOT supported. Aborting.");
		} else {
			BLU_UNIQUE_ID = mBluetoothAdapter.getAddress();

			// append unique id
			if (BLU_UNIQUE_ID != null) {
				System.out.println("BLU_UNIQUE_ID is: " + BLU_UNIQUE_ID);
				byteArray = BLU_UNIQUE_ID.getBytes();
				for (int j = 0; j < byteArray.length; j++) {
					buffer[lengthOfData + j] = byteArray[j];
				}
				lengthOfData = lengthOfData + byteArray.length;
				System.out
						.println("Now the final length of data after appending mac address is "
								+ lengthOfData);
			}
			System.out.println("data from client is \n "+new String(buffer));
			
			findPairedDevices(mBluetoothAdapter);
		}

	}

	public void findPairedDevices(BluetoothAdapter mBluetoothAdapter) {

		if (mBluetoothAdapter.isEnabled()) {
			System.out.println("\nBluetooth is enabled.");
		}
		// boolean found = mBluetoothAdapter.startDiscovery();
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		if (devices != null) {
			System.out.println("\n Devices found number of paired device: "
					+ devices.size());
			for (BluetoothDevice device : devices) {
				System.out.println("\n Found: " + device.getName());
				if (deviceName.equals(device.getName())) {
					System.out.println("\n Found Target device "
							+ device.getName() + " --device.getAddress()="
							+ device.getAddress());
					try {
						targetDevice = mBluetoothAdapter.getRemoteDevice(device
								.getAddress());
						;
						startTransfer(targetDevice);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public void startTransfer(BluetoothDevice device)
			throws InterruptedException {

		ConnectThread ct = new ConnectThread(device);
		ct.start();

	}

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private class ConnectThread extends Thread {
		private BluetoothSocket mmSocket = null;
		private BluetoothDevice mmDevice = null;

		public ConnectThread(BluetoothDevice device)
				throws InterruptedException {
			System.out.println("ConnectThread");
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				System.out.println("ConnectThread creating connection");
				// MY_UUID is the app's UUID string, also used by the server
				// code
				mmSocket = BluetoothAdapter.getDefaultAdapter()
						.getRemoteDevice(device.getAddress())
						.createRfcommSocketToServiceRecord(MY_UUID);
				try {
					System.out
							.println("ConnectThread connction created. UUID = "
									+ mmDevice.getUuids()[0].getUuid());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (IOException e) {
			}
		}

		public void run() {
			System.out.println("\nConnectThread inside run");
			// Cancel discovery because it will slow down the connection
			// mBluetoothAdapter.cancelDiscovery();
			try {
				System.out
						.println("ConnectThread inside try. trying to connect.");
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				System.out.println("connected!!!! mmSocket: "
						+ mmSocket.toString());
			} catch (IOException connectException) {
				System.out
						.println("ConnectThread connect exception. exception is: "
								+ connectException.getMessage());
				// Unable to connect; close the socket and get out
				/*
				 * try { //mmSocket.close(); } catch (IOException
				 * closeException) { }
				 */
				return;
			}
			// Do work to manage the connection (in a separate thread)
			manageConnectedSocket(mmSocket);
		}
	}

	public void manageConnectedSocket(BluetoothSocket mmSocket) {
		System.out.println("\ninside manage connected socket. socket: "
				+ mmSocket.toString() + " ***** " + mmSocket.getRemoteDevice());
		ConnectedThread sendData = new ConnectedThread(mmSocket);
		sendData.start();
		System.out.println("sending this data: " + new String(buffer));
		sendData.write(buffer);
		//sendData.write(list);
		/*try {
			sendData.sleep(3000);
			System.out.println("waking!!!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		System.out.println("closing socket");
		sendData.cancel();
	}

	public Handler mHandler;

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			System.out.println("\ninside Connected Thread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				System.out
						.println("\nException caught ConnectedThread. Exception is: "
								+ e.getMessage());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			System.out.println("\ninside Connected Thread run");
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			/*
			 * // Keep listening to the InputStream until an exception occurs
			 * while (true) { try { // Read from the InputStream bytes =
			 * mmInStream.read(buffer); // Send the obtained bytes to the UI
			 * activity mHandler.obtainMessage(0, bytes, -1, buffer)
			 * .sendToTarget(); } catch (IOException e) {
			 * System.out.println("\ninside Connected Thread run exception: "
			 * +e.getMessage()); break; } }
			 */
		}

		/*
		 * Call this from the main activity to send data to the remote device
		 */
		public void write(byte[] bytes) {
			try {
				System.out.println("\ninside Connected Thread write function");
				mmOutStream.write(bytes, 0, lengthOfData);
				mmOutStream.flush();
				mmOutStream.close();
				System.out.println("\ninside Connected Thread. data written"
						+ bytes + " --num Byte=" + lengthOfData);
			} catch (IOException e) {
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				System.out.println("inside cancel to close socket");
				mmSocket.getOutputStream().flush();
				mmSocket.getOutputStream().close();
				mmSocket.getInputStream().close();
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}
}
