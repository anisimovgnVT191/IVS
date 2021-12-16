package NMEA.Commands

import NMEA.Enumerations.Direction
import NMEA.Extensions.toDegreesOrNull
import NMEA.Extensions.toDirection
import java.sql.Date
import java.sql.Time

var count:Int = 0
class Report(commandString: String):CommandNMEA(commandString), Position {
    override val messageID: String by lazy { extractMessageName() }
    val device_id: String? = commandsList[1]
    val date: Date?  = commandsList[2]?.let {
        Date.valueOf("20${it.substring(4, 6)}-${it.substring(2, 4)}-${it.substring(0, 2)}")
    }
    override val timeUTC: Time? by lazy { extractUTCTime(3, commandsList) }
    override val latitude: Double = commandsList[4]?.toDegreesOrNull()?:0.0
    override val indicatorNS: Direction? = commandsList[5]?.toDirection()
    override val longitude: Double? = commandsList[6]?.toDegreesOrNull()?:0.0
    override val indicatorEW: Direction? = commandsList[7]?.toDirection()
    val speed: Int? = commandsList[8]?.toIntOrNull()
    val course: Int? = commandsList[9]?.toIntOrNull()
    val altitude: Int? = commandsList[10]?.toIntOrNull()
    val odometer: Int? = commandsList[11]?.toIntOrNull()
    val io_status = commandsList[12]?.toIntOrNull()
    val event_id = commandsList[13]?.toIntOrNull()
    val ain1 = commandsList[14]?.toDoubleOrNull()
    val ain2 = commandsList[15]?.toDoubleOrNull()
    val fix_mode = commandsList[16]?.toIntOrNull()
    val glonasssat_no = commandsList[17]?.toIntOrNull()
    val gpssat_no = commandsList[18]?.toIntOrNull()
    val hdop= commandsList[19]?.toIntOrNull()


    override fun toString() ="""Message ID = $messageID
        |DEVICE_ID = $device_id
        |DATE = $date
        |TIME = $timeUTC
        |LATITUDE = $latitude
        |N/S = $indicatorNS
        |LONGITUDE = $longitude
        |E/W = $indicatorEW
        |SPEED = $speed
        |COURSE = $course
        |ALTITUDE = $altitude
        |ODOMETER = $odometer
        |IO_STATUS = $io_status
        |EVENT_ID = $event_id
        |AIN1 = $ain1
        |AIN2 = $ain2
        |FIX_MODE = $fix_mode
        |GLONASSSAT_NO = $glonasssat_no
        |GPSSAT_NO = $gpssat_no
        |HDOP = $hdop
    """.trimMargin()
}