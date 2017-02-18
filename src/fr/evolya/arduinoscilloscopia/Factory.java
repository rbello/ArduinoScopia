package fr.evolya.arduinoscilloscopia;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.typicons.Typicons;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.scene.Node;

public class Factory {

	public static double TILE_SIZE = 200;
	
	private static final FontIcon iconOff;
	private static final FontIcon iconOn;
	
	static {
        iconOn = new FontIcon(Typicons.TICK);
        iconOn.setIconSize((int) TILE_SIZE);
        iconOn.setFill(Tile.FOREGROUND);
        iconOn.setIconColor(Tile.GREEN);

        iconOff = new FontIcon(Typicons.TIMES);
        iconOff.setIconSize((int) TILE_SIZE);
        iconOff.setFill(Tile.FOREGROUND);
        iconOff.setIconColor(Tile.RED);
	}
	
	public static Tile createDigitalWriteTile(String portName, int portNumber, Link link) {
		
		Tile tile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE)
                .skinType(SkinType.SWITCH)
                .title("Change GPIO " + portName)
                .text("Digital " + portNumber)
                .build();
		
		tile.setOnSwitchReleased(evt -> {
			try {
				link.switchDigitalPin(DigitalPin.digitalPin(portNumber), tile.isSelected());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		return tile;
	}

	public static Tile createDigitalReadTile(String portName, int portNumber, Link link) {
		Tile tile = TileBuilder.create()
                .skinType(SkinType.CUSTOM)
                .prefSize(TILE_SIZE, TILE_SIZE)
                .title("Read GPIO " + portName)
                .graphic(iconOn)
                .text("Digital " + portNumber)
                .build();
		try {
			link.addListener(new EventListener() {
				@Override
				public void stateChanged(DigitalPinValueChangedEvent evt) {
					if (evt.getPin().pinNum() != portNumber) return;
					Platform.runLater(() -> {
						tile.setGraphic(evt.getValue() ? iconOn : iconOff);
					});
				}
				@Override
				public void stateChanged(AnalogPinValueChangedEvent evt) { }
			});
			link.startListening(Pin.digitalPin(portNumber));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return tile;
	}

	public static Tile createAnalogReadTile(String portName, int portNumber, Link link) {
        Gauge gauge = createGauge(Gauge.SkinType.DIGITAL);
        Tile tile = TileBuilder.create()
                                  .prefSize(TILE_SIZE, TILE_SIZE)
                                  .skinType(SkinType.CUSTOM)
                                  .title("Read GPIO " + portName)
                                  .text("Analogic " + portNumber)
                                  .graphic(gauge)
                                  .build();
        try {
			
			link.addListener(new EventListener() {
				@Override
				public void stateChanged(DigitalPinValueChangedEvent evt) {
					System.out.println("D " + evt);
				}
				@Override
				public void stateChanged(AnalogPinValueChangedEvent evt) {
					System.out.println("A " + evt);
					if (evt.getPin().pinNum() != portNumber) return;
					Platform.runLater(() -> {
						System.out.println("traité");
						tile.setValue(evt.getValue());
					});
				}
			});
			link.startListening(Pin.analogPin(portNumber));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        return tile;
	}
	
    private static Gauge createGauge(final Gauge.SkinType TYPE) {
        return GaugeBuilder.create()
                           .skinType(TYPE)
                           .prefSize(TILE_SIZE, TILE_SIZE)
                           .animated(true)
                           .unit("\u00B0C") // TODO A virer
                           .valueColor(Tile.FOREGROUND)
                           .titleColor(Tile.FOREGROUND)
                           .unitColor(Tile.FOREGROUND)
                           .barColor(Tile.BLUE)
                           .needleColor(Tile.FOREGROUND)
                           .barColor(Tile.BLUE)
                           .barBackgroundColor(Tile.BACKGROUND.darker())
                           .tickLabelColor(Tile.FOREGROUND)
                           .majorTickMarkColor(Tile.FOREGROUND)
                           .minorTickMarkColor(Tile.FOREGROUND)
                           .mediumTickMarkColor(Tile.FOREGROUND)
                           .build();
    }

}
