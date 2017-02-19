package fr.evolya.arduinoscilloscopia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.PinValueChangedEvent;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.util.URIs;

public class Arduino implements EventListener {
	
	private Link link;
	
	private Map<Integer, ArrayList<PinListener<DigitalPinValueChangedEvent>>> listenersDigitalPin;
	private Map<Integer, ArrayList<PinListener<AnalogPinValueChangedEvent>>> listenersAnalogicPin;
	
	private Arduino(String connectionString) {
		try {
	    	LinkManager mgr = LinkManager.getInstance();
	    	link = mgr.getConfigurer(URIs.newURI(connectionString)).newLink();
	    	link.addListener(this);
		}
		catch (Throwable ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		listenersDigitalPin = new HashMap<>();
		listenersAnalogicPin = new HashMap<>();
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static Arduino getAutomaticInstance() {
		return new Arduino("ardulink://serial-jssc?port=COM5&baudrate=115200&pingprobe=false&waitsecs=1");
	}

	public boolean switchDigitalPin(int portNumber, boolean value) {
		try {
			link.switchDigitalPin(DigitalPin.digitalPin(portNumber), value);
			return true;
		}
		catch (Throwable ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			return false;
		}
	}

	@Override
	public void stateChanged(AnalogPinValueChangedEvent evt) {
		broadcast(evt, listenersAnalogicPin);
		
	}

	@Override
	public void stateChanged(DigitalPinValueChangedEvent evt) {
		broadcast(evt, listenersDigitalPin);
	}
	
	private <E extends PinValueChangedEvent> void broadcast(E event, Map<Integer, ArrayList<PinListener<E>>> listeners) {
		System.out.println(event);
		// Pin number must be declared as listened
		if (!listeners.containsKey(event.getPin().pinNum())) return;
		// Fetch listeners for event propagation
		for (PinListener<E> listener : listeners.get(event.getPin().pinNum())) {
			listener.stateChanged(event);
		}
	}
	
	public boolean addDigitalPinListener(int portNumber, PinListener<DigitalPinValueChangedEvent> listener) {
		return addListener(Pin.digitalPin(portNumber), listener, listenersDigitalPin);
	}
	
	public boolean addAnalogicPinListener(int portNumber, PinListener<AnalogPinValueChangedEvent> listener) {
		return addListener(Pin.analogPin(portNumber), listener, listenersAnalogicPin);
	}
	
	private <E extends PinValueChangedEvent> boolean addListener(Pin pin, PinListener<E> listener,
			Map<Integer, ArrayList<PinListener<E>>> listeners) {
		if (!listeners.containsKey(pin.pinNum())) {
			// Create list
			ArrayList<PinListener<E>> list = new ArrayList<PinListener<E>>();
			// Add the new one
			list.add(listener);
			// Create listeners list
			listeners.put(pin.pinNum(), list);
			// Start listening
			try {
				link.startListening(pin);
			}
			catch (Throwable ex) {
				listeners.remove(pin.pinNum());
				// TODO Auto-generated catch block
				ex.printStackTrace();
				return false;
			}
		}
		else {
			listeners.get(pin.pinNum()).add(listener);
		}
		return true;
	}
	
	public boolean removeDigitalPinListeners(int portNumber) {
		return removeListeners(Pin.digitalPin(portNumber), listenersDigitalPin);
	}
	
	public boolean removeAnalogicPinListeners(int portNumber) {
		return removeListeners(Pin.analogPin(portNumber), listenersAnalogicPin);
	}
	
	private <E extends PinValueChangedEvent> boolean removeListeners(Pin pin, Map<Integer, ArrayList<PinListener<E>>> listeners) {
		if (listeners.containsKey(pin.pinNum())) {
			// Detach all listeners
			listeners.remove(pin.pinNum());
			// Stop listening
			try {
				link.stopListening(pin);
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public boolean removeDigitalPinListener(int portNumber, PinListener<DigitalPinValueChangedEvent> listener) {
		return removeListener(portNumber, listener, listenersDigitalPin);
	}
	
	public boolean removeAnalogicPinListener(int portNumber, PinListener<AnalogPinValueChangedEvent> listener) {
		return removeListener(portNumber, listener, listenersAnalogicPin);
	}
	
	private <E extends PinValueChangedEvent> boolean removeListener(int portNumber, PinListener<E> listener, 
			Map<Integer, ArrayList<PinListener<E>>> listeners) {
		if (listeners.containsKey(portNumber) && listeners.get(portNumber).contains(listener)) {
			listeners.get(portNumber).remove(listener);
			if (listeners.get(portNumber).isEmpty()) {
				// Detach listeners list
				listeners.remove(portNumber);
				// Stop listening
				try {
					link.stopListening(Pin.digitalPin(portNumber));
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	@FunctionalInterface
	public interface PinListener<E> {
		public void stateChanged(E evt);
	}
	
}