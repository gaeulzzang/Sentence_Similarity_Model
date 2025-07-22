package com.sentence.similarity.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.sentence.similarity.R

@Composable
fun BaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = dimensionResource(R.dimen.tf_border_width),
                color = Color.Gray,
                shape = RoundedCornerShape(dimensionResource(R.dimen.tf_radius))
            ),
        value = value,
        onValueChange = { onValueChange(it) },
        placeholder = {
            Text(text = placeholder)
        },
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions
    )
}

@Preview(showBackground = true)
@Composable
fun BaseTextFieldPreview() {
    BaseTextField(
        value = "내용",
        onValueChange = {},
        placeholder = "힌트"
    )
}
