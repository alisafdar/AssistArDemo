package com.teamviewer.assistvision.gateway.contract

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.teamviewer.assistvision.constants.FailureReasons
import com.teamviewer.assistvision.gateway.constants.ArgKeys.ASSIST_AR_ARGUMENT_KEY
import com.teamviewer.assistvision.gateway.constants.ArgKeys.ASSIST_AR_RESULT_KEY
import com.teamviewer.assistvision.gateway.domain.model.AssistArConfig
import com.teamviewer.assistvision.gateway.domain.model.AssistArResult
import com.teamviewer.assistvision.gateway.ui.AssistArActivity
import de.check24.android.simpleident.utils.extensions.parcelable
import kotlin.jvm.java

class AssistArActivityContract : ActivityResultContract<AssistArConfig, AssistArResult>() {
    override fun createIntent(
        context: Context,
        input: AssistArConfig
    ): Intent =
        Intent(
            context,
            AssistArActivity::class.java
        ).apply {
            putExtra(
                ASSIST_AR_ARGUMENT_KEY, input
            )
        }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): AssistArResult =
        intent?.parcelable<AssistArResult>(ASSIST_AR_RESULT_KEY) ?: AssistArResult.Failed(
            FailureReasons.DECODING_FAILURE,
            false
        )
}
