package com.teamviewer.assistvision.gateway.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.teamviewer.assistvision.gateway.constants.ArgKeys.ASSIST_AR_ARGUMENT_KEY
import com.teamviewer.assistvision.gateway.constants.ArgKeys.ASSIST_AR_RESULT_KEY
import com.teamviewer.assistvision.gateway.domain.model.AssistArConfig
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult
import com.teamviewer.assistvision.ui.root.AssistArComposeApp
import com.teamviewer.assistvision.ui.theme.AssistArTheme
import de.check24.android.simpleident.utils.extensions.parcelable
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

class AssistArActivity : ComponentActivity(), BaseSimpleIdent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
            .launch(Manifest.permission.CAMERA)

        val config = intent.parcelable<AssistArConfig>(ASSIST_AR_ARGUMENT_KEY)
        if (config == null || config.caseId.isEmpty()) {
            returnResult(makeInvalidConfigResult())
            return
        }

        setContent {
            AssistArTheme {
                AssistArComposeApp(
                    onResult = {
                        returnResult(it)
                    })
            }
        }

        makeView(this, applicationContext, savedInstanceState, config)
    }

    override fun returnResult(result: AssistArResult) {
        val intent = Intent().apply {
            putExtra(
                ASSIST_AR_RESULT_KEY,
                result
            )
        }
        setResult(
            RESULT_OK,
            intent
        )
        finish()
    }
}