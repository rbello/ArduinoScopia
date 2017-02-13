package fr.evolya.arduinoscilloscopia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class Arduino implements SerialPortEventListener {
	
	SerialPort serialPort;
	/**
	* A BufferedReader which will be fed by a InputStreamReader 
	* converting the bytes into characters 
	* making the displayed results codepage independent
	*/
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	private String port;
	private ReadListener onRead;
	private Listener onConnected;
	private Listener onDisconnected;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public static final String A0 = null;

	public Arduino(String port) {
		this.port = port;
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=input.readLine();
				if (onRead != null)
					onRead.onRead(new Message(inputLine, this));
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

	public static Arduino create(String port) {
		return new Arduino(port);
	}

	public Arduino onConnected(Listener listener) {
		onConnected = listener;
		return this;
	}
	
	public Arduino onDisconnected(Listener listener) {
		onDisconnected = listener;
		return this;
	}
	
	@FunctionalInterface
	public static interface Listener {
		public void onReady(Arduino uno);
	}
	
	public Arduino onRead(ReadListener listener) {
		onRead = listener;
		return this;
	}
	
	@FunctionalInterface
	public static interface ReadListener {
		public void onRead(Message msg);
	}
	
	public static class Message {
		public final String contents;
		public final Arduino arduino;
		public Message(String contents, Arduino arduino) {
			this.contents = contents;
			this.arduino = arduino;
		}
		@Override
		public String toString() {
			return this.arduino + ": " + contents;
		}
	}
	
	@Override
	public String toString() {
		return String.format("[Arduino %s]", port);
	}

	public void open() {
		new Thread(() -> {
			
			CommPortIdentifier portId = null;
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

			//First, Find an instance of serial port as set in PORT_NAMES.
			while (portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				if (currPortId.getName().equals(Arduino.this.port)) {
					portId = currPortId;
					break;
				}
			}
			if (portId == null) {
				System.out.println("Could not find port " + Arduino.this.port);
				return;
			}
			
			try {
				// open serial port, and use class name for the appName.
				serialPort = (SerialPort) portId.open(Arduino.this.getClass().getName(), TIME_OUT);

				// set port parameters
				serialPort.setSerialPortParams(DATA_RATE,
						SerialPort.DATABITS_8,
						SerialPort.STOPBITS_1,
						SerialPort.PARITY_NONE);

				// open the streams
				input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
				output = serialPort.getOutputStream();

				// add event listeners
				serialPort.addEventListener(Arduino.this);
				serialPort.notifyOnDataAvailable(true);
				
				if (Arduino.this.onConnected != null) {
					Arduino.this.onConnected.onReady(Arduino.this);
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
			
		}).start();
	}

}