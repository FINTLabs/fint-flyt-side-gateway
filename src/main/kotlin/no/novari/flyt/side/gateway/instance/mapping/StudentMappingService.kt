package no.novari.flyt.side.gateway.instance.mapping

import no.novari.flyt.gateway.webinstance.InstanceMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.gateway.webinstance.model.instance.InstanceObject
import no.novari.flyt.side.gateway.instance.ImportantInformation
import no.novari.flyt.side.gateway.instance.Marker
import no.novari.flyt.side.gateway.instance.MarkerUser
import no.novari.flyt.side.gateway.instance.Note
import no.novari.flyt.side.gateway.instance.NoteContent
import no.novari.flyt.side.gateway.instance.NoteUpdate
import no.novari.flyt.side.gateway.instance.SideDocument
import no.novari.flyt.side.gateway.instance.SideStudentInstance
import no.novari.flyt.side.gateway.instance.UserSummary
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StudentMappingService : InstanceMapper<SideStudentInstance> {
    override fun map(
        sourceApplicationId: Long,
        incomingInstance: SideStudentInstance,
        persistFile: (File) -> UUID,
    ): InstanceObject {
        val documentInstanceObjects =
            mapDocumentToInstanceObjects(
                persistFile = persistFile,
                sourceApplicationId = sourceApplicationId,
                sourceApplicationInstanceId = incomingInstance.instanceId,
                document = incomingInstance.document,
            )

        val noteInstanceObjects = mapNotesToInstanceObjects(incomingInstance.notes)
        val importantInformationInstanceObjects =
            mapImportantInformationToInstanceObjects(incomingInstance.importantInformation)
        val markerInstanceObjects = mapMarkersToInstanceObjects(incomingInstance.markers)

        val valuePerKey =
            buildMap {
                putOrEmpty("instanceId", incomingInstance.instanceId)
                putOrEmpty("id", incomingInstance.id)
                putOrEmpty("studentNumber", incomingInstance.studentNumber)
                putOrEmpty("nationalId", incomingInstance.nationalId)
                putOrEmpty("feideId", incomingInstance.feideId)
                putOrEmpty("name", incomingInstance.name)
                putOrEmpty("manuallyCreated", incomingInstance.manuallyCreated)
                putOrEmpty("lastUpdated", incomingInstance.lastUpdated)
                putOrEmpty("documentType", incomingInstance.documentType)
            }

        val objectCollectionPerKey =
            mutableMapOf<String, Collection<InstanceObject>>(
                "documents" to documentInstanceObjects,
                "notes" to noteInstanceObjects,
                "importantInformation" to importantInformationInstanceObjects,
                "markers" to markerInstanceObjects,
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
                putOrEmpty("date", note.date)
                putOrEmpty("dueDate", note.dueDate)
                putOrEmpty("title", note.title)
                putOrEmpty("type", note.type)
                putOrEmpty("updateFrequency", note.updateFrequency)
                putOrEmpty("editedDate", note.editedDate)
                putOrEmpty("closed", note.closed)
            }

        val objectCollectionPerKey =
            mutableMapOf<String, Collection<InstanceObject>>(
                "roles" to mapStringListToInstanceObjects(note.roles, "role"),
                "content" to mapNoteContentToInstanceObjects(note.content),
                "updates" to mapNoteUpdatesToInstanceObjects(note.updates),
                "responsible" to mapUserSummaryListToInstanceObjects(note.responsible),
                "createdBy" to mapUserSummaryToInstanceObjects(note.createdBy),
                "editedBy" to mapUserSummaryToInstanceObjects(note.editedBy),
            )

        return InstanceObject(valuePerKey, objectCollectionPerKey)
    }

    private fun mapNoteContentToInstanceObjects(content: List<NoteContent>): List<InstanceObject> {
        return content.map {
            InstanceObject(
                valuePerKey =
                    buildMap {
                        putOrEmpty("label", it.label)
                        putOrEmpty("text", it.text)
                    },
            )
        }
    }

    private fun mapNoteUpdatesToInstanceObjects(updates: List<NoteUpdate>): List<InstanceObject> {
        return updates.map { update ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("date", update.date)
                    putOrEmpty("content", update.content)
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "updatedBy" to mapUserSummaryToInstanceObjects(update.updatedBy),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapImportantInformationToInstanceObjects(
        importantInformation: List<ImportantInformation>,
    ): List<InstanceObject> {
        return importantInformation.map { info ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("information", info.information)
                    putOrEmpty("lastUpdated", info.lastUpdated)
                    putOrEmpty("deletedDate", info.deletedDate)
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "lastUpdatedBy" to mapUserSummaryToInstanceObjects(info.lastUpdatedBy),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapMarkersToInstanceObjects(markers: List<Marker>): List<InstanceObject> {
        return markers.map { marker ->
            val valuePerKey =
                buildMap {
                    putOrEmpty("id", marker.id)
                    putOrEmpty("value", marker.value)
                    putOrEmpty("date", marker.date)
                    putOrEmpty("deletedDate", marker.deletedDate)
                }
            val objectCollectionPerKey =
                mutableMapOf<String, Collection<InstanceObject>>(
                    "createdBy" to mapMarkerUserToInstanceObjects(marker.createdBy),
                    "deletedBy" to mapMarkerUserToInstanceObjects(marker.deletedBy),
                )
            InstanceObject(valuePerKey, objectCollectionPerKey)
        }
    }

    private fun mapUserSummaryListToInstanceObjects(users: List<UserSummary>): List<InstanceObject> {
        return users.map(::mapUserSummaryToInstanceObject)
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
                    putOrEmpty("username", user.username)
                    putOrEmpty("name", user.name)
                },
        )
    }

    private fun mapMarkerUserToInstanceObjects(user: MarkerUser?): List<InstanceObject> {
        return if (user == null) {
            emptyList()
        } else {
            listOf(mapMarkerUserToInstanceObject(user))
        }
    }

    private fun mapMarkerUserToInstanceObject(user: MarkerUser): InstanceObject {
        val valuePerKey =
            buildMap {
                putOrEmpty("username", user.username)
                putOrEmpty("name", user.name)
                putOrEmpty("superAdmin", user.superAdmin)
                putOrEmpty("apprenticeAdmin", user.apprenticeAdmin)
                putOrEmpty("active", user.active)
                putOrEmpty("lastLogin", user.lastLogin)
                putOrEmpty("readGuidelines", user.readGuidelines)
            }

        val objectCollectionPerKey =
            mutableMapOf<String, Collection<InstanceObject>>(
                "mailDays" to mapStringListToInstanceObjects(user.mailDays, "mailDay"),
                "hiddenExtraInformation" to mapStringListToInstanceObjects(user.hiddenExtraInformation, "value"),
            )

        return InstanceObject(valuePerKey, objectCollectionPerKey)
    }

    private fun mapStringListToInstanceObjects(values: List<String>, valueKey: String): List<InstanceObject> {
        return values.map { value ->
            InstanceObject(
                valuePerKey =
                    buildMap {
                        putOrEmpty(valueKey, value)
                    },
            )
        }
    }

    private fun mapDocumentToInstanceObjects(
        persistFile: (File) -> UUID,
        sourceApplicationId: Long,
        sourceApplicationInstanceId: String,
        document: SideDocument,
    ): List<InstanceObject> {
        val mediaType = resolveMediaType(document)
        val file = toFile(sourceApplicationId, sourceApplicationInstanceId, document, mediaType)
        val fileId = persistFile(file)
        return listOf(mapDocumentAndFileIdToInstanceObject(document, mediaType, fileId))
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

    private fun mapDocumentAndFileIdToInstanceObject(
        document: SideDocument,
        mediaType: MediaType,
        fileId: UUID,
    ): InstanceObject {
        return InstanceObject(
            valuePerKey =
                buildMap {
                    putOrEmpty("title", document.title)
                    putOrEmpty("fileName", document.fileName)
                    putOrEmpty("mediaType", mediaType.toString())
                    putOrEmpty("file", fileId)
                    putOrEmpty("mainDocument", true)
                },
        )
    }
}
