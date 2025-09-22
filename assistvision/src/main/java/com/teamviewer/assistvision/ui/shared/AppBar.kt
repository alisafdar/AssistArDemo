package com.teamviewer.assistvision.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teamviewer.assistvision.R
import com.teamviewer.assistvision.ui.theme.ARTheme

@Composable
fun AppBar(
    visible: Boolean = true,
    onClose: () -> Unit = {}
) {
    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ARTheme.colors.mainDark)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = Dimens.Paddings.half
                    ),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Row(
                        modifier = Modifier
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        content = {
                            Image(
                                modifier = Modifier
                                    .padding(
                                        horizontal = Dimens.Paddings.default
                                    )
                                    .size(40.dp),
                                painter = painterResource(id = R.drawable.assist_ar_logo),
                                contentDescription = null
                            )

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .clip(RoundedCornerShape(100))
                                    .background(Color.White)
                                    .padding(
                                        vertical = Dimens.Paddings.half + Dimens.Paddings.small
                                    )
                            )

                            Text(
                                modifier = Modifier.padding(
                                    horizontal = Dimens.Paddings.default
                                ),
                                text = stringResource(R.string.assist_screen_header),
                                color = Color.White,
                                style = ARTheme.typography.subText
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.padding(
                            horizontal = Dimens.Paddings.default
                        ),
                        content = {
                            CloseIcon(
                                tintColor = Color.White,
                                onPressed = {
                                    onClose()
                                }
                            )
                        }
                    )
                }
            )
        }
    }
}

@Composable
@Preview
fun AppBarPreview() {
    FillBackground(
        modifier =
        Modifier
            .fillMaxSize()
            .background(Color.Gray),
        content = {
            AppBar(
                visible = true,
                onClose = {}
            )
        }
    )
}
