package UI.`Compose Functions`

import NMEA.Commands.CommandGGA
import NMEA.Commands.CommandNMEA
import NMEA.Commands.CommandRMC
import NMEA.Enumerations.CommandsName
import UI.`Helper Functions`.imageFromFile
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File
import kotlin.reflect.full.memberProperties


@Composable
fun AxisMenuAndText(label: String, mutableData: String, onDataChanged: (String) -> Unit){
    var isValid = false
    var expanded by remember { mutableStateOf(false) }
    if(mutableData.isNotBlank())
        isValid = true
    CategoryMenu(Modifier.padding(start = 8.dp, end = 8.dp), CommandsName.values().toList().map{it.toString()}, "x", { })
//    val test1:CommandNMEA = CommandGGA("\$GPGGA,114045.00,5035.249337,N,03635.241527,E,1,12,0.4,120.4,M,17.4,M,,*64")
//    val list = test1::class.memberProperties.map{
//        val regex = """.[a-z A-z]+:""".toRegex()
//        val res = regex.find(it.toString())?.value
//        res?.substring(1..res.length-2)
//    }
//    list.forEach {
//        println(it)
//    }
    Text(text = "$label:", modifier = Modifier.padding(start = 8.dp, end = 8.dp))
    TextField(
        value = mutableData,
        onValueChange = onDataChanged,
        maxLines = 1,
        placeholder = { Text("00:00:00") },
        isError = isValid,
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListOfCommands(modifier: Modifier, filePath: String, onListChange: () -> List<CommandNMEA>, onSizeChange: (List<CommandNMEA>) -> Int){
    var size by remember { mutableStateOf(0) }
    var chosenCommand by remember { mutableStateOf(CommandsName.All) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(state = listState, modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        stickyHeader() {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.border(1.dp, Color.Black)
                    .fillParentMaxWidth()
                    .background(Color.White)){
                CategoryMenu(Modifier.weight(1F).padding(8.dp), CommandsName.values().toList().map {it.toString()}.sorted(), "All"){
                    chosenCommand = when(it){
                        "All "-> CommandsName.All
                        "GGA" -> CommandsName.GGA
                        "RMC" -> CommandsName.RMC
                        else -> chosenCommand
                    }
                    coroutineScope.launch { listState.scrollToItem( index= 0 ) }
                }
                Text(text = "Info", fontSize = 25.sp, modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
                Text(text = "Count of items = $size", modifier = Modifier.weight(1F))

            }
        }
        if(filePath.isNotBlank()){
            val commands = onListChange().filter {  chosenCommand.toString() in it.messageID || chosenCommand == CommandsName.All }
            size = onSizeChange(commands)
            items(count = size){
                commands.forEachIndexed {index, it ->
                    Text(text = (index+1).toString(), textAlign = TextAlign.Center)
                    Text(text = it.toString(), modifier = Modifier.border(1.dp, Color.Black).fillMaxWidth().padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryMenu(modifier: Modifier, menuItems: List<String>, menuName: String, onItemClicked: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(menuName) }
    Box(modifier = modifier.border(1.dp, Color.Gray, shape = RoundedCornerShape(15))){
        Row(modifier = Modifier.clickable { expanded = true }.fillMaxWidth().padding(start = 16.dp)){
            Text(
                text = name,
                modifier = Modifier.weight(1F)
                    .align(Alignment.CenterVertically))
            Icon(bitmap = imageFromFile(File("src/main/resources/outline_expand_more_black_24dp.png")),
                contentDescription = null,
                modifier = Modifier.weight(1F))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier.scrollable(state = ScrollableState { 1F }, orientation = Orientation.Vertical )
        ){
            for(i in menuItems){
                Text(
                    text = i,
                    modifier = Modifier.clickable {
                        expanded = false
                        name = i
                        onItemClicked(i)
                    }.padding(start = 8.dp, end = 8.dp))
            }
        }
    }
}




