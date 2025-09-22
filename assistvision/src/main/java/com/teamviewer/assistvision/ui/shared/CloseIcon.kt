package com.teamviewer.assistvision.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.teamviewer.assistvision.R

@Composable
fun CloseIcon(
    tintColor: Color = Color.Black,
    onPressed: () -> Unit = {}
) {
    Image(
        modifier = Modifier
            .clickable {
                onPressed()
            },
        painter = painterResource(id = R.drawable.ar_ic_exit),
        contentDescription = "Close",
        colorFilter = ColorFilter.tint(tintColor)
    )
}

@Composable
@Preview
fun CloseIconPreview() {
    FillWidthBackground {
        CloseIcon()
    }
}
