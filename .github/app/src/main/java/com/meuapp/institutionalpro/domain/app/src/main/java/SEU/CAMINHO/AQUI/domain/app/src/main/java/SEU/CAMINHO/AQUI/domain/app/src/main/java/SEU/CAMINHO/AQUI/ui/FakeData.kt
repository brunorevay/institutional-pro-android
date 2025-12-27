package SEU.PACKAGE.AQUI.ui

import SEU.PACKAGE.AQUI.domain.Candle

object FakeData {
  fun sampleCandles(): List<Candle> {
    val out = mutableListOf<Candle>()
    var price = 0.60
    val now = System.currentTimeMillis()
    for (i in 0 until 120) {
      val open = price
      price += listOf(-0.002, 0.003, 0.001, -0.001, 0.002).random()
      val close = price
      val high = maxOf(open, close) + 0.002
      val low = minOf(open, close) - 0.002
      out += Candle(now - (120 - i) * 60_000L, open, high, low, close, (1000..5000).random().toDouble())
    }
    return out
  }
}
