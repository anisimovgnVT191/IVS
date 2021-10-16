package NMEA.Commands
import NMEA.Enumerations.CommandsName
import NMEA.Enumerations.Direction
import java.io.File
import java.lang.IllegalArgumentException
import java.sql.Date
import java.sql.Time

sealed class CommandNMEA(commandStr: String){
    val commandsList: List<String?> = commandStr.split(",").map { it.ifBlank { null } }
    abstract val messageID: String
    abstract val timeUTC: Time
    abstract val latitude: Double
    abstract val longitude: Double
    abstract val indicatorNS: Direction
    abstract val indicatorEW: Direction

    protected fun extractMessageName():String{
        return commandsList[0]!!.substring(1)
    }

    protected fun extractUTCTime():Time{
        val regex = """[0-9]{2}""".toRegex()
        val (hours, minutes, seconds) = regex.findAll(commandsList[1]!!).map { it.value.toInt() }.toList()
        return Time.valueOf("$hours:$minutes:$seconds")
    }

    companion object{
        fun commandFromString(str: String):CommandNMEA =
            with(str){
                when{
                    contains("GPGGA") -> CommandGGA(this)
                    contains("GPRMC") -> CommandRMC(this)
                    else -> throw IllegalArgumentException("Unknown command in string")
                }
            }

        fun readListOfCommandsFromFile(filePath: String, commandType: CommandsName):List<CommandNMEA>{
            val file = File(filePath)
            val list: MutableList<CommandNMEA> = mutableListOf()

            val keySubstring = if(commandType == CommandsName.All) "" else commandType.toString()

            file.forEachLine {
                if(it.contains(keySubstring))
                    list.add(commandFromString(it))
            }

            return list.toList()
        }
    }
}