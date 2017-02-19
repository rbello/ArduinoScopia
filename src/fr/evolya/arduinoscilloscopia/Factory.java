package fr.evolya.arduinoscilloscopia;

import java.util.Date;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.typicons.Typicons;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;

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
	
	public static Tile createDigitalWriteTile(String pinName, int pinNumber, Arduino link) {
		
		Tile tile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE)
                .skinType(SkinType.SWITCH)
                .title("Change GPIO " + pinName)
                .text("Digital " + pinNumber)
                .build();
		
		tile.setOnSwitchReleased(evt -> {
			link.switchDigitalPin(pinNumber, tile.isSelected());
		});
		
		return tile;
	}

	public static Tile createDigitalReadTile(String pinName, int pinNumber, Arduino link) {
		
		Tile tile = TileBuilder.create()
                .skinType(SkinType.CUSTOM)
                .prefSize(TILE_SIZE, TILE_SIZE)
                .title("Read GPIO " + pinName)
                .graphic(iconOn)
                .text("Digital " + pinNumber)
                .build();
		
		link.addDigitalPinListener(pinNumber, (evt) -> {
			Platform.runLater(() -> {
				tile.setGraphic(evt.getValue() ? iconOn : iconOff);
			});
		});
		
		return tile;
	}

	public static Tile createAnalogReadTile(String pinName, int pinNumber, Arduino link) {
        Gauge gauge = createGauge(Gauge.SkinType.DASHBOARD);
        gauge.setMinValue(0);
        gauge.setMaxValue(1023);
        gauge.setThresholdVisible(false);
        Tile tile = TileBuilder.create()
                                  .prefSize(TILE_SIZE, TILE_SIZE)
                                  .skinType(SkinType.CUSTOM)
                                  .title("Read GPIO " + pinName)
                                  .text("Analogic " + pinNumber)
                                  .graphic(gauge)
                                  .build();
        
        link.addAnalogicPinListener(pinNumber, (evt) -> {
			Platform.runLater(() -> {
				gauge.setValue(evt.getValue());
			});
		});
        return tile;
	}
	
    private static Gauge createGauge(final Gauge.SkinType TYPE) {
        return GaugeBuilder.create()
                           .skinType(TYPE)
                           .prefSize(TILE_SIZE, TILE_SIZE)
                           .animated(true)
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

	public static Tile createAnalogGraphTile(String pinName, int pinNumber, Arduino link) {
		// LineChart Data
        XYChart.Series<Long, Integer> series = new XYChart.Series<Long, Integer>();
        series.setName("Whatever");
//        series.getData().add(new XYChart.Data("MO", 23));
//        series.getData().add(new XYChart.Data("TU", 21));
//        series.getData().add(new XYChart.Data("WE", 20));
//        series.getData().add(new XYChart.Data("TH", 22));
//        series.getData().add(new XYChart.Data("FR", 24));
//        series.getData().add(new XYChart.Data("SA", 22));
//        series.getData().add(new XYChart.Data("SU", 20));

		Tile tile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE)
                .skinType(SkinType.AREA_CHART)
                .title("Analogic " + pinName)
                .series(series)
                .minValue(0)
                .maxValue(1023)
                .build();
		
		link.addAnalogicPinListener(pinNumber, (evt) -> {
			Platform.runLater(() -> {
				series.getData().add(new XYChart.Data(new Date().getTime(), 50));
			});
		});
		
		return tile;
	}

}
