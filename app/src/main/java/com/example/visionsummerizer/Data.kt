package com.example.visionsummerizer

import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class Data(
    val context: @RawValue ComponentActivity,
    val padding: @RawValue PaddingValues
):Parcelable
