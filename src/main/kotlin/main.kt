import NMEA.Commands.CommandNMEA
import NMEA.Enumerations.CommandsName
import UI.`Compose Functions`.AxisMenuAndText
import UI.`Compose Functions`.ListOfCommands
import UI.`Helper Functions`.getFileFromDialog
import UI.`Helper Functions`.imageFromFile
import UI.`Helper Functions`.resourceExist
import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.intellij.lang.annotations.JdkConstants
import org.jetbrains.skija.Bitmap
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ui.HorizontalAlignment
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JOptionPane
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
fun main() = Window(size  = IntSize(1280, 720),
                    resizable = false) {
    var beginTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("")}
    var imagePath by remember { mutableStateOf("defaultPng.png") }
    var commandsFile by remember { mutableStateOf("") }
    var expand by remember { mutableStateOf(false) }
    var commandsListSize by remember { mutableStateOf(0) }
    var commandsList by remember { mutableStateOf(listOf<CommandNMEA>()) }
    var chosenCommand by remember { mutableStateOf(CommandsName.All) }
    MaterialTheme {
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()){

            ListOfCommands(modifier = Modifier
                .fillMaxHeight()
                .width(500.dp).
                padding(16.dp).
                weight(1F ).
                border(1.dp, Color.Black),
            filePath = commandsFile,
            onListChange = {commandsList = CommandNMEA.readListOfCommandsFromFile(commandsFile, commandType = CommandsName.All); commandsList},
            onSizeChange = { commandsListSize = it.size; commandsListSize})

            Column(modifier = Modifier
                .fillMaxHeight()
                .width(500.dp)
                .padding(16.dp)
                .weight(0.5F)
                .border(1.dp, Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
                Button(onClick = {
                    commandsFile = getFileFromDialog()?:""
                }){ Text("Import")}
            }

            Column(modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .weight(2F).border(1.dp, Color.Black)){
                Surface(shape = RectangleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    color = Color.Magenta) {

                    if(resourceExist(imagePath)){
                        val file = File("src/main/resources/$imagePath")
                        val imageBitmap = imageFromFile(file)
                        Image(bitmap = imageBitmap,
                            contentDescription = "image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier)
                    }else{
                        Image(bitmap = imageFromResource("defaultPng.png"),
                            contentDescription = "image",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier)
                    }

                }
                Spacer(modifier = Modifier.height(22.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1F)){
                        AxisMenuAndText("Начало интервала", beginTime) { beginTime = it }
                    }
                    Button(onClick = {
                                     GlobalScope.async{ testPngSaving{imagePath = it; it} }
                    },
                        modifier = Modifier
                            .weight(1F))
                    {Text("Построить график")}
                    Column(modifier = Modifier
                        .weight(1F), verticalArrangement = Arrangement.Center){
                        AxisMenuAndText("Конец интервала", endTime) { endTime = it }
                    }
                }
            }
        }
    }
}


val constructXYChart: (List<Double>, String) -> (List<Double>, String) -> JFreeChart =
    {xData, xLabel ->
        {yData, yLabel ->
            val series1 = XYSeries("$yLabel from $xLabel")
            xData.forEachIndexed { index, d ->
                series1.add(d, yData[index])
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

suspend fun testPngSaving(func: (String) -> String){
        val list1 = mutableListOf<Double>()
        val list2 = mutableListOf<Double>()

        for (i in 0..10) {
            list1.add(Random.nextDouble())
            list2.add(Random.nextDouble())
        }

        val xAxisCreated = constructXYChart(list1, "this is x Axis")
        val createdChart = xAxisCreated(list2, "this is y Axis")

        val newAddres = "${Random.nextInt()}.png"
        ChartUtils.saveChartAsPNG(File("src/main/resources/$newAddres"), createdChart, 800, 600)
        func(newAddres)
}