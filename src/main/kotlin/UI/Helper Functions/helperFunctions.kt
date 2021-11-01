package UI.`Helper Functions`

import NMEA.Commands.CommandGGA
import NMEA.Commands.CommandNMEA
import NMEA.Commands.CommandRMC
import NMEA.Commands.graphicableParams
import NMEA.Enumerations.CommandsName
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import constructXYChart
import org.jfree.data.Range
import testPngSaving
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.sql.Time
import javax.swing.JOptionPane
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun getFileFromDialog():String?{
    val frame = Frame()
    frame.size = Dimension(800, 600)
    val fd = FileDialog(frame)
    fd.isVisible = true
    val res = "${fd.directory}${fd.file}"

    if(res.contains(".txt"))
        return res
    else{
        val frame = Frame()
        frame.size = Dimension(800, 900)
        JOptionPane.showMessageDialog(frame,"Не тот тип файла","Ты еблан?", JOptionPane.WARNING_MESSAGE)
        return null
    }
}
fun imageFromFile(file: File): ImageBitmap {
    return org.jetbrains.skija.Image.makeFromEncoded(file.readBytes()).asImageBitmap()
}
fun resourceExist(path:String):Boolean{
    return File("src/main/resources/$path").exists()
}

fun checkButtonEnabled(commandType: CommandsName) = when(commandType){
    CommandsName.All -> false
    else -> true
}

fun axisMenuItems(commandType: CommandsName) = when(commandType){
    CommandsName.All -> listOf("")
    CommandsName.GGA -> getClassParameters(CommandGGA::class).filter { it in graphicableParams }
    CommandsName.RMC -> getClassParameters(CommandRMC::class).filter { it in graphicableParams }
}

fun getClassParameters(classRef: KClass<*>) = classRef.memberProperties.map{
        val regex = """.[a-z A-z]+:""".toRegex()
        val res = regex.find(it.toString())?.value
        res?.substring(1..res.length-2)
    }

suspend fun showChart(
    commandsList: List<CommandNMEA>,
    chosenCommand: CommandsName,
    xAxis: String,
    yAxis: String,
    beginTime: String = "00:00:00",
    endTime: String = "23:59:59",
    onNewImagePath: (String) -> String){
    print(chosenCommand.name)

    println("BT = $beginTime ET = $endTime")
    val xAxisValues = if(xAxis == "latitude"){
        commandsList.asSequence().filter{it.messageID.contains(chosenCommand.name) && (it.timeUTC!! >= Time.valueOf(beginTime) && it.timeUTC!! <= Time.valueOf(endTime))}.map {it.latitude!!}.toList()
    }else{
        commandsList.asSequence().filter { it.messageID.contains(chosenCommand.name) && (it.timeUTC!! >= Time.valueOf(beginTime) && it.timeUTC!! <= Time.valueOf(endTime))}.map { it.longitude!! }.toList()
    }

    val yAxisValues = if(yAxis == "latitude"){
        commandsList.asSequence().filter{it.messageID.contains(chosenCommand.name) && (it.timeUTC!! >= Time.valueOf(beginTime) && it.timeUTC!! <= Time.valueOf(endTime))}.map {it.latitude!!}.toList()
    }
    else{
        commandsList.asSequence().filter { it.messageID.contains(chosenCommand.name) && (it.timeUTC!! >= Time.valueOf(beginTime) && it.timeUTC!! <= Time.valueOf(endTime))}.map { it.longitude!! }.toList()
    }
    val long = commandsList.asSequence().filter { it.messageID.contains(chosenCommand.name)}.map { it.longitude!! }.toList()
    val lat = commandsList.asSequence().filter{it.messageID.contains(chosenCommand.name)}.map {it.latitude!!}.toList()
    val xAxis = constructXYChart(xAxisValues, xAxis)
    val chart = xAxis(yAxisValues, yAxis)

    chart.xyPlot.apply {
        rangeAxis.range = Range(yAxisValues.minOrNull()!!, yAxisValues.maxOrNull()!!)
        domainAxis.range = Range(xAxisValues.minOrNull()!!, xAxisValues.maxOrNull()!!)
    }

    testPngSaving(chart, onNewImagePath)
}