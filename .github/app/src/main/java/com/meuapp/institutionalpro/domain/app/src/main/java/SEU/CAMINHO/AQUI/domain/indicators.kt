package SEU.PACKAGE.AQUI.domain

import kotlin.math.abs

object Indicators {
  fun ema(values: List<Double>, period: Int): List<Double?> {
    if (values.size < period) return emptyList()
    val k = 2.0 / (period + 1.0)
    val out = MutableList<Double?>(values.size) { null }

    val sma = values.take(period).average()
    out[period - 1] = sma

    for (i in period until values.size) {
      val prev = out[i - 1]!!
      out[i] = prev * (1 - k) + values[i] * k
    }
    return out
  }

  fun rsi(closes: List<Double>, period: Int = 14): Double? {
    if (closes.size < period + 1) return null
    var gains = 0.0
    var losses = 0.0
    for (i in closes.size - period until closes.size) {
      val diff = closes[i] - closes[i - 1]
      if (diff >= 0) gains += diff else losses += abs(diff)
    }
    if (losses == 0.0) return 100.0
    val rs = gains / losses
    return 100.0 - (100.0 / (1.0 + rs))
  }

  fun tripleCrossTrend(closes: List<Double>): String {
    val e4 = ema(closes, 4)
    val e9 = ema(closes, 9)
    val e18 = ema(closes, 18)
    val last = closes.lastIndex
    val a = e4.getOrNull(last)
    val b = e9.getOrNull(last)
    val c = e18.getOrNull(last)
    if (a == null || b == null || c == null) return "NEUTRA"
    return when {
      a > b && b > c -> "ALTA"
      a < b && b < c -> "BAIXA"
      else -> "NEUTRA"
    }
  }
}
