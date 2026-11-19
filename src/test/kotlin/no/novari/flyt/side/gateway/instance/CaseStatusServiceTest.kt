package no.novari.flyt.side.gateway.instance

import no.novari.flyt.gateway.instance.HistoryClient
import no.novari.flyt.webresourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.core.Authentication

class CaseStatusServiceTest {
    private val sourceApplicationAuthorizationService =
        mock(SourceApplicationAuthorizationService::class.java)
    private val historyClient = mock(HistoryClient::class.java)
    private val service = CaseStatusService(sourceApplicationAuthorizationService, historyClient)

    @Test
    fun `returns case status when archive case id exists`() {
        val authentication = mock(Authentication::class.java)
        `when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication))
            .thenReturn(123L)
        `when`(historyClient.getArchiveInstanceId(123L, "instance-1"))
            .thenReturn("archive-1")

        val result = service.getCaseStatus(authentication, "instance-1")

        assertEquals(CaseStatus("archive-1"), result)
        verify(historyClient).getArchiveInstanceId(123L, "instance-1")
    }

    @Test
    fun `returns null when archive case id is missing`() {
        val authentication = mock(Authentication::class.java)
        `when`(sourceApplicationAuthorizationService.getSourceApplicationId(authentication))
            .thenReturn(123L)
        `when`(historyClient.getArchiveInstanceId(123L, "instance-1"))
            .thenReturn(null)

        val result = service.getCaseStatus(authentication, "instance-1")

        assertNull(result)
    }
}
