package NMEA.Commands
import NMEA.Enumerations.CommandsName
import NMEA.Enumerations.Direction
import java.io.File
import java.lang.IllegalArgumentException
import java.sql.Date
import java.sql.Time

interface Position{
    abstract val timeUTC: Time?
    abstract val latitude: Double?
    abstract val longitude: Double?
    abstract val indicatorNS: Direction?
    abstract val indicatorEW: Direction?

    fun extractUTCTime(index: Int = 1, list: List<String?>):Time?{
        val regex = """[0-9]{2}""".toRegex()
        if(list[index] == null)
            return null
        val (hours, minutes, seconds) = regex.findAll(list[index]!!).map { it.value.toInt() }.toList()
        return Time.valueOf("$hours:$minutes:$seconds")
    }
}
sealed class CommandNMEA(commandStr: String){
    val commandsList: List<String?> = commandStr.split(",").map { it.ifBlank { null } }
    abstract val messageID: String

    protected fun extractMessageName():String{
        return commandsList[0]!!.substring(1)
    }

    companion object{
        fun commandFromString(str: String):CommandNMEA =
            with(str){
                when{
                    contains("GPGGA") -> CommandGGA(this)
                    contains("GPRMC") -> CommandRMC(this)
                    contains("REPORT") -> Report(this)
                    contains("GPGSV") -> CommandGSV(this)
                    contains("GPGSA") ->CommandGSA(this)
                    else -> throw IllegalArgumentException("Unknown command in string")
                }
            }


        suspend fun readListOfCommandsFromFile(filePath: String, commandType: CommandsName):List<CommandNMEA>{
            val file = File(filePath)
            val list: MutableList<CommandNMEA> = mutableListOf()

            val keySubstring = if(commandType == CommandsName.All) "" else commandType.toString()

            file.forEachLine {
                if(it.contains(keySubstring))
                    try {
                        list.add(commandFromString(it))
                    }catch (e: IllegalArgumentException){

                    }
            }

            return list.toList()
        }
    }
}