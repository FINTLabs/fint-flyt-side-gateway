package no.novari.flyt.side.gateway.instance

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SideStudentInstance(
    @JsonProperty("instans_id")
    val instanceId: String,
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("elevnummer")
    val studentNumber: String,
    @JsonProperty("fodselsnummer")
    val nationalId: String,
    @JsonProperty("feideId")
    val feideId: String,
    @JsonProperty("navn")
    val name: String,
    @JsonProperty("manueltOpprettet")
    val manuallyCreated: Boolean,
    @JsonProperty("sistOppdatert")
    val lastUpdated: String,
    @JsonProperty("notater")
    val notes: List<Note> = emptyList(),
    @JsonProperty("viktigInformasjon")
    val importantInformation: List<ImportantInformation> = emptyList(),
    @JsonProperty("markeringer")
    val markers: List<Marker> = emptyList(),
    @JsonProperty("dokument")
    val document: SideDocument,
    @JsonProperty("dokumenttype")
    val documentType: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SideDocument(
    @JsonProperty("filnavn")
    val fileName: String,
    @JsonProperty("fil")
    val fileBase64: String,
    @JsonProperty("tittel")
    val title: String,
    @JsonProperty("format")
    val format: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Note(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("dato")
    val date: String,
    @JsonProperty("frist")
    val dueDate: String? = null,
    @JsonProperty("tittel")
    val title: String,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("roller")
    val roles: List<String> = emptyList(),
    @JsonProperty("oppdateringsfrekvens")
    val updateFrequency: String? = null,
    @JsonProperty("innhold")
    val content: List<NoteContent> = emptyList(),
    @JsonProperty("redigertDato")
    val editedDate: String? = null,
    @JsonProperty("oppdateringer")
    val updates: List<NoteUpdate> = emptyList(),
    @JsonProperty("ansvarlige")
    val responsible: List<UserSummary> = emptyList(),
    @JsonProperty("redigertAv")
    val editedBy: UserSummary? = null,
    @JsonProperty("opprettetAv")
    val createdBy: UserSummary,
    @JsonProperty("avsluttet")
    val closed: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NoteContent(
    @JsonProperty("verdi")
    val label: String,
    @JsonProperty("innhold")
    val text: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NoteUpdate(
    @JsonProperty("dato")
    val date: String? = null,
    @JsonProperty("innhold")
    val content: String? = null,
    @JsonProperty("oppdatertAv")
    val updatedBy: UserSummary? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ImportantInformation(
    @JsonProperty("informasjon")
    val information: String,
    @JsonProperty("sistOppdatert")
    val lastUpdated: String,
    @JsonProperty("slettetDato")
    val deletedDate: String? = null,
    @JsonProperty("sistOppdatertAv")
    val lastUpdatedBy: UserSummary,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Marker(
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("verdi")
    val value: String,
    @JsonProperty("dato")
    val date: String,
    @JsonProperty("slettetDato")
    val deletedDate: String? = null,
    @JsonProperty("opprettetAv")
    val createdBy: MarkerUser,
    @JsonProperty("slettetAv")
    val deletedBy: MarkerUser? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserSummary(
    @JsonProperty("brukernavn")
    val username: String? = null,
    @JsonProperty("navn")
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarkerUser(
    @JsonProperty("brukernavn")
    val username: String? = null,
    @JsonProperty("navn")
    val name: String? = null,
    @JsonProperty("superAdmin")
    val superAdmin: Boolean? = null,
    @JsonProperty("larlingAdmin")
    val apprenticeAdmin: Boolean? = null,
    @JsonProperty("aktiv")
    val active: Boolean? = null,
    @JsonProperty("sistInnlogget")
    val lastLogin: String? = null,
    @JsonProperty("mailDager")
    val mailDays: List<String> = emptyList(),
    @JsonProperty("ikkeVisEkstraInformasjon")
    val hiddenExtraInformation: List<String> = emptyList(),
    @JsonProperty("lestRutiner")
    val readGuidelines: String? = null,
)
