package Stats

import NMEA.Commands.CommandGGA
import NMEA.Enumerations.StatsParams
import UI.`Helper Functions`.testPngSaving
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.Range
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection

class StatsPlot(
    private val list: List<CommandGGA>,
    private val solution: StatsParams,
    private val stats: Stats
) {

    suspend fun showPlot(onNewImagePath: (String) -> String){
        val xAxisValue = list.map { it.timeUTC!!.time.toDouble() }
        val yAxisValue = mapBy(solution).filter { it != 0.0 }

        val chart = constructChart(xAxisValue, yAxisValue)
        chart.xyPlot.apply {
            rangeAxis.range = Range(yAxisValue.minOrNull()!!, yAxisValue.maxOrNull()!!)
            domainAxis.range = Range(xAxisValue.minOrNull()!!, xAxisValue.maxOrNull()!!)
        }
        testPngSaving(chart, onNewImagePath)
    }

    private fun mapBy(sol: StatsParams) =
        when(sol){
            StatsParams.SolutionLatitude -> list.map { it.latitude }
            StatsParams.SolutionLongitude -> list.map { it.longitude }
            StatsParams.SolutionMSL -> list.map { it.altitudeMSL }
        }
    private fun constructChart(xData: List<Double>, yData: List<Double>): JFreeChart{
        val series1 = XYSeries("${solution.name} from timeUTC", false)
        if(xData.size <= yData.size)
            xData.forEachIndexed { index, d ->
                series1.add(d, yData[index])
            }
        else
            yData.forEachIndexed{index, d ->
                series1.add(xData[index], d)
            }
        val series2 = XYSeries("someKey", false)
        val yValue = when(solution){
            StatsParams.SolutionLatitude -> stats.mean[0, 0]
            StatsParams.SolutionLongitude -> stats.mean[1, 0]
            StatsParams.SolutionMSL -> stats.mean[2, 0]

        }
        xData.forEach {
            series2.add(it, yValue)
        }
        val dataset = XYSeriesCollection().apply {
            addSeries(series1)
            addSeries(series2)
        }
        return ChartFactory.createXYLineChart(
            null,
            "timeUTC",
            solution.name,
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        )
    }
}
