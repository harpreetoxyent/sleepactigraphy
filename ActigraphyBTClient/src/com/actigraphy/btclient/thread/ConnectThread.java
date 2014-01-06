package com.actigraphy.btclient.thread;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectThread extends Thread 
{
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private BluetoothSocket mmSocket = null;
	private BluetoothDevice mmDevice = null;
	byte[] buffer = null;
	int lengthOfDataTobeSent = 0;
	public ConnectThread(BluetoothDevice device, byte[] bufferToBeSent,int lengthOfData)
			throws InterruptedException {
		System.out.println("ConnectThread");
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		mmDevice = device;
		lengthOfDataTobeSent = lengthOfData;
		buffer = bufferToBeSent;
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
		manageConnectedSocket(mmSocket,lengthOfDataTobeSent);
	}

	public void manageConnectedSocket(BluetoothSocket mmSocket, int lengthOfDataTobeSent) 
	{
		System.out.println("\ninside manage connected socket. socket: "
				+ mmSocket.toString() + " ***** " + mmSocket.getRemoteDevice());
		DataSendingThread sendData = new DataSendingThread(mmSocket,buffer,lengthOfDataTobeSent);
		sendData.start();
	}
	
}