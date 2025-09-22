package com.teamviewer.assistvision.gateway.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AssistArConfig(
    override val caseId: String,
    override val environment: IdentEnvironment? = null,
) : ISimpleIdentConfig(
    caseId,
    environment
), Parcelable
