package UI.`Helper Functions`

import NMEA.Commands.*
import NMEA.Enumerations.CommandsName
import NMEA.Enumerations.Protocols
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.Range
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.sql.Date
import java.sql.Time
import javax.swing.JOptionPane
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun getFileFromDialog():String?{
    val frame = Frame()
    frame.size = Dimension(800, 600)
    val fd = FileDialog(frame)
    fd.isVisible = true
    val res = "${fd.directory}${fd.file}"

    if(res.contains(".txt") || res.contains(".csv"))
        return res
    else{
        val frame = Frame()
        frame.size = Dimension(800, 900)
        JOptionPane.showMessageDialog(frame,"Не тот тип файла","Ты еблан?", JOptionPane.WARNING_MESSAGE)
        return null
    }
}
fun imageFromFile(file: File): ImageBitmap {
    return org.jetbrains.skia.Image.makeFromEncoded(file.readBytes()).asImageBitmap()

}
fun resourceExist(path:String):Boolean{
    return File("src/main/resources/$path").exists()
}

fun checkButtonEnabled(commandType: CommandsName) = when(commandType){
    CommandsName.All -> false
    else -> true
}

fun axisMenuItems(commandType: CommandsName) = when(commandType){
    CommandsName.All -> emptyList()
    CommandsName.GGA -> getClassParameters(CommandGGA::class).filter { it in graphicableParams }
    CommandsName.RMC -> getClassParameters(CommandRMC::class).filter { it in graphicableParams }
    CommandsName.Report -> getClassParameters(Report::class).filter { it in graphicableParams}
    CommandsName.GSV -> listOf("azimuth", "elevation")
    CommandsName.GSA -> emptyList()
    CommandsName.ETK -> getClassParameters(CommandETK::class).filter { it in graphicableParams }
}

fun getClassParameters(classRef: KClass<*>) = classRef.memberProperties.map{
        val regex = """.[a-z A-z]+:""".toRegex()
        val res = regex.find(it.toString())?.value
        res?.substring(1..res.length-2)
    }

suspend fun showChart(
    commandsList: List<CommandNMEA>,
    chosenCommand: CommandsName,
    xAxisS: String,
    yAxis: String,
    beginTime: String = "00:00:00 2000-01-01",
    endTime: String = "23:59:59 2050-12-31",
    onNewImagePath: (String) -> String){

    println(chosenCommand.name)
    var filteredList =
        if(chosenCommand != CommandsName.GSV)
            commandsList.filter{it.messageID.contains(chosenCommand.name.uppercase()) && timeFilter(Time.valueOf(beginTime.split(" ").get(0)), Time.valueOf(endTime.split(" ").get(0)), (it as Position).timeUTC!!)}
        else{
            commandsList.filter{it.messageID.contains(chosenCommand.name.uppercase())}
        }
    if(filteredList.first() is CommandETK){
        val beginDate = Date.valueOf(beginTime.split(" ").get(1))
        val endDate = Date.valueOf(endTime.split(" ").get(1))
        filteredList = filteredList.filter { dateFilter(beginDate, endDate, (it as CommandETK).date!!) }
    }
    if(filteredList.first() is CommandGSV){
        val chart = constructPolarPlot(filteredList as List<CommandGSV>)
        testPngSaving(chart, onNewImagePath)
        return
    }
    val xAxisValues = filteredList.map(mapBy(xAxisS)).filter { it != 0.0 }
    val yAxisValues = filteredList.map(mapBy(yAxis)).filter { it != 0.0 }
    val xAxis: (List<Double>, String) -> JFreeChart = constructXYChart(xAxisValues, xAxisS)
    val chart: JFreeChart =  xAxis(yAxisValues, yAxis)


    chart.xyPlot.apply {
        rangeAxis.range = Range(yAxisValues.minOrNull()!!, yAxisValues.maxOrNull()!!)
        domainAxis.range = Range(xAxisValues.minOrNull()!!, xAxisValues.maxOrNull()!!)
    }

    testPngSaving(chart, onNewImagePath)
}

 fun constructPolarPlot(list: List<CommandGSV>): JFreeChart{
    val listOfSatellite = list.map { it.getSatelliteMap() }
    val mapOfSatellite: MutableMap<Int, MutableList<Satellite>> = mutableMapOf()

    val result = listOfSatellite.asSequence()
        .flatMap { it.asSequence() }
        .groupBy({ it.key }, { it.value })

    val dataset = XYSeriesCollection()

    result.keys.forEach { key ->
        val series = XYSeries("$key", false, true)
        result[key]!!.forEach { satellite ->
            series.add(satellite.azimuth, satellite.elevation)
        }
        dataset.addSeries(series)
    }
//   val polarPlot = PolarPlot()
//     polarPlot.dataset = dataset
//     polarPlot.renderer = CustomPolarItemRender().apply {
//
//     }
//     polarPlot.axis = NumberAxis()
//     return JFreeChart("", polarPlot)
     return  ChartFactory.createPolarChart(
         "",
         dataset,
         true,
         false,
         false
     )
}
private val dateFilter = {beginD: Date, endD: Date, currD: Date ->
    currD in beginD..endD
}
private val timeFilter = {beginT: Time, endT: Time, currT: Time ->
    currT in beginT..endT
}

val constructXYChart: (List<Double>, String) -> (List<Double>, String) -> JFreeChart =
    {xData, xLabel ->
        {yData, yLabel ->
            val series1 = XYSeries("$yLabel from $xLabel", false)
            if(xData.size <= yData.size)
                xData.forEachIndexed { index, d ->
                    series1.add(d, yData[index])
                }
            else
                yData.forEachIndexed{index, d ->
                    series1.add(xData[index], d)
                }
            val dataset = XYSeriesCollection().apply {addSeries(series1)}
            ChartFactory.createXYLineChart(
                null,
                xLabel,
                yLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
            )
        }}
private fun mapBy(atribute: String):(CommandNMEA) -> Double =
    when(atribute){
        "longitude" -> {command: CommandNMEA -> (command as Position).longitude!! }
        "latitude"  -> {command: CommandNMEA -> (command as Position).latitude!!  }
        "timeUTC"   -> {command: CommandNMEA -> (command as Position).timeUTC!!.time.toDouble()}
        "speed"     -> {command: CommandNMEA ->
            if(command is Report)
                command.speed?.toDouble()!!
            else
                (command as CommandETK).speed?.toDouble()?:0.0
        }
        "course" -> {command: CommandNMEA -> (command as Report).course?.toDouble()!!}
        "altitudeMSL" -> {command: CommandNMEA -> (command as CommandGGA).altitudeMSL}
        else -> {_ -> 0.0}
    }
suspend fun testPngSaving(chart: JFreeChart, func: (String) -> String){
    val newAddres = "${Random.nextInt()}.png"
    ChartUtils.saveChartAsPNG(File("src/main/resources/$newAddres"), chart, 800, 600)
    func(newAddres)
}