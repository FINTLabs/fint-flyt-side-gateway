package no.novari.flyt.side.gateway.instance.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import no.novari.flyt.side.gateway.instance.ImportantInformation
import no.novari.flyt.side.gateway.instance.Marker
import no.novari.flyt.side.gateway.instance.Note
import no.novari.flyt.side.gateway.instance.NoteContent
import no.novari.flyt.side.gateway.instance.SideDocument
import no.novari.flyt.side.gateway.instance.SideInstance
import no.novari.flyt.side.gateway.instance.UserSummary
import no.novari.flyt.side.gateway.instance.VisitLogEntry
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class SideMappingService : InstanceMapper<SideInstance> {
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    override fun map(
        sourceApplicationId: Long,
        incomingInstance: SideInstance,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        val documentValuePerKey =
            mapDocumentToValuePerKey(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = incomingInstance.instanceId,
                document = incomingInstance.document,
            )

        val noteInstanceObjects = mapNotesToInstanceObjects(incomingInstance.notes)
        val importantInformationInstanceObjects =
            mapImportantInformationToInstanceObjects(incomingInstance.importantInformation)
        val markerInstanceObjects = mapMarkersToInstanceObjects(incomingInstance.markers)
        val visitLogInstanceObjects = mapVisitLogToInstanceObjects(incomingInstance.visitLog)

        val valuePerKey =
            buildMap {
                putOrEmpty("instans_id", incomingInstance.instanceId)
                putOrEmpty("id", incomingInstance.id)
                putOrEmpty("elevnummer", incomingInstance.studentNumber)
                putOrEmpty("fodselsnummer", incomingInstance.nationalId)
                putOrEmpty("feideId", incomingInstance.feideId)
                putOrEmpty("navn", incomingInstance.name)
                putOrEmpty("manueltOpprettet", incomingInstance.manuallyCreated)
                putOrEmpty("sistOppdatert", incomingInstance.lastUpdated)
                putOrEmpty("dokumenttype", incomingInstance.documentType)
                putAll(documentValuePerKey)
            }

        val objectCollectionPerKey =
            mutableMapOf<String, Collection<InstanceObject>>(
                "notater" to noteInstanceObjects,
                "viktigInformasjon" to importantInformationInstanceObjects,
                "markeringer" to markerInstanceObjects,
                "besokLogg" to visitLogInstanceObjects,
            )

        return InstanceObject(valuePerKey, objectCollectionPerKey)
    }

    private fun mapNotesToInstanceObjects(notes: List<Note>): List<InstanceObject> {
        return notes.map(::mapNoteToInstanceObject)
    }

    private fun mapNoteToInstanceObject(note: Note): InstanceObject {
        val valuePerKey =
            buildMap {
                putOrEmpty("id", note.id)
                putOrEmpty("dato", note.date)
                putOrEmpty("frist", note.dueDate)
                putOrEmpty("tittel", note.title)
                putOrEmpty("type", note.type)
                putOrEmpty("roller", serialize(note.roles))
                putOrEmpty("oppdateringsfrekvens", note.updateFrequency)
                putOrEmpty("slettetDato", note.deletedDate)
                putOrEmpty("redigertDato", note.editedDate)
                putOrEmpty("oppdateringer", serialize(note.updates))
                putOrEmpty("ansvarlige", serialize(note.responsible))
                putOrEmpty("avsluttet", note.closed)
            }

        val objectCollectionPerKey =
            mutableMapOf<String, Collection<InstanceObject>>(
                "innhold" to mapNoteContentToInstanceObjects(note.content),
                "opprettetAv" to mapUserSummaryToInstanceObjects(note.createdBy),
                "redigertAv" to mapUserSummaryToInstanceObjects(note.editedBy),
            )

        return InstanceObject(valuePerKey, objectCollectionPerKey)
    }

    private fun mapNoteContentToInstanceObjects(content: List<NoteContent>): List<InstanceObject> {
        return content.map {
            InstanceObject(
                valuePerKey =
                    buildMap {
                        putOrEmpty("verdi", it.label)
                        putOrEmpty("innhold", it.text)
                    },
            )
        }
    }

    private fun mapImportantInformationToInstanceObjects(
        importantInformation: List<ImportantInformation>,
    ): List<InstanceObject> {
        return importantInformation.map { info ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("informasjon", info.information)
                    putOrEmpty("sistOppdatert", info.lastUpdated)
                    putOrEmpty("slettetDato", info.deletedDate)
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "sistOppdatertAv" to mapUserSummaryToInstanceObjects(info.lastUpdatedBy),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapMarkersToInstanceObjects(markers: List<Marker>): List<InstanceObject> {
        return markers.map { marker ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("id", marker.id)
                    putOrEmpty("verdi", marker.value)
                    putOrEmpty("dato", marker.date)
                    putOrEmpty("slettetDato", marker.deletedDate)
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "opprettetAv" to mapUserSummaryToInstanceObjects(marker.createdBy),
                    "slettetAv" to mapUserSummaryToInstanceObjects(marker.deletedBy),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapVisitLogToInstanceObjects(visitLog: List<VisitLogEntry>): List<InstanceObject> {
        return visitLog.map { entry ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("id", entry.id)
                    putOrEmpty("dato", entry.date)
                    putOrEmpty("tilganger", serialize(entry.accesses))
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "bruker" to mapUserSummaryToInstanceObjects(entry.user),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapUserSummaryToInstanceObjects(user: UserSummary?): List<InstanceObject> {
        return if (user == null) {
            emptyList()
        } else {
            listOf(mapUserSummaryToInstanceObject(user))
        }
    }

    private fun mapUserSummaryToInstanceObject(user: UserSummary): InstanceObject {
        return InstanceObject(
            valuePerKey =
                buildMap {
                    putOrEmpty("brukernavn", user.username)
                    putOrEmpty("navn", user.name)
                },
        )
    }

    private fun mapDocumentToValuePerKey(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        document: SideDocument,
    ): Map<String, String> {
        val mediaType = resolveMediaType(document)
        val file = toFile(sourceApplicationId, sourceApplicationInstanceId, document, mediaType)
        val fileId = persistFile(file)
        return mapDocumentAndFileIdToValuePerKey(document, mediaType, fileId)
    }

    private fun resolveMediaType(document: SideDocument): MediaType {
        val providedFormat = document.format.trim()
        if (providedFormat.isNotEmpty()) {
            return MediaType.parseMediaType(providedFormat)
        }
        val mediaType = MediaTypeFactory.getMediaType(document.fileName)
        return mediaType.orElseThrow {
            IllegalArgumentException("No media type found for fileName=${document.fileName}")
        }
    }

    private fun toFile(
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        document: SideDocument,
        mediaType: MediaType,
    ): File {
        return File(
            name = document.fileName,
            type = mediaType,
            sourceApplicationId = sourceApplicationId,
            sourceApplicationInstanceId = sourceApplicationInstanceId,
            encoding = "UTF-8",
            base64Contents = document.fileBase64,
        )
    }

    private fun mapDocumentAndFileIdToValuePerKey(
        document: SideDocument,
        mediaType: MediaType,
        fileId: UUID,
    ): Map<String, String> {
        return buildMap {
            putOrEmpty("dokument.tittel", document.title)
            putOrEmpty("dokument.filnavn", document.fileName)
            putOrEmpty("dokument.format", mediaType.toString())
            putOrEmpty("dokument.fil", fileId)
        }
    }

    private fun serialize(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }
}
