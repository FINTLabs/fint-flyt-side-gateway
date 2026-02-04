package no.novari.flyt.side.gateway.instance.mapping

import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.side.gateway.instance.ImportantInformation
import no.novari.flyt.side.gateway.instance.Marker
import no.novari.flyt.side.gateway.instance.MarkerUser
import no.novari.flyt.side.gateway.instance.Note
import no.novari.flyt.side.gateway.instance.NoteContent
import no.novari.flyt.side.gateway.instance.NoteUpdate
import no.novari.flyt.side.gateway.instance.SideDocument
import no.novari.flyt.side.gateway.instance.SideStudentInstance
import no.novari.flyt.side.gateway.instance.UserSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class StudentMappingServiceTest {
    private val service = StudentMappingService()

    @Test
    fun `maps side student instance with collections and main document`() {
        val document =
            SideDocument(
                fileName = "SiDE-Elev-Normann-dokumentasjon.pdf",
                fileBase64 = "Zm9v",
                title = "SiDE elevdokumentasjon for Elev Normann",
                format = "application/pdf",
            )

        val note =
            Note(
                id = 4005,
                date = "2025-08-06T21:35:37.000Z",
                dueDate = null,
                title = "test",
                type = "elev-notat",
                roles = listOf("elevtjenesten", "kontaktlarer"),
                updateFrequency = null,
                content = listOf(NoteContent(label = "beskrivelse", text = "test")),
                editedDate = "2025-08-07T10:00:00.000Z",
                updates =
                    listOf(
                        NoteUpdate(
                            date = "2025-08-07T10:00:00.000Z",
                            content = "oppdatert",
                            updatedBy = UserSummary(username = "editor", name = "Editor"),
                        ),
                    ),
                responsible = listOf(UserSummary(username = "ansvarlig", name = "Ansvarlig")),
                editedBy = UserSummary(username = "editor", name = "Editor"),
                createdBy = UserSummary(username = "havhil", name = "Havard Hilding"),
                closed = null,
            )

        val importantInformation =
            ImportantInformation(
                information = "test",
                lastUpdated = "2025-08-06T21:35:49.000Z",
                deletedDate = null,
                lastUpdatedBy = UserSummary(username = "havhil", name = "Havard Hilding"),
            )

        val markerUser =
            MarkerUser(
                username = "havhil",
                name = "Havard Hilding",
                superAdmin = true,
                apprenticeAdmin = false,
                active = true,
                lastLogin = "2026-02-02T14:11:07.000Z",
                mailDays = listOf("Mandag", "Tirsdag"),
                hiddenExtraInformation = listOf("elevtabell", "notat-roller"),
                readGuidelines = "2025-02-18T20:08:52.000Z",
            )

        val marker =
            Marker(
                id = 1019,
                value = "internat",
                date = "2025-08-06T00:38:13.000Z",
                deletedDate = null,
                createdBy = markerUser,
                deletedBy = null,
            )

        val input =
            SideStudentInstance(
                instanceId = "19643037",
                id = 20127,
                studentNumber = "19643037",
                nationalId = "09070647602",
                feideId = "elenor12",
                name = "Elev Normann",
                manuallyCreated = false,
                lastUpdated = "2025-09-26T11:02:24.063Z",
                notes = listOf(note),
                importantInformation = listOf(importantInformation),
                markers = listOf(marker),
                document = document,
                documentType = "SIDE-ELEV-DOKUMENTASJON",
                callbackUrl = "https://callback.example.com",
            )

        val capturedFiles = mutableListOf<File>()
        val expectedFileId = UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")

        val result =
            service.map(
                sourceApplicationId = 99L,
                incomingInstance = input,
                persistFile = { file ->
                    capturedFiles += file
                    expectedFileId
                },
            )

        assertEquals(1, capturedFiles.size)
        val capturedFile = capturedFiles.single()
        assertEquals("SiDE-Elev-Normann-dokumentasjon.pdf", capturedFile.name)
        assertEquals(99L, capturedFile.sourceApplicationId)
        assertEquals("19643037", capturedFile.sourceApplicationInstanceId)
        assertEquals(MediaType.APPLICATION_PDF, capturedFile.type)
        assertEquals("UTF-8", capturedFile.encoding)
        assertEquals("Zm9v", capturedFile.base64Contents)

        assertEquals(
            mapOf(
                "instanceId" to "19643037",
                "id" to "20127",
                "studentNumber" to "19643037",
                "nationalId" to "09070647602",
                "feideId" to "elenor12",
                "name" to "Elev Normann",
                "manuallyCreated" to "false",
                "lastUpdated" to "2025-09-26T11:02:24.063Z",
                "documentType" to "SIDE-ELEV-DOKUMENTASJON",
            ),
            result.valuePerKey,
        )

        val documentObjects = result.objectCollectionPerKey.getValue("documents")
        assertEquals(1, documentObjects.size)
        val documentObject = documentObjects.single()
        assertEquals(
            mapOf(
                "title" to "SiDE elevdokumentasjon for Elev Normann",
                "fileName" to "SiDE-Elev-Normann-dokumentasjon.pdf",
                "mediaType" to "application/pdf",
                "file" to expectedFileId.toString(),
                "mainDocument" to "true",
            ),
            documentObject.valuePerKey,
        )
        assertTrue(documentObject.objectCollectionPerKey.isEmpty())

        val noteObjects = result.objectCollectionPerKey.getValue("notes")
        assertEquals(1, noteObjects.size)
        val noteObject = noteObjects.single()
        assertEquals(
            mapOf(
                "id" to "4005",
                "date" to "2025-08-06T21:35:37.000Z",
                "dueDate" to "",
                "title" to "test",
                "type" to "elev-notat",
                "updateFrequency" to "",
                "editedDate" to "2025-08-07T10:00:00.000Z",
                "closed" to "",
            ),
            noteObject.valuePerKey,
        )

        val roleObjects = noteObject.objectCollectionPerKey.getValue("roles")
        assertEquals(2, roleObjects.size)
        assertEquals(mapOf("role" to "elevtjenesten"), roleObjects.first().valuePerKey)

        val contentObjects = noteObject.objectCollectionPerKey.getValue("content")
        assertEquals(1, contentObjects.size)
        assertEquals(
            mapOf(
                "label" to "beskrivelse",
                "text" to "test",
            ),
            contentObjects.single().valuePerKey,
        )

        val updateObjects = noteObject.objectCollectionPerKey.getValue("updates")
        assertEquals(1, updateObjects.size)
        val updateObject = updateObjects.single()
        assertEquals(
            mapOf(
                "date" to "2025-08-07T10:00:00.000Z",
                "content" to "oppdatert",
            ),
            updateObject.valuePerKey,
        )
        val updatedByObjects = updateObject.objectCollectionPerKey.getValue("updatedBy")
        assertEquals(1, updatedByObjects.size)
        assertEquals(
            mapOf(
                "username" to "editor",
                "name" to "Editor",
            ),
            updatedByObjects.single().valuePerKey,
        )

        val responsibleObjects = noteObject.objectCollectionPerKey.getValue("responsible")
        assertEquals(1, responsibleObjects.size)
        assertEquals(
            mapOf(
                "username" to "ansvarlig",
                "name" to "Ansvarlig",
            ),
            responsibleObjects.single().valuePerKey,
        )

        val createdByObjects = noteObject.objectCollectionPerKey.getValue("createdBy")
        assertEquals(1, createdByObjects.size)
        assertEquals(
            mapOf(
                "username" to "havhil",
                "name" to "Havard Hilding",
            ),
            createdByObjects.single().valuePerKey,
        )

        val editedByObjects = noteObject.objectCollectionPerKey.getValue("editedBy")
        assertEquals(1, editedByObjects.size)
        assertEquals(
            mapOf(
                "username" to "editor",
                "name" to "Editor",
            ),
            editedByObjects.single().valuePerKey,
        )

        val importantInformationObjects = result.objectCollectionPerKey.getValue("importantInformation")
        assertEquals(1, importantInformationObjects.size)
        val infoObject = importantInformationObjects.single()
        assertEquals(
            mapOf(
                "information" to "test",
                "lastUpdated" to "2025-08-06T21:35:49.000Z",
                "deletedDate" to "",
            ),
            infoObject.valuePerKey,
        )
        val lastUpdatedByObjects = infoObject.objectCollectionPerKey.getValue("lastUpdatedBy")
        assertEquals(1, lastUpdatedByObjects.size)
        assertEquals(
            mapOf(
                "username" to "havhil",
                "name" to "Havard Hilding",
            ),
            lastUpdatedByObjects.single().valuePerKey,
        )

        val markerObjects = result.objectCollectionPerKey.getValue("markers")
        assertEquals(1, markerObjects.size)
        val markerObject = markerObjects.single()
        assertEquals(
            mapOf(
                "id" to "1019",
                "value" to "internat",
                "date" to "2025-08-06T00:38:13.000Z",
                "deletedDate" to "",
            ),
            markerObject.valuePerKey,
        )

        val markerCreatedByObjects = markerObject.objectCollectionPerKey.getValue("createdBy")
        assertEquals(1, markerCreatedByObjects.size)
        val markerCreatedBy = markerCreatedByObjects.single()
        assertEquals(
            mapOf(
                "username" to "havhil",
                "name" to "Havard Hilding",
                "superAdmin" to "true",
                "apprenticeAdmin" to "false",
                "active" to "true",
                "lastLogin" to "2026-02-02T14:11:07.000Z",
                "readGuidelines" to "2025-02-18T20:08:52.000Z",
            ),
            markerCreatedBy.valuePerKey,
        )

        val mailDayObjects = markerCreatedBy.objectCollectionPerKey.getValue("mailDays")
        assertEquals(2, mailDayObjects.size)
        assertEquals(mapOf("mailDay" to "Mandag"), mailDayObjects.first().valuePerKey)

        val hiddenObjects = markerCreatedBy.objectCollectionPerKey.getValue("hiddenExtraInformation")
        assertEquals(2, hiddenObjects.size)
        assertEquals(mapOf("value" to "elevtabell"), hiddenObjects.first().valuePerKey)

        val deletedByObjects = markerObject.objectCollectionPerKey.getValue("deletedBy")
        assertTrue(deletedByObjects.isEmpty())
    }
}
