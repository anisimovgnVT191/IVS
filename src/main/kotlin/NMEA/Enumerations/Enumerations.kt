package NMEA.Enumerations

enum class Direction{
    North, South, West, East
}
enum class CommandsName{
    GSA, GSV, GGA, RMC, Report, All, ETK
}
enum class Protocols{
    GPS, ETK
}
enum class StatsParams{
    SolutionLatitude, SolutionLongitude, SolutionMSL
}