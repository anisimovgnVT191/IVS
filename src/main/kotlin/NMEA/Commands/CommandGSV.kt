package NMEA.Commands


class CommandGSV(commandStr: String): CommandNMEA(commandStr){
    override val messageID: String = commandsList.first()!!
    val numberOfMessages: Int = commandsList[1]?.toIntOrNull()?:0
    val messageNumber: Int = commandsList[2]?.toIntOrNull()?:0
    val satellitesInView: Int = commandsList[3]?.toIntOrNull()?:0
    val satellitesList: List<Satellite>
    val checkSum:Int? = commandsList.last()?.split("*")?.get(1)?.toInt(16)

    init{
        val resultingList: MutableList<Satellite> = mutableListOf()
        for (i in 0..satellitesInView){
            if(commandsList.size < 4+(i*4)+4)
                break
            val tmp = commandsList.subList(4+(i*4), 4+(i*4)+4)
            resultingList.add(
                Satellite(
                    ID = tmp[0]?.toIntOrNull()?:0,
                    elevation = tmp[1]?.toIntOrNull()?:0,
                    azimuth = tmp[2]?.toIntOrNull()?:0,
                    SNR = tmp[3]?.takeIf({ it.contains("*") })?.let {
                        it.split("*")
                        it.toIntOrNull()?:0
                    }?: tmp[3]?.toIntOrNull()?:0
                )
            )
        }
        satellitesList = resultingList.toList()
    }

    fun getSatelliteMap(): Map<Int, Satellite>{
        val result: MutableMap<Int, Satellite> = mutableMapOf()

        satellitesList.forEach {
            result[it.ID] = it
        }

        return result.toMap()
    }
    override fun toString(): String {
        var result = """Message ID = $messageID
        |Number of messages = $numberOfMessages
        |Message Number = $messageNumber
        |Satellites in View = $satellitesInView
    """.trimMargin()

        satellitesList.forEach {
            result += "\n$it"
        }
        result += "\nChecksum = $checkSum"
        return result
    }

}

data class Satellite(
    val ID: Int,
    val elevation: Int,
    val azimuth: Int,
    val SNR: Int
) {
    override fun toString() = """Satellite ID = $ID
        |Elevation = $elevation
        |Azimuth = $azimuth
        |SNR(C/No) = $SNR
    """.trimMargin()
}