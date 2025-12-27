package SEU.PACKAGE.AQUI.domain

enum class Side { LONG, SHORT, WAIT }
enum class Tf { M1, M5, M15, M30, H1, H4, D1 }

data class Candle(
  val ts: Long,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Double
)

data class FlowSnapshot(
  val oi: Double? = null,
  val oiDelta: Double? = null,
  val funding: Double? = null,
  val takerBuy: Double? = null,
  val takerSell: Double? = null,
  val cvdSpot: Double? = null,
  val cvdFut: Double? = null,
  val cvdTotal: Double? = null
)

data class Plan(
  val symbol: String,
  val timeframe: Tf,
  val side: Side,
  val thesis: String,
  val entry: ClosedFloatingPointRange<Double>?,
  val sl: Double?,
  val tps: List<Double>,
  val rr: Double,
  val validityMinutes: Int,
  val warnings: List<String>,
  val confirmations: List<String>
)
