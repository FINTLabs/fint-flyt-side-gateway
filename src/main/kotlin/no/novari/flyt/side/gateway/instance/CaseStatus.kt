package no.novari.flyt.side.gateway.instance

import com.fasterxml.jackson.annotation.JsonProperty

data class CaseStatus(
    @JsonProperty("archiveCaseId")
    val archiveCaseId: String,
)
