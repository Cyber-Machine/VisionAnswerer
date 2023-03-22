package com.example.visionsummerizer

import android.os.Parcelable
import androidx.compose.foundation.layout.PaddingValues
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class OCR(
    val paddingValues: @RawValue PaddingValues,
    val text : String
):Parcelable