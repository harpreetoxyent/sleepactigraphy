package com.actigraphy.btclient.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class DataSendingThread extends Thread {
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private byte[] bytes;
	private int lengthOfData;
	public DataSendingThread(BluetoothSocket socket,byte[] bytesIn, int lengthOfDataIn) 
	{
		System.out.println("\ninside Connected Thread");
		bytes = bytesIn;
		lengthOfData = lengthOfDataIn;
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

		System.out.println("sending this data: " + new String(bytes));
		write(bytes,lengthOfData);
		System.out.println("closing socket");
		closeStreams();
	}

	/*
	 * Call this from the main activity to send data to the remote device
	 */
	public void write(byte[] bytes, int lengthOfData) {
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
	public void closeStreams() {
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