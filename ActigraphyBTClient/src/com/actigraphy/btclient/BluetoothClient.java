package com.actigraphy.btclient;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Handler;

import com.actigraphy.btclient.thread.ConnectThread;
import com.actigraphy.constants.BTClientConstants;
public class BluetoothClient {
	private static String BLU_UNIQUE_ID = null;
	IntentFilter filter;
	BroadcastReceiver mReceiver;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothDevice targetDevice;
	byte[] bufferBluetoothClient = new byte[1024];
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
				bufferBluetoothClient[lengthOfData + j] = byteArray[j];
			}
			lengthOfData = lengthOfData + byteArray.length;
			System.out.println("length of this epoch " + byteArray.length
					+ " &&&total length gets " + lengthOfData);
		}
		byteArray = ",".getBytes();
		System.out.println("lengthOfData  " + lengthOfData);

		// insert a string
		for (int j = 0; j < byteArray.length; j++) {
			bufferBluetoothClient[lengthOfData + j] = byteArray[j];

		}
		lengthOfData = lengthOfData + byteArray.length;

	}

	public BluetoothClient(List<Double> x, List<Double> y, List<Double> z) {
		list = x;
		if(x.size()!=0)
		{
			prepareData(x);
			prepareData(y);
			prepareData(z);	
		}
		else
		{
			System.out.println("received stop as list size is zero..................");
		}
		byteArray = "_".getBytes();

		// insert a string
		for (int j = 0; j < byteArray.length; j++) {
			bufferBluetoothClient[lengthOfData + j] = byteArray[j];

		}

		lengthOfData = lengthOfData + byteArray.length;
		System.out.println("lengthOfData  " + lengthOfData);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) 
		{
			// Device does not support Bluetooth
			// Inform user that we're done.
			System.out.println("\n Bluetooth NOT supported. Aborting.");
		} 
		else 
		{
			BLU_UNIQUE_ID = mBluetoothAdapter.getAddress();
			// append unique id
			if (BLU_UNIQUE_ID != null) {
				System.out.println("BLU_UNIQUE_ID is: " + BLU_UNIQUE_ID);
				byteArray = BLU_UNIQUE_ID.getBytes();
				for (int j = 0; j < byteArray.length; j++) {
					bufferBluetoothClient[lengthOfData + j] = byteArray[j];
				}
				lengthOfData = lengthOfData + byteArray.length;
				System.out
						.println("Now the final length of data after appending mac address is "
								+ lengthOfData);
			}
			System.out.println("data from client is \n "+new String(bufferBluetoothClient));
			findPairedDevices(mBluetoothAdapter);
		}

	}

	public void findPairedDevices(BluetoothAdapter mBluetoothAdapter) {
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		if (devices != null) 
		{
			System.out.println("\n Devices found number of paired device: "
					+ devices.size());
			for (BluetoothDevice device : devices) {
				System.out.println("\n Found: " + device.getName());
				if (device.getName().equals(BTClientConstants.BT_Server_Name)) {
					System.out.println("\n Found Target device "
							+ device.getName() + " --device.getAddress()="
							+ device.getAddress());
					try {
						targetDevice = mBluetoothAdapter.getRemoteDevice(device
								.getAddress());
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
			throws InterruptedException 
	{
		ConnectThread ct = new ConnectThread(device, bufferBluetoothClient, lengthOfData);
		ct.start();
	}
	public Handler mHandler;
}
