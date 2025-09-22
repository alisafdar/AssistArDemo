package com.teamviewer.assistar.demo.ui.demo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.teamviewer.assistvision.gateway.constants.EnvironmentType
import com.teamviewer.assistvision.gateway.constants.ServerType
import com.teamviewer.assistvision.gateway.contract.AssistArActivityContract
import com.teamviewer.assistvision.gateway.domain.model.AssistArConfig
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult
import com.teamviewer.assistvision.gateway.domain.model.IdentEnvironment

class DemoActivity : ComponentActivity() {
    private val mainActivityLauncher = registerForActivityResult(AssistArActivityContract()) { result ->
        when (result) {
            AssistArResult.Cancelled -> Toast.makeText(
                this, "Cancelled by user", Toast.LENGTH_SHORT
            ).show()

            is AssistArResult.Failed -> Toast.makeText(
                this, "Failed due to ${result.reason}", Toast.LENGTH_SHORT
            ).show()

            AssistArResult.Competed -> Toast.makeText(
                this, "Successful", Toast.LENGTH_SHORT
            ).show()
        }
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            return
        }

        mainActivityLauncher.launch(
            AssistArConfig(
                caseId = "123456789", environment = IdentEnvironment(
                    serverType = ServerType.MAIN_SERVER, environmentType = EnvironmentType.INT
                )
            )
        )
    }
}
