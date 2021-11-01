package NMEA.Commands
import NMEA.Enumerations.Direction
import NMEA.Extensions.toDirection
import java.sql.Date
import java.sql.Time

class CommandRMC(commandStr: String): CommandNMEA(commandStr){
    override val messageID: String by lazy { extractMessageName() }
    override val timeUTC: Time? by lazy { extractUTCTime() }
    val status: Char? = commandsList[2]?.get(0)
    override val latitude: Double? = commandsList[3]?.toDoubleOrNull()?:0.0
    override val indicatorNS: Direction? = commandsList[4]?.toDirection()
    override val longitude: Double? = commandsList[5]?.toDoubleOrNull()?:0.0
    override val indicatorEW: Direction? = commandsList[6]?.toDirection()
    val speedOverGround: Double? = commandsList[7]?.toDouble()?:0.0
    val courseOverGround: Double? = commandsList[8]?.toDoubleOrNull()?:0.0
    val date: Date? by lazy { getDate(9) }
    val magneticVariationDegress: Double? = commandsList[10]?.toDoubleOrNull()?:0.0
    val magneticVariationDiraction: Direction?  = commandsList[11]?.toDirection()
    val modeIndicator: Char? = commandsList[12]?.split("*")?.get(0)?.get(0)
    val checksum: Int? = commandsList[12]?.split("*")?.get(1)?.toInt(16)

    private fun getDate(index: Int): Date? {
        val regex = """[0-9]{2}""".toRegex()

        if(commandsList[index] == null)
            return null
        val (days, months, years) = regex.findAll(commandsList[index]!!).map { it.value.toInt() }.toList()
        return Date.valueOf("${if(years > 80) 1900 + years else 2000 + years}-$months-$days")
    }
    override fun toString() = """Message ID = $messageID
            |UTC Time = $timeUTC
            |Status = $status
            |Latitude = $latitude
            |N/S Indicator = $indicatorNS
            |Longitude = $longitude
            |E/W Indicator = $indicatorEW
            |Speed over ground = $speedOverGround
            |Course over ground = $courseOverGround
            |Date = $date
            |Magnetic Variation (degrees) = ${magneticVariationDegress?.also {  }?:""}
            |Magnetic Variation (direction) = ${magneticVariationDiraction?.also {  }?:""}
            |Indicator Mode = $modeIndicator
            |Checksum = $checksum
        """.trimMargin()
}