package com.sentence.similarity.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sentence.similarity.R
import com.sentence.similarity.component.BaseButton
import com.sentence.similarity.component.BaseTextField
import com.sentence.similarity.component.LoadingScreen
import com.sentence.similarity.component.SliderWithLabel
import com.sentence.similarity.type.SearchResult
import com.sentence.similarity.ui.theme.SentenceSimilarityTheme

@Composable
fun SentenceRoute(modifier: Modifier = Modifier) {
    val viewModel: SentenceViewModel = hiltViewModel()
    val isReady by viewModel.initializedState.collectAsState(initial = false)
    val results by viewModel.searchResultsState.collectAsState(initial = emptyList())
    val time by viewModel.searchTimeState.collectAsState(initial = 0L)

    if (isReady) {
        SentenceScreen(
            modifier = modifier,
            results = results,
            searchTimeMillis = time,
            onClick = { inputText, nValue ->
                viewModel.search(
                    query = inputText,
                    topN = nValue
                )
            }
        )
    } else {
        LoadingScreen()
    }
}

@Composable
fun SentenceScreen(
    modifier: Modifier = Modifier,
    results: List<SearchResult>,
    searchTimeMillis: Long,
    onClick: (String, Int) -> Unit = { _, _ -> }
) {
    var inputText by remember { mutableStateOf("") }
    var nValue by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(dimensionResource(R.dimen.content_padding))
    ) {
        Text(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.header_horizontal_padding),
                vertical = dimensionResource(R.dimen.header_vertical_padding)
            ),
            text = stringResource(R.string.chunk_header),
            fontSize = dimensionResource(R.dimen.header_size).value.sp,
            fontWeight = FontWeight.Bold
        )
        BaseTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = stringResource(R.string.tf_placeholder)
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.content_space)))
        SliderWithLabel(
            nValue = nValue,
            onValueChange = { nValue = it },
            range = 0..59
        )
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.content_space)))
        BaseButton(
            text = stringResource(R.string.btn_search),
            onClick = { onClick(inputText, nValue) },
            enabled = inputText.isNotBlank() && nValue > 0 && nValue <= 100
        )
        ChunkList(
            results = results,
            time = searchTimeMillis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SentenceScreenPreview() {
    SentenceSimilarityTheme {
        SentenceScreen(
            results = listOf(
                SearchResult(id = 1, similarity = 0.95f, content = "Example sentence 1"),
                SearchResult(id = 2, similarity = 0.90f, content = "Example sentence 2")
            ),
            searchTimeMillis = 1234L,
            onClick = { _, _ -> }
        )
    }
}
