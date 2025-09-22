package com.teamviewer.assistvision.gateway.domain.model

import android.os.Parcelable
import com.teamviewer.assistvision.gateway.constants.EnvironmentType
import com.teamviewer.assistvision.gateway.constants.ServerType
import kotlinx.parcelize.Parcelize

abstract class ISimpleIdentConfig(
    open val caseId: String,
    open val environment: IdentEnvironment? = null,
)

@Parcelize
data class IdentEnvironment(
    val serverType: ServerType,
    val environmentType: EnvironmentType
) : Parcelable

@Parcelize
sealed interface AssistArResult : Parcelable {
    @Parcelize
    data object Competed : AssistArResult

    @Parcelize
    data object Cancelled : AssistArResult

    @Parcelize
    data class Failed(
        val reason: String,
        val isRecoverable: Boolean
    ) : AssistArResult
}
