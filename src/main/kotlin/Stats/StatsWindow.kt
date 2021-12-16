package Stats

import NMEA.Commands.CommandGGA
import NMEA.Enumerations.StatsParams
import UI.Bundels.MenuBundle
import UI.`Compose Functions`.AxisMenuAndText
import UI.`Compose Functions`.CategoryMenu
import UI.`Helper Functions`.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File

@Composable
fun StatsWindowContent(commands: List<CommandGGA>){
    val stats = Stats(commands)
    var imagePath by remember { mutableStateOf("defaultPng.png") }
    var selectedSolution by remember { mutableStateOf(StatsParams.SolutionLatitude) }
    Row(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxHeight().padding(8.dp).border(1.dp, Color.Black).weight(0.5F)){
            Text(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black),
                text = "Статистика",
                textAlign = TextAlign.Center,
                fontSize = 30.sp)
            Text(modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 4.dp),
                text = "$stats",
                textAlign = TextAlign.Left,)
        }

        Column(modifier = Modifier.fillMaxHeight().padding(8.dp).border(1.dp, Color.Black).weight(1.5F)){
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
                    Image(painter = painterResource("defaultPng.png"),
                        contentDescription = "image",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier)
                }

            }
            Spacer(modifier = Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1F)) {
                    CategoryMenu(Modifier.padding(start = 8.dp, end = 8.dp), MenuBundle("timeUTC", listOf("timeUTC")), {})
                }
                var loadingOfPlot by remember { mutableStateOf(false) }
                Column(modifier = Modifier.weight(1F)){
                    Button(
                        onClick = {
                            loadingOfPlot = true
                            GlobalScope.async {
                                val statsPlot = StatsPlot(commands, selectedSolution, stats)
                                statsPlot.showPlot { imagePath =it; it }
                                loadingOfPlot = false
                            }
                        },enabled = true,
                        modifier = Modifier.fillMaxWidth().padding(5.dp)
                    ){Text("Построить график")}
                    if(loadingOfPlot)
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                Column(modifier = Modifier.weight(1F)) {
                    CategoryMenu(Modifier.padding(start = 8.dp, end = 8.dp), MenuBundle(selectedSolution.name, listOf(StatsParams.SolutionLatitude.name,
                        StatsParams.SolutionLongitude.name,
                        StatsParams.SolutionMSL.name))) {
                            selectedSolution = when(it){
                                StatsParams.SolutionLatitude.name -> StatsParams.SolutionLatitude
                                StatsParams.SolutionLongitude.name -> StatsParams.SolutionLongitude
                                StatsParams.SolutionMSL.name -> StatsParams.SolutionMSL
                                else -> selectedSolution
                            }
                    }
                }
            }
        }
    }
}
