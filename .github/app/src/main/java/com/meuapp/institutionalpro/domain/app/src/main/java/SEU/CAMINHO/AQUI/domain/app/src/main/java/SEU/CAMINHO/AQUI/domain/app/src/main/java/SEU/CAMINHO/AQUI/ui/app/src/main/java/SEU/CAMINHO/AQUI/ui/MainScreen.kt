package SEU.PACKAGE.AQUI.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import SEU.PACKAGE.AQUI.domain.*

@Composable
fun MainScreen(engine: InstitutionalEngine = InstitutionalEngine()) {
  var symbol by remember { mutableStateOf("KAITOUSDT") }
  var tf by remember { mutableStateOf(Tf.M5) }
  var result by remember { mutableStateOf<Plan?>(null) }

  Column(Modifier.fillMaxSize().padding(16.dp)) {
    Text("Institutional PRO (MVP)", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
      value = symbol,
      onValueChange = { symbol = it },
      label = { Text("Ativo") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf(Tf.M1, Tf.M5, Tf.M15, Tf.M30, Tf.H1, Tf.H4).forEach {
        FilterChip(selected = tf == it, onClick = { tf = it }, label = { Text(it.name) })
      }
    }

    Spacer(Modifier.height(12.dp))
    Button(onClick = {
      val candles = FakeData.sampleCandles()
      val flow = FlowSnapshot(oiDelta = 0.4, funding = -0.7, cvdTotal = 0.2)
      result = engine.analyze(InstitutionalEngine.Request(symbol, tf, candles, flow))
    }) { Text("Analisar (MVP)") }

    Spacer(Modifier.height(16.dp))

    result?.let { plan ->
      Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
          Text("Sinal: ${plan.side}", style = MaterialTheme.typography.titleMedium)
          Text(plan.thesis)
          Spacer(Modifier.height(8.dp))
          Text("Entrada: ${plan.entry?.start} – ${plan.entry?.endInclusive}")
          Text("SL: ${plan.sl}")
          Text("TPs: ${plan.tps.joinToString()}")
          Text("RR: ${plan.rr} | Validade: ${plan.validityMinutes} min")

          if (plan.warnings.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Alertas:", style = MaterialTheme.typography.titleSmall)
            plan.warnings.forEach { Text("• $it") }
          }

          if (plan.confirmations.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Confirmações:", style = MaterialTheme.typography.titleSmall)
            plan.confirmations.forEach { Text("• $it") }
          }
        }
      }
    }
  }
}
