/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alma.scheduling.spt.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import javax.swing.JPanel;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.gantt.XYTaskDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import alma.scheduling.output.generated.Array;
import alma.scheduling.output.generated.ObservationProject;
import alma.scheduling.output.generated.Results;
import alma.scheduling.output.generated.SchedBlock;

public class ScheduleChart extends ApplicationFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1101792410280201454L;

	private Results result;

	public ScheduleChart(String paramString) {
		super(paramString);
		
	}
	
	private void setupPlot() {
		JPanel localJPanel = createPanel();
		localJPanel.setPreferredSize(new Dimension(500, 300));
		setContentPane(localJPanel);
	}

	private XYPlot createSubplot1(XYDataset paramXYDataset) {
		XYLineAndShapeRenderer localXYLineAndShapeRenderer = new XYLineAndShapeRenderer();
		localXYLineAndShapeRenderer.setUseFillPaint(true);
		localXYLineAndShapeRenderer.setBaseFillPaint(Color.white);
		localXYLineAndShapeRenderer.setBaseShape(new Ellipse2D.Double(-4.0D,
				-4.0D, 8.0D, 8.0D));
		localXYLineAndShapeRenderer.setAutoPopulateSeriesShape(false);
		NumberAxis localNumberAxis = new NumberAxis("Y");
		localNumberAxis.setLowerMargin(0.1D);
		localNumberAxis.setUpperMargin(0.1D);
		XYPlot localXYPlot = new XYPlot(paramXYDataset, new DateAxis("Time"),
				localNumberAxis, localXYLineAndShapeRenderer);
		return localXYPlot;
	}

	private XYPlot createTaskPlot(IntervalXYDataset paramIntervalXYDataset) {
		DateAxis localDateAxis = new DateAxis("Date/Time");
		String[] names = new String[result.getArray().length];
		int i = 0;
		for(Array a: result.getArray()) {
			names[i++] = a.getConfigurationName();
		}
		SymbolAxis localSymbolAxis = new SymbolAxis("Arrays", names);
		localSymbolAxis.setGridBandsVisible(false);
		XYBarRenderer localXYBarRenderer = new XYBarRenderer();
		localXYBarRenderer.setUseYInterval(true);
		XYPlot localXYPlot = new XYPlot(paramIntervalXYDataset, localDateAxis,
				localSymbolAxis, localXYBarRenderer);
		return localXYPlot;
	}

	private JFreeChart createChart() {
		CombinedDomainXYPlot localCombinedDomainXYPlot = new CombinedDomainXYPlot(new DateAxis("Date/Time"));
//		localCombinedDomainXYPlot.add(createSubplot1(createDataset1()));
		localCombinedDomainXYPlot.add(createTaskPlot(createArrayDataSet()));
		localCombinedDomainXYPlot.add(createTaskPlot(createExecBlockDataSet()));
		JFreeChart localJFreeChart = new JFreeChart("Schedule Plan", localCombinedDomainXYPlot);
		localJFreeChart.setBackgroundPaint(Color.white);
		ChartUtilities.applyCurrentTheme(localJFreeChart);
		return localJFreeChart;
	}

	public JPanel createPanel() {
		return new ChartPanel(createChart());
	}

	private static XYDataset createDataset1() {
		TimeSeriesCollection localTimeSeriesCollection = new TimeSeriesCollection();
		TimeSeries localTimeSeries = new TimeSeries("Time Series 1", Hour.class);
		localTimeSeries.add(new Hour(0, new Day()), 20214.5D);
		localTimeSeries.add(new Hour(4, new Day()), 73346.5D);
		localTimeSeries.add(new Hour(8, new Day()), 54643.599999999999D);
		localTimeSeries.add(new Hour(12, new Day()), 92683.800000000003D);
		localTimeSeries.add(new Hour(16, new Day()), 110235.39999999999D);
		localTimeSeries.add(new Hour(20, new Day()), 120742.5D);
		localTimeSeries.add(new Hour(24, new Day()), 90654.5D);
		localTimeSeriesCollection.addSeries(localTimeSeries);
		return localTimeSeriesCollection;
	}
	
	private IntervalXYDataset createArrayDataSet() {
		XYTaskDataset localXYTaskDataset = new XYTaskDataset(createArrayTasks());
		localXYTaskDataset.setTransposed(true);
		localXYTaskDataset.setSeriesWidth(0.6D);
		return localXYTaskDataset;
	}

	private TaskSeriesCollection createArrayTasks() {
		TaskSeriesCollection localTaskSeriesCollection = new TaskSeriesCollection();
		for (Array a : result.getArray()) {
			TaskSeries series = new TaskSeries(a.getConfigurationName());
			series.add(new Task(a.getConfigurationName(), a.getStartDate(), a.getEndDate()));
			localTaskSeriesCollection.add(series);
		}
		return localTaskSeriesCollection;
	}

	private IntervalXYDataset createExecBlockDataSet() {
		XYTaskDataset localXYTaskDataset = new XYTaskDataset(
				createExecBlocktasks());
		localXYTaskDataset.setTransposed(true);
		localXYTaskDataset.setSeriesWidth(0.6D);
		return localXYTaskDataset;
	}

	private TaskSeriesCollection createExecBlocktasks() {
		TaskSeriesCollection localTaskSeriesCollection = new TaskSeriesCollection();
		for (Array a : result.getArray()) {
			TaskSeries series = new TaskSeries(a.getConfigurationName());
			for (ObservationProject p : result.getObservationProject()) {
				for (SchedBlock sb : p.getSchedBlock()) {
					if (sb.getArrayRef().getArrayRef().compareTo(a.getId()) == 0)
						series.add(new Task(sb.getOriginalId(), sb
								.getStartDate(), sb.getEndDate()));
				}
			}
			localTaskSeriesCollection.add(series);
		}
		return localTaskSeriesCollection;
	}

	private void loadOutputFile() {
		FileReader fr = null;
		try {
			fr = new FileReader(
					new File("/home/javarias/pms/work_dir/output/output_1399232499790.xml"));
			result = Results.unmarshal(fr);
		} catch (FileNotFoundException | MarshalException | ValidationException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if (fr != null)
				try {
					fr.close();
				} catch (IOException e) {
				}
		}

	}

	public static void main(String[] args) {
		ScheduleChart scheduleChart = new ScheduleChart("Schedule Chart");
		scheduleChart.loadOutputFile();
		scheduleChart.setupPlot();
		scheduleChart.pack();
		RefineryUtilities.centerFrameOnScreen(scheduleChart);
		scheduleChart.setVisible(true);
	}
}
