package NMEA.Commands

class CommandGSA(commandStr: String): CommandNMEA(commandStr){
    override val messageID = commandsList.first()!!
    val mode1: Char = commandsList[1]?.get(0)?:' '
    val mode2: Int = commandsList[2]?.toIntOrNull()?:0
    val satelliteUsed: List<Int>
    val PDOP: Double = commandsList[15]?.toDoubleOrNull()?:0.0
    val HDOP: Double = commandsList[16]?.toDoubleOrNull()?:0.0
    val VDOP: Double  = commandsList.last()?.split("*")?.first()?.toDoubleOrNull()?:0.0
    val checkSum:Int? = commandsList.last()?.split("*")?.get(1)?.toInt(16)

    init{
        val resultList: MutableList<Int> = mutableListOf()
        for(i in 0..11){
            resultList.add(
                commandsList[3+i]?.toIntOrNull()?:0
            )
        }
        satelliteUsed = resultList
    }

    override fun toString(): String {
        var result = """Message ID = $messageID
            |Mode1 = $mode1
            |Mode2 = $mode2
        """.trimMargin()
        satelliteUsed.forEach {
            result += "\nSatellite Used = $it"
        }
        result += "\n"
        result += """PDOP = $PDOP
            |HDOP = $HDOP
            |VDOP = $VDOP
        """.trimMargin()
        return "$result\nChecksum = $checkSum"
    }
}

