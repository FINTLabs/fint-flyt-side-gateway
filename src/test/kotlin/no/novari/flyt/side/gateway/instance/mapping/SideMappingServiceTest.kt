package no.novari.flyt.side.gateway.instance.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.gateway.webinstance.model.File
import no.novari.flyt.side.gateway.instance.ImportantInformation
import no.novari.flyt.side.gateway.instance.Marker
import no.novari.flyt.side.gateway.instance.Note
import no.novari.flyt.side.gateway.instance.NoteContent
import no.novari.flyt.side.gateway.instance.NoteUpdate
import no.novari.flyt.side.gateway.instance.SideDocument
import no.novari.flyt.side.gateway.instance.SideInstance
import no.novari.flyt.side.gateway.instance.UserSummary
import no.novari.flyt.side.gateway.instance.VisitLogEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.util.UUID

class SideMappingServiceTest {
    private val service = SideMappingService()
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @Test
    fun `maps side instance with collections and main document`() {
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
                deletedDate = null,
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

        val marker =
            Marker(
                id = 1019,
                value = "internat",
                date = "2025-08-06T00:38:13.000Z",
                deletedDate = null,
                createdBy = UserSummary(username = "havhil", name = "Havard Hilding"),
                deletedBy = null,
            )

        val visitLogEntry =
            VisitLogEntry(
                id = 13026,
                date = "2025-08-06T00:41:43.000Z",
                accesses = listOf("larer", "administrator"),
                user = UserSummary(username = "havhil", name = "Havard Hilding"),
            )

        val input =
            SideInstance(
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
                visitLog = listOf(visitLogEntry),
                document = document,
                documentType = "SIDE-ELEV-DOKUMENTASJON",
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
                "instans_id" to "19643037",
                "id" to "20127",
                "elevnummer" to "19643037",
                "fodselsnummer" to "09070647602",
                "feideId" to "elenor12",
                "navn" to "Elev Normann",
                "manueltOpprettet" to "false",
                "sistOppdatert" to "2025-09-26T11:02:24.063Z",
                "dokumenttype" to "SIDE-ELEV-DOKUMENTASJON",
                "dokument.tittel" to "SiDE elevdokumentasjon for Elev Normann",
                "dokument.filnavn" to "SiDE-Elev-Normann-dokumentasjon.pdf",
                "dokument.fil" to expectedFileId.toString(),
                "dokument.format" to "application/pdf",
            ),
            result.valuePerKey,
        )
        assertTrue("dokument" !in result.objectCollectionPerKey)

        val noteObjects = result.objectCollectionPerKey.getValue("notater")
        assertEquals(1, noteObjects.size)
        val noteObject = noteObjects.single()
        assertEquals(
            mapOf(
                "id" to "4005",
                "dato" to "2025-08-06T21:35:37.000Z",
                "frist" to "",
                "tittel" to "test",
                "type" to "elev-notat",
                "roller" to objectMapper.writeValueAsString(listOf("elevtjenesten", "kontaktlarer")),
                "oppdateringsfrekvens" to "",
                "slettetDato" to "",
                "redigertDato" to "2025-08-07T10:00:00.000Z",
                "oppdateringer" to objectMapper.writeValueAsString(note.updates),
                "ansvarlige" to objectMapper.writeValueAsString(note.responsible),
                "avsluttet" to "",
            ),
            noteObject.valuePerKey,
        )

        val contentObjects = noteObject.objectCollectionPerKey.getValue("innhold")
        assertEquals(1, contentObjects.size)
        assertEquals(
            mapOf(
                "verdi" to "beskrivelse",
                "innhold" to "test",
            ),
            contentObjects.single().valuePerKey,
        )

        val createdByObjects = noteObject.objectCollectionPerKey.getValue("opprettetAv")
        assertEquals(1, createdByObjects.size)
        assertEquals(
            mapOf(
                "brukernavn" to "havhil",
                "navn" to "Havard Hilding",
            ),
            createdByObjects.single().valuePerKey,
        )

        val editedByObjects = noteObject.objectCollectionPerKey.getValue("redigertAv")
        assertEquals(1, editedByObjects.size)
        assertEquals(
            mapOf(
                "brukernavn" to "editor",
                "navn" to "Editor",
            ),
            editedByObjects.single().valuePerKey,
        )

        val importantInformationObjects = result.objectCollectionPerKey.getValue("viktigInformasjon")
        assertEquals(1, importantInformationObjects.size)
        val infoObject = importantInformationObjects.single()
        assertEquals(
            mapOf(
                "informasjon" to "test",
                "sistOppdatert" to "2025-08-06T21:35:49.000Z",
                "slettetDato" to "",
            ),
            infoObject.valuePerKey,
        )
        val lastUpdatedByObjects = infoObject.objectCollectionPerKey.getValue("sistOppdatertAv")
        assertEquals(1, lastUpdatedByObjects.size)
        assertEquals(
            mapOf(
                "brukernavn" to "havhil",
                "navn" to "Havard Hilding",
            ),
            lastUpdatedByObjects.single().valuePerKey,
        )

        val markerObjects = result.objectCollectionPerKey.getValue("markeringer")
        assertEquals(1, markerObjects.size)
        val markerObject = markerObjects.single()
        assertEquals(
            mapOf(
                "id" to "1019",
                "verdi" to "internat",
                "dato" to "2025-08-06T00:38:13.000Z",
                "slettetDato" to "",
            ),
            markerObject.valuePerKey,
        )

        val markerCreatedByObjects = markerObject.objectCollectionPerKey.getValue("opprettetAv")
        assertEquals(1, markerCreatedByObjects.size)
        val markerCreatedBy = markerCreatedByObjects.single()
        assertEquals(
            mapOf(
                "brukernavn" to "havhil",
                "navn" to "Havard Hilding",
            ),
            markerCreatedBy.valuePerKey,
        )
        assertTrue(markerCreatedBy.objectCollectionPerKey.isEmpty())

        val deletedByObjects = markerObject.objectCollectionPerKey.getValue("slettetAv")
        assertTrue(deletedByObjects.isEmpty())

        val visitLogObjects = result.objectCollectionPerKey.getValue("besokLogg")
        assertEquals(1, visitLogObjects.size)
        val visitLogObject = visitLogObjects.single()
        assertEquals(
            mapOf(
                "id" to "13026",
                "dato" to "2025-08-06T00:41:43.000Z",
                "tilganger" to objectMapper.writeValueAsString(listOf("larer", "administrator")),
            ),
            visitLogObject.valuePerKey,
        )

        val visitUserObjects = visitLogObject.objectCollectionPerKey.getValue("bruker")
        assertEquals(1, visitUserObjects.size)
        assertEquals(
            mapOf(
                "brukernavn" to "havhil",
                "navn" to "Havard Hilding",
            ),
            visitUserObjects.single().valuePerKey,
        )
    }
}
