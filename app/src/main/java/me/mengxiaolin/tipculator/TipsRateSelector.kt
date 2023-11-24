package me.mengxiaolin.tipculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.mengxiaolin.tipculator.ui.theme.Typography

@Composable
fun TipsRateSelector(
    value: Int,
    onValueChanged: (Int)-> Unit,
    isRoundToDollar: Boolean,
    onIsRoundToDollarChanged: (Boolean)->Unit
) {
    Column(
        Modifier
            .padding(12.dp)
            .fillMaxWidth()) {
        Text(text = stringResource(id = R.string.tips_rate_label), style = Typography.h5)
        Row(modifier= Modifier.fillMaxWidth()) {
            Slider(
                value = value.toFloat(),
                onValueChange = {onValueChanged((it+0.5f).toInt()) },
                valueRange = 10.0f..20.0f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(0.75f)
            )
            Text(
                text = "$value%",
                style = Typography.h6,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
        Row(modifier = Modifier.padding(top=8.dp)) {
            Checkbox(checked = isRoundToDollar, onCheckedChange = onIsRoundToDollarChanged)
            Text(
                stringResource(id = R.string.is_round_to_dollar_label),
                style = Typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}