import NMEA.Commands.CommandGGA
import NMEA.Commands.CommandNMEA
import NMEA.Enumerations.CommandsName
import Stats.StatsWindowContent
import UI.Bundels.MenuBundle
import UI.`Compose Functions`.AxisMenuAndText
import UI.`Compose Functions`.ListOfCommands
import UI.`Compose Functions`.mainWindowContent
import UI.`Helper Functions`.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import java.io.File
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    var openStatsWindow by remember { mutableStateOf(false) }
    var commandsForStats by remember { mutableStateOf<List<CommandGGA>>(emptyList()) }
    Window(
        onCloseRequest = ::exitApplication,
        enabled = !openStatsWindow,
        title = "Compose for Desktop",
        resizable = false,
        state = rememberWindowState(width = 1280.dp, height = 720.dp)
    ) { mainWindowContent {open, list ->
        openStatsWindow = open
        commandsForStats = list} }
    if(openStatsWindow){
        Window(
            onCloseRequest = {openStatsWindow = false},
            title = "Statistics",
            resizable = false,
            state = rememberWindowState(width = 1280.dp, height = 720.dp)
        ) { StatsWindowContent(commandsForStats) }
    }
}
