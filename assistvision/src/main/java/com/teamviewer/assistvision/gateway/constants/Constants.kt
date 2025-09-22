package com.teamviewer.assistvision.gateway.constants

enum class ServerType {
    NO_SERVER,
    MAIN_SERVER,
    MOCK_SERVER,
    HOLO_MOCK_SERVER
}

enum class EnvironmentType(
    val key: String
) {
    INT("int"),
    STAGING("test"),
    PROD("prod")
}

object ArgKeys {
    const val ASSIST_AR_REQUEST_KEY = "AssistArRequestKey"
    const val ASSIST_AR_RESULT_KEY = "AssistArResultKey"
    const val ASSIST_AR_ARGUMENT_KEY = "AssistArArgumentKey"
    const val ASSIST_AR_KOIN_CONFIG_KEY = "AssistArKoinConfigKey"
}
