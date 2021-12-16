package NMEA.Commands

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import java.io.File
import java.sql.Date
import java.sql.Time

class CommandETK(row: Map<String, String>, commandString: String = ""): CommandNMEA(commandStr = commandString), Position {
    override val messageID = "ETK"
    override val timeUTC: Time?
    val date: Date?
    val speed: Int?
    override val longitude: Double?
    override val latitude: Double?
    val address: String
    override val indicatorEW = null
    override val indicatorNS = null

    init {
        val dateTime = row["Время"]?.split(" ")
        val speed_ = row["Скорость"]?.split(" ")?.get(0)
        val coordinates = row["Координаты"]?.split(",")
        val address_ = row["Положение"]

        date = Date.valueOf(dateTime?.get(0)?:"")
        timeUTC = Time.valueOf(dateTime?.get(1)?:"")
        speed = speed_?.toIntOrNull()?:0
        latitude = coordinates?.getOrNull(0)?.toDoubleOrNull()?:0.0
        longitude = coordinates?.getOrNull(1)?.toDoubleOrNull()?:0.0
        address = address_?:""
    }

    companion object {
        suspend fun readListOfCommandsFromCSV(filepath: String): List<CommandETK> {
            val resultList = mutableListOf<CommandETK>()
            try{
                val file = File(filepath)
                val container = mutableListOf<Map<String, String>>()
                csvReader{
                    this.delimiter = ';'
                }.openAsync(file){
                    readAllWithHeaderAsSequence().asFlow().collect { row ->
                        resultList.add(CommandETK(row))
                    }
                }
                return resultList
            }catch(e: Throwable){
                println(e.message)
            }
            return emptyList()
        }
    }

    override fun toString()  = """Имя: ЕТК
        |Дата = $date
        |Время $timeUTC
        |Скорость  = $speed км/ч
        |Широта = $latitude
        |Долгота  = $latitude
    """.trimMargin()
}