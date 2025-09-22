package com.teamviewer.assistar.demo.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teamviewer.assistar.demo.R
import com.teamviewer.assistar.demo.ui.shared.UIEventListener
import com.teamviewer.assistvision.ui.shared.FillBackground
import com.teamviewer.assistvision.ui.shared.FillSizeBackground
import com.teamviewer.assistvision.ui.theme.ARTheme
import org.koin.androidx.compose.koinViewModel


@Composable
fun DashboardScreen() {
    val viewModel = koinViewModel<DashboardScreenViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreenUI(
        uiState = uiState,
        uiEventListener = viewModel
    )
}

@Composable
fun DashboardScreenUI(
    uiState: DashboardScreenUiState,
    uiEventListener: UIEventListener
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        uiState.logo?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = uiState.logoDescription,
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 24.dp)
            )
        }


        Text(
            text = uiState.welcomeTitle,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ARTheme.colors.main,
            ),
            shape = RoundedCornerShape(12),
            onClick = {
                uiEventListener.onUIEvent(UIEventStartAssistAr)
            },
            content = {
                Text(
                    text = uiState.buttonTitle,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        )
    }
}

@Preview
@Composable
fun DashboardScreenPreview(){
    FillSizeBackground {
        DashboardScreenUI(
            uiState = DashboardScreenUiState(
                welcomeTitle = "Welcome to AssistAR", buttonTitle = "Start AssistAR", logo = R.drawable.assist_ar_logo, logoDescription = "Teamviewer Logo"
            ), uiEventListener = {})
    }
}