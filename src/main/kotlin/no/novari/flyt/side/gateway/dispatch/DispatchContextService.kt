package no.novari.flyt.side.gateway.dispatch

import no.novari.flyt.side.gateway.dispatch.model.DispatchContextEntity
import no.novari.flyt.side.gateway.dispatch.repository.DispatchContextRepository
import no.novari.flyt.side.gateway.instance.SideStudentInstance
import org.springframework.stereotype.Service

@Service
class DispatchContextService(
    private val dispatchContextRepository: DispatchContextRepository,
) {
    fun save(studentInstance: SideStudentInstance) {
        val id = buildDispatchKey(INTEGRATION_STUDENT, studentInstance.instanceId)
        dispatchContextRepository.save(
            DispatchContextEntity(
                id = id,
                sourceApplicationIntegrationId = INTEGRATION_STUDENT,
                sourceApplicationInstanceId = studentInstance.instanceId,
                callbackUrl = studentInstance.callbackUrl,
            ),
        )
    }

    companion object {
        const val INTEGRATION_STUDENT = "sideStudent"

        fun buildDispatchKey(
            sourceApplicationIntegrationId: String,
            sourceApplicationInstanceId: String,
        ): String = "$sourceApplicationIntegrationId:$sourceApplicationInstanceId"
    }
}
