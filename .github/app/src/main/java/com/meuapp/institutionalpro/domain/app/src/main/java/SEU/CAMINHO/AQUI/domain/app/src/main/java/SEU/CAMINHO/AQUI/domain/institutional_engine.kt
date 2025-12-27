package SEU.PACKAGE.AQUI.domain

import kotlin.math.max
import kotlin.math.min

class InstitutionalEngine {

  data class Request(
    val symbol: String,
    val timeframe: Tf,
    val candles: List<Candle>,
    val flow: FlowSnapshot? = null,
    val ignoreLastCandleOn1m: Boolean = true
  )

  fun analyze(req: Request): Plan {
    var candles = req.candles
    val warnings = mutableListOf<String>()
    val confs = mutableListOf<String>()
    val flow = req.flow ?: FlowSnapshot()

    // Regra: 1m ignora último candle (não fechado)
    if (req.timeframe == Tf.M1 && req.ignoreLastCandleOn1m && candles.size > 30) {
      candles = candles.dropLast(1)
      warnings += "1m: último candle ignorado (não fechado)."
    }

    val closes = candles.map { it.close }
    val highs = candles.map { it.high }
    val lows = candles.map { it.low }

    val trend = Indicators.tripleCrossTrend(closes)
    val rsi14 = Indicators.rsi(closes, 14)

    // topo/fundo local (range recente)
    val lookback = min(60, candles.size)
    val recentHigh = highs.takeLast(lookback).maxOrNull()!!
    val recentLow  = lows.takeLast(lookback).minOrNull()!!
    val range = max(1e-9, recentHigh - recentLow)
    val lastClose = closes.last()
    val pos = (lastClose - recentLow) / range

    if (pos > 0.90) warnings += "Topo local: entrada só com confluência forte (fluxo + confirmação)."
    if (pos < 0.10) warnings += "Fundo local: entrada só com confluência forte (fluxo + confirmação)."

    // fluxo (heurísticas iniciais)
    flow.oiDelta?.let {
      if (it > 0) confs += "OI delta positivo (entrada de posição)."
      if (it < 0) warnings += "OI delta negativo (fechamento de posição / alívio)."
    }
    flow.cvdTotal?.let {
      if (it > 0) confs += "CVD total positivo (agressão compradora)."
      if (it < 0) confs += "CVD total negativo (agressão vendedora)."
    }
    flow.funding?.let {
      if (it < 0) confs += "Funding negativo (potencial de squeeze se romper)."
      if (it > 0) warnings += "Funding positivo (cuidado com squeeze reverso)."
    }

    // scalp: validade 60 min para 1m/5m
    val validity = if (req.timeframe == Tf.M1 || req.timeframe == Tf.M5) 60 else 240

    val thesis = "Trend 4/9/18: $trend. RSI14=${rsi14?.let { String.format(\"%.1f\", it) } ?: "n/a"}."

    var side = Side.WAIT
    var entry: ClosedFloatingPointRange<Double>? = null
    var sl: Double? = null
    var tps = emptyList<Double>()
    var rr = 0.0

    // níveis estilo fib com base no range recente
    val pull618 = recentHigh - range * 0.618
    val pull50  = recentHigh - range * 0.50
    val pull786 = recentHigh - range * 0.786
    val ext1272 = recentHigh + range * 0.272
    val ext1618 = recentHigh + range * 0.618

    fun rrLong(e: Double, stop: Double, tp: Double): Double {
      val risk = e - stop
      val reward = tp - e
      return if (risk <= 0) 0.0 else reward / risk
    }

    fun rrShort(e: Double, stop: Double, tp: Double): Double {
      val risk = stop - e
      val reward = e - tp
      return if (risk <= 0) 0.0 else reward / risk
    }

    // LONG
    if (trend == "ALTA" && (rsi14 == null || rsi14 < 80 || (flow.cvdTotal ?: 0.0) > 0)) {
      side = Side.LONG
      entry = pull618..pull50
      sl = pull786
      tps = listOf(ext1272, ext1618)
      rr = rrLong(entry.endInclusive, sl, tps.first())
      confs += "Alta: pullback 0.5–0.618, SL 0.786, TPs em extensões."
    }
    // SHORT
    else if (trend == "BAIXA" && (rsi14 == null || rsi14 > 20 || (flow.cvdTotal ?: 0.0) < 0)) {
      side = Side.SHORT
      entry = pull50..pull618
      sl = pull786
      val tp1 = recentLow + range * 0.25
      val tp2 = recentLow + range * 0.10
      tps = listOf(tp1, tp2)
      rr = rrShort(entry.start, sl, tps.first())
      confs += "Baixa: pullback, confirmar com fluxo."
    }

    // Regra hard: RR >= 2:1
    if (side != Side.WAIT && rr < 2.0) {
      warnings += "RR ${"%.2f".format(rr)} < 2:1 — rebaixado para WAIT."
      side = Side.WAIT
      entry = null
      sl = null
      tps = emptyList()
      rr = 0.0
    }

    if (req.timeframe == Tf.M1 || req.timeframe == Tf.M5) {
      confs += "SCALP: validade 1h. Executar só após candle fechar + confirmação (fluxo)."
    }

    return Plan(req.symbol, req.timeframe, side, thesis, entry, sl, tps, (kotlin.math.round(rr*100)/100.0), validity, warnings, confs)
  }
}
