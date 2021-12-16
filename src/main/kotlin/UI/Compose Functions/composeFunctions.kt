package UI.`Compose Functions`


import NMEA.Commands.CommandETK
import NMEA.Commands.CommandGGA
import NMEA.Commands.CommandNMEA
import NMEA.Commands.CommandRMC
import NMEA.Enumerations.CommandsName
import NMEA.Enumerations.Protocols
import Stats.Stats
import UI.Bundels.MenuBundle
import UI.`Helper Functions`.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun AxisMenuAndText(
    label: String,
    mutableData: String,
    menuContent: MenuBundle,
    onDataChanged: (String) -> Unit,
    onMenuItemChanged:(String) -> Unit){
    var isValid = false
    var expanded by remember { mutableStateOf(false) }
    if(mutableData.isNotBlank())
        isValid = true
    CategoryMenu(Modifier.padding(start = 8.dp, end = 8.dp), menuContent, onMenuItemChanged)
    Text(text = "$label:", modifier = Modifier.padding(start = 8.dp, end = 8.dp), )
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
fun ListOfCommands(modifier: Modifier, filePath: String, menuContent: MenuBundle, onListChange: () -> List<CommandNMEA>, onSizeChange: (List<CommandNMEA>) -> Int, onCommandChange: (CommandsName) -> Unit){
    var size by remember { mutableStateOf(0) }
    var chosenCommand by remember { mutableStateOf(CommandsName.All) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
    ){
        LazyColumn(state = listState, horizontalAlignment = Alignment.CenterHorizontally) {
            stickyHeader() {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.border(1.dp, Color.Black)
                        .fillParentMaxWidth()
                        .background(Color.White)
                ) {
                    CategoryMenu(Modifier.weight(1F).padding(8.dp), menuContent) {
                        chosenCommand = when (it) {
                            "All" -> CommandsName.All
                            "GGA" -> CommandsName.GGA
                            "RMC" -> CommandsName.RMC
                            "Report" -> CommandsName.Report
                            "GSV" -> CommandsName.GSV
                            "GSA" -> CommandsName.GSA
                            "ETK" -> CommandsName.ETK
                            else -> chosenCommand
                        }
                        onCommandChange(chosenCommand)
                        coroutineScope.launch { listState.scrollToItem(index = 0) }
                    }
                    Text(text = "Info", fontSize = 25.sp, modifier = Modifier.weight(1F), textAlign = TextAlign.Center)
                    Text(text = "Count of items = $size", modifier = Modifier.weight(1F))

                }
            }
            if (filePath.isNotBlank()) {
                val commands = onListChange().filter {
                    chosenCommand.toString().uppercase() in it.messageID || chosenCommand == CommandsName.All
                }
                size = onSizeChange(commands)
                itemsIndexed(commands) { index, item ->
                    Text(text = (index + 1).toString(), textAlign = TextAlign.Center)
                    Text(
                        text = item.toString(),
                        modifier = Modifier.border(1.dp, Color.Black).fillMaxWidth().padding(8.dp)
                    )
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().padding(top = 74.dp),
            adapter = rememberScrollbarAdapter(
                scrollState = listState
            )
        )
    }
}

@Composable
fun CategoryMenu(modifier: Modifier, menuContent: MenuBundle, onItemClicked: (String) -> Unit){
    var expanded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(menuContent.name) }
    
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
            for(i in menuContent.items){
                Text(
                    text = i?:"",
                    modifier = Modifier.clickable {
                        expanded = false
                        name = i?:""
                        onItemClicked(i?:"")
                    }.padding(start = 8.dp, end = 8.dp))
            }
        }
    }
}

@Composable
fun mainWindowContent(openStats: (Boolean, List<CommandGGA>) -> Unit){
    var beginTime by remember { mutableStateOf("00:00:00 2000-01-01") }
    var endTime by remember { mutableStateOf("23:59:59 2050-12-31")}
    var imagePath by remember { mutableStateOf("defaultPng.png") }
    var commandsFile by remember { mutableStateOf("") }
    var expand by remember { mutableStateOf(false) }
    var commandsListSize by remember { mutableStateOf(0) }
    var commandsList by remember { mutableStateOf(listOf<CommandNMEA>()) }
    var chosenCommand by remember { mutableStateOf(CommandsName.All) }
    var xAxis by remember { mutableStateOf("") }
    var yAxis by remember { mutableStateOf("") }
    var loadingOfFile by remember { mutableStateOf(false) }
    var selectedProtocol by remember { mutableStateOf(Protocols.GPS) }

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
                onListChange = {commandsList},
                onSizeChange = { commandsListSize = it.size; commandsListSize},
                onCommandChange = { chosenCommand = it },
                menuContent = MenuBundle(name = "All", items = CommandsName.values().toList().map{it.toString()}.sorted())
            )

            Column(modifier = Modifier
                .fillMaxHeight()
                .width(500.dp)
                .padding(16.dp)
                .weight(0.5F)
                .border(1.dp, Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                CategoryMenu(Modifier.padding(start = 8.dp, end = 8.dp), MenuBundle(selectedProtocol.name, listOf(Protocols.GPS.name, Protocols.ETK.name))){
                    selectedProtocol = when(it){
                        "GPS" -> Protocols.GPS
                        "ETK" -> Protocols.ETK
                        else -> selectedProtocol
                    }
                }
                Button(onClick = {
                    openStats(true, commandsList.filter { it.messageID.contains(CommandsName.GGA.name) } as List<CommandGGA>)

                }, enabled = chosenCommand == CommandsName.GGA){Text("Stats")}
                Button(onClick = {
                    commandsFile = getFileFromDialog()?.apply {
                        loadingOfFile = true
                        GlobalScope.launch {
                            commandsList = when(selectedProtocol){
                                Protocols.ETK -> CommandETK.readListOfCommandsFromCSV(this@apply)
                                Protocols.GPS -> CommandNMEA.readListOfCommandsFromFile(this@apply, commandType = CommandsName.All)
                            }
                            loadingOfFile = false
                        }
                    }?:""
                }){ Text("Import")}

                if(loadingOfFile)
                    CircularProgressIndicator()
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
                    Column(modifier = Modifier.weight(1F)){
                        AxisMenuAndText("Начало интервала", beginTime, menuContent = MenuBundle(name = "x", items = axisMenuItems(chosenCommand)), onDataChanged = { beginTime = it }, onMenuItemChanged = { xAxis = it})
                    }
                    var loadingOfPlot by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1F)){
                        Button(
                            onClick = {
                                loadingOfPlot = true
                                GlobalScope.async {
                                    showChart(
                                        commandsList = commandsList,
                                        chosenCommand = chosenCommand,
                                        xAxisS = xAxis,
                                        yAxis = yAxis,
                                        beginTime = beginTime,
                                        endTime = endTime
                                    ) { imagePath = it; println(imagePath); it }
                                    loadingOfPlot = false
                                }
                            },enabled = checkButtonEnabled(chosenCommand),
                            modifier = Modifier.fillMaxWidth().padding(5.dp)
                        ){Text("Построить график")}
                        if(loadingOfPlot)
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    Column(modifier = Modifier
                        .weight(1F), verticalArrangement = Arrangement.Center){
                        AxisMenuAndText("Конец интервала", endTime, menuContent = MenuBundle(name = "y", items = axisMenuItems(chosenCommand)), onDataChanged = { endTime = it}, onMenuItemChanged = { yAxis =it })
                    }
                }
            }
        }
    }
}





