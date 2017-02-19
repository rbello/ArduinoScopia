package fr.evolya.arduinoscilloscopia;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class OscilloscopeSeries {

	private XYChart.Series<String, Integer> series;
	private int MAX = 7;
	
	public OscilloscopeSeries() {
		series = new XYChart.Series<>();
		series.setName("Oscillo");
		for (int i = 0; i < MAX ; ++i) {
			series.getData().add(new XYChart.Data<String, Integer>("" + i, 50));
		}
	}	

	public XYChart.Series<String, Integer> getSeries() {
		return series;
	}

	public void add(int value) {
		ObservableList<Data<String, Integer>> list = series.getData();
		System.out.println("Add " + value + " to the end");
		for (int i = 0, l = MAX - 1; i <= l; ++i) {
			if (i == l) {
				System.out.println("Set value to " + i + "(" + value + ")");
				list.get(i).setYValue(value);
			}
			else {
				System.out.println("Switch " + (i+1) + " (" + list.get(i + 1).getYValue() + ") instead of " + i + " ");
				list.get(i).setYValue(list.get(i + 1).getYValue());
			}
		}
//		list.remove(0);
//		list.add(new XYChart.Data<String, Integer>("X", value));
	}
	
}
