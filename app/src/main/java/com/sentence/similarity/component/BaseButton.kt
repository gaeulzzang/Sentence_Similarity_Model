package com.sentence.similarity.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.sentence.similarity.R

@Composable
fun BaseButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.btn_horizontal_padding),
            vertical = dimensionResource(R.dimen.btn_vertical_padding)
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.btn_radius)),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.White
        )
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun BaseButtonPreview() {
    BaseButton(
        text = "검색",
        onClick = {},
        enabled = true
    )
}
