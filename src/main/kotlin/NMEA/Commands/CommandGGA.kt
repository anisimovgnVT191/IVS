package NMEA.Commands

import NMEA.Enumerations.Direction
import NMEA.Extensions.toDegreesOrNull
import NMEA.Extensions.toDirection
import java.sql.Time

class CommandGGA(commandStr: String): CommandNMEA(commandStr){
    override val messageID: String by lazy { extractMessageName() }
    override val timeUTC: Time? by lazy { extractUTCTime() }
    override val latitude: Double = commandsList[2]?.toDegreesOrNull()?:0.0
    override val indicatorNS: Direction? = commandsList[3]?.toDirection()
    override val longitude: Double? = commandsList[4]?.toDegreesOrNull()?:0.0
    override val indicatorEW: Direction? = commandsList[5]?.toDirection()
    val positionFixIndicator:Short? = commandsList[6]?.toShort()
    val satellitesUsed:Short? = commandsList[7]?.toShort()
    val HDOP: Double? = commandsList[8]?.toDoubleOrNull()
    val altitudeMSL:Double? = commandsList[9]?.toDoubleOrNull()
    val units:Char? = commandsList[10]?.get(0)
    val geoidSeparation:Double? = commandsList[11]?.toDoubleOrNull()
    val units1:Char? = commandsList[12]?.get(0)
    val ageOfDiffCorr:Int? = commandsList[13]?.toIntOrNull()
    val diffRefStation:Int? = commandsList[14]?.split("*")?.get(0)?.toIntOrNull()
    val checkSum:Int? = commandsList[14]?.split("*")?.get(1)?.toInt(16)

//        init {
//            if(commandsList.size != 14) throw IllegalArgumentException("Exception in GGA init block")
//        }

    override fun toString(): String = """MessageID = $messageID
                |UTC Time = $timeUTC
                |Latitude = $latitude
                |N/S Indicator = $indicatorNS
                |Longitude = $longitude
                |E/W Indicator = $indicatorEW
                |Position Fix Indicator = $positionFixIndicator
                |Satellites Used = $satellitesUsed
                |HDOP = $HDOP
                |MSL Altitude = $altitudeMSL
                |Units = $units
                |Geoid Separation = ${geoidSeparation?.also {  }?:""}
                |Units = $units1
                |Age of Diff. Corr. = ${ageOfDiffCorr?.also {  }?:""}
                |Diff. Ref. Station ID  = ${diffRefStation?.also {  }?:""}
                |Checksum = $checkSum
            """.trimMargin()


}