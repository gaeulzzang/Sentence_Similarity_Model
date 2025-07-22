package com.sentence.similarity.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.sentence.similarity.R
import com.sentence.similarity.type.SearchResult

@Composable
fun ChunkList(
    results: List<SearchResult>,
    time: Long = 0L
) {
    val listState = rememberLazyListState()

    LaunchedEffect(results) {
        listState.scrollToItem(0)
    }

    Column {
        Text(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.header_horizontal_padding),
                vertical = dimensionResource(R.dimen.header_vertical_padding)
            ),
            text = stringResource(R.string.chunk_list_header),
            fontSize = dimensionResource(R.dimen.header_size).value.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.header_horizontal_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chunk_space))
        ) {
            Text(text = stringResource(R.string.chunk_item_time, time.toString()))
            Text(text = stringResource(R.string.chunk_item_result, results.size.toString()))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            itemsIndexed(results, key = { _, result -> result.id }) { index, result ->
                ChunkItem(
                    index = index,
                    similarity = result.similarity,
                    content = result.content
                )
            }
        }
    }
}

@Composable
fun ChunkItem(
    index: Int,
    similarity: Float,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.chunk_item_vertical_padding))
    ) {
        Text(
            text = stringResource(
                R.string.chunk_item_similarity,
                index + 1,
                similarity
            ),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = content,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChunkItemPreview() {
    ChunkItem(
        index = 0,
        similarity = 0.1234f,
        content = "이것은 테스트 문장입니다. 이 문장은 검색 결과의 일부로 사용됩니다. "
    )
}
