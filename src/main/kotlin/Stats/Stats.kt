package Stats

import NMEA.Commands.CommandGGA
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.minus
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.timesAssign
import kotlin.math.abs
import kotlin.math.sqrt

class Stats(
    list: List<CommandGGA>
) {
    private val listGGA: List<CommandGGA> = list.filter {
        it.latitude != 0.0 && it.longitude != 0.0 && it.altitudeMSL != 0.0
    }
    val mean: D2Array<Double> by lazy { calculateMean()}
    val covMatrix: D2Array<Double> by lazy { calculateCovMatrix()}
    val SKO: D2Array<Double> by lazy {calculateSKO()}
    val corSKO: D2Array<Double> by lazy { calculateCorSKO() }
    val anomaly: D2Array<Int> by lazy { calculateAnomaly() }
    private fun calculateMean(): D2Array<Double>{
        val N = listGGA.size
        val res: D2Array<Double>  = mk.zeros(3, 1)

        listGGA.forEach {
            val Pn = d2ArrayFromCommand(it)
            res += Pn
        }
        res *= 1.0/N.toDouble()
        return res
    }
    private fun calculateCovMatrix():D2Array<Double>{
        val N = listGGA.size
        val res: D2Array<Double> = mk.zeros(3, 3)

        listGGA.forEach {
            val Pn = d2ArrayFromCommand(it)
            val PnDeviation = Pn - mean
            val PnDeviationT = mk.ndarray(mk[
                    mk[PnDeviation[0, 0], PnDeviation[1, 0], PnDeviation[2, 0]]
            ])
            res += PnDeviation.mult(PnDeviationT)
        }
        res *= 1.0/N.toDouble()
        return res
    }
    private fun calculateSKO():D2Array<Double>{
        return mk.ndarray(mk[
                mk[sqrt(covMatrix[0, 0])],
                mk[sqrt(covMatrix[1, 1])],
                mk[sqrt(covMatrix[2, 2])]
        ])
    }
    private fun calculateCorSKO():D2Array<Double>{
        val tmp = mk.zeros<Double>(3, 3)
        tmp[0,0] = 1.0/SKO[0,0]
        tmp[1, 1] = 1.0/SKO[1,0]
        tmp[2, 2] = 1.0/SKO[2, 0]
        val res = tmp*covMatrix
        return res*tmp
    }
    private fun calculateAnomaly():D2Array<Int>{
        val res = mk.zeros<Int>(3, 1)
        listGGA.forEach {
            val Pn = d2ArrayFromCommand(it)
            res[0, 0] += if(abs(mean[0,0]) > 3.0*Pn[0,0]) 1 else 0
            res[1, 0] += if(abs(mean[1,0]) > 3.0*Pn[1,0]) 1 else 0
            res[2, 0] += if(abs(mean[2,0]) > 3.0*Pn[2,0]) 1 else 0
        }
        return res
    }
    private fun d2ArrayFromCommand(command:CommandGGA) =
        with(command){mk.ndarray(mk[
                mk[this.latitude],
                mk[this.longitude],
                mk[this.altitudeMSL]
        ])
    }
    override fun toString(): String {
        val meanStr = """Усреднённые координаты ПНС:
            |Широта = ${mean[0, 0]}
            |Долгота = ${mean[1, 0]}
            |Высота = ${mean[2, 0]}
            |
        """.trimMargin()
        val covMatrixStr = """Ковариационная матрица:
            |$covMatrix
            |
        """.trimMargin()
        val skoStr = """Среднее квадратическое отклонение:
            |Широта = ${SKO[0, 0]}
            |Долгота = ${SKO[1, 0]}
            |Высота = ${SKO[2, 0]}
            |
        """.trimMargin()
        val corSKOStr = """Корреляционная матрица отклонений:
            |$corSKO
            |
        """.trimMargin()
        val anomalyStr = """Оценка количества аномальных отклонений
            |По широте = ${anomaly[0, 0]}
            |По долготе = ${anomaly[1, 0]}
            |По высоте = ${anomaly[2, 0]}
            |
        """.trimMargin()
        return meanStr + covMatrixStr + skoStr + corSKOStr + anomalyStr
    }
}

private fun D2Array<Double>.mult(second: D2Array<Double>):D2Array<Double>{
    val res = mk.zeros<Double>(3, 3)

    for(i in 0 until 3){
        for(j in 0 until 3){
            res[i, j] = this[i, 0] * second[0, j]
        }
    }
    return res
}