package NMEA.Extensions
import NMEA.Enumerations.Direction
import java.lang.IllegalArgumentException

fun String.toDirection(): Direction {
    if(this.length != 1)
        throw IllegalArgumentException("String $this can`t be representation of Direction")
    return when(this[0]){
        'N'-> {
            Direction.North
        }
        'S' -> {
            Direction.South
        }
        'W' -> {
            Direction.West
        }
        'E' -> {
            Direction.East
        }
        else -> {
            throw IllegalArgumentException("String $this can`t be representation of Direction")
        }
    }
}

fun String.toDegreesOrNull():Double?{
    val regex = """[0-9]{2}[.]{1}[0-9]{1,}""".toRegex()
    val minutes = regex.find(this)?.value?:let {
        return@toDegreesOrNull null
    }
    val degrees = this.substringBefore(minutes)

    return degrees.toDouble() + minutes.toDouble()/60
}