package no.novari.flyt.side.gateway.instance

import no.novari.flyt.gateway.webinstance.kafka.ArchiveCaseIdRequestService
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.security.core.Authentication

class CaseStatusServiceTest {
    private val sourceApplicationAuthorizationService =
        Mockito.mock(SourceApplicationAuthorizationService::class.java)
    private val archiveCaseIdRequestService = Mockito.mock(ArchiveCaseIdRequestService::class.java)
    private val service = CaseStatusService(sourceApplicationAuthorizationService, archiveCaseIdRequestService)

    @Test
    fun `returns case status when archive case id exists`() {
        val authentication = Mockito.mock(Authentication::class.java)
        Mockito.`when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication))
            .thenReturn(123L)
        Mockito.`when`(archiveCaseIdRequestService.getArchiveCaseId(123L, "instance-1"))
            .thenReturn("archive-1")

        val result = service.getCaseStatus(authentication, "instance-1")

        assertEquals(CaseStatus("archive-1"), result)
        Mockito.verify(archiveCaseIdRequestService).getArchiveCaseId(123L, "instance-1")
    }

    @Test
    fun `returns null when archive case id is missing`() {
        val authentication = Mockito.mock(Authentication::class.java)
        Mockito.`when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication))
            .thenReturn(123L)
        Mockito.`when`(archiveCaseIdRequestService.getArchiveCaseId(123L, "instance-1"))
            .thenReturn(null)

        val result = service.getCaseStatus(authentication, "instance-1")

        assertNull(result)
    }
}
