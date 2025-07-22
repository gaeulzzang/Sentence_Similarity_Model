package com.sentence.similarity.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.sentence.similarity.R

@Composable
fun SliderWithLabel(
    nValue: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 0..10
) {
    Column {
        Text(
            text = stringResource(R.string.slider_label, nValue),
            fontSize = dimensionResource(R.dimen.sub_header_size).value.sp,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = nValue.toFloat(),
            onValueChange = { newValue ->
                onValueChange(newValue.toInt())
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SliderWithLabelPreview() {
    SliderWithLabel(
        nValue = 5,
        onValueChange = {},
        range = 0..10
    )
}
