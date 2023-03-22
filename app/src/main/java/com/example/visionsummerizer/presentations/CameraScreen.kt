package com.example.visionsummerizer.presentations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@ExperimentalMaterial3Api
@Composable
fun CameraScreen() {
    Scaffold(
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription ="BACK"
                        )
                    }
                },
                title = {
                    Text(text = "VisionSummerizer")
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceTint
                )
            )
        },
        content = {

        }
    )
}