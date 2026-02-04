package no.novari.flyt.side.gateway.dispatch

import com.fasterxml.jackson.databind.ObjectMapper
import no.novari.flyt.gateway.webinstance.kafka.ArchiveCaseIdRequestService
import no.novari.flyt.side.gateway.dispatch.DispatchContextService.Companion.INTEGRATION_STUDENT
import no.novari.flyt.side.gateway.dispatch.DispatchContextService.Companion.buildDispatchKey
import no.novari.flyt.side.gateway.dispatch.model.DispatchReceiptEntity
import no.novari.flyt.side.gateway.dispatch.repository.DispatchContextRepository
import no.novari.flyt.side.gateway.dispatch.repository.DispatchReceiptRepository
import no.novari.flyt.kafka.instanceflow.headers.InstanceFlowHeaders
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
@ConditionalOnProperty(
    prefix = "novari.flyt.side.dispatch",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class SideDispatchService(
    private val restClient: RestClient,
    private val dispatchContextRepository: DispatchContextRepository,
    private val dispatchReceiptRepository: DispatchReceiptRepository,
    private val archiveCaseIdRequestService: ArchiveCaseIdRequestService,
    private val objectMapper: ObjectMapper,
) {
    fun handleInstanceDispatched(instanceFlowHeaders: InstanceFlowHeaders) {
        val sourceApplicationIntegrationId = instanceFlowHeaders.sourceApplicationIntegrationId
        if (sourceApplicationIntegrationId != INTEGRATION_STUDENT) {
            log.debug(
                "Skipping instance-dispatched for sourceApplicationIntegrationId={}",
                sourceApplicationIntegrationId,
            )
            return
        }

        val sourceApplicationInstanceId =
            instanceFlowHeaders.sourceApplicationInstanceId
                ?: error("Missing sourceApplicationInstanceId in instance-dispatched headers")

        val dispatchKey = buildDispatchKey(sourceApplicationIntegrationId, sourceApplicationInstanceId)
        val existingReceipt = dispatchReceiptRepository.findById(dispatchKey).orElse(null)
        if (existingReceipt != null) {
            dispatchReceipt(existingReceipt)
            return
        }

        val dispatchContext =
            dispatchContextRepository.findById(dispatchKey).orElse(null)
                ?: error("Missing dispatch context for sourceApplicationInstanceId=$sourceApplicationInstanceId")

        val payload = buildPayload(instanceFlowHeaders, sourceApplicationInstanceId)

        val receipt =
            DispatchReceiptEntity(
                id = dispatchKey,
                sourceApplicationIntegrationId = sourceApplicationIntegrationId,
                sourceApplicationInstanceId = sourceApplicationInstanceId,
                callbackUrl = dispatchContext.callbackUrl,
                payload = objectMapper.writeValueAsString(payload),
            )

        dispatchReceiptRepository.save(receipt)
        dispatchContextRepository.delete(dispatchContext)
        dispatchReceipt(receipt)
    }

    @Scheduled(
        initialDelayString = "\${novari.flyt.side.dispatch.retry-initial-delay:5m}",
        fixedDelayString = "\${novari.flyt.side.dispatch.retry-fixed-delay:24h}",
    )
    fun retryFailedDispatches() {
        val pendingDispatches = dispatchReceiptRepository.findAll()
        if (pendingDispatches.isEmpty()) {
            return
        }

        log.info("Retrying {} dispatch receipts", pendingDispatches.size)
        pendingDispatches.forEach { receipt ->
            try {
                dispatchReceipt(receipt)
            } catch (ex: Exception) {
                log.warn("Retry failed for dispatchReceiptId={}", receipt.id, ex)
            }
        }
    }

    private fun buildPayload(
        instanceFlowHeaders: InstanceFlowHeaders,
        sourceApplicationInstanceId: String,
    ): SideDispatchPayload {
        val sourceApplicationId = instanceFlowHeaders.sourceApplicationId
        val archiveCaseId =
            instanceFlowHeaders.archiveInstanceId
                ?: archiveCaseIdRequestService.getArchiveCaseId(sourceApplicationId, sourceApplicationInstanceId)
                ?: error("Missing archiveCaseId for instanceId=$sourceApplicationInstanceId")

        return SideDispatchPayload(
            instanceId = sourceApplicationInstanceId,
            archiveCaseId = archiveCaseId,
        )
    }

    private fun dispatchReceipt(receipt: DispatchReceiptEntity) {
        log.debug(
            "Dispatching sourceApplicationInstanceId={} via PUT to callback={}",
            receipt.sourceApplicationInstanceId,
            receipt.callbackUrl,
        )

        val payloadNode = objectMapper.readTree(receipt.payload)

        try {
            restClient
                .put()
                .uri(receipt.callbackUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payloadNode)
                .retrieve()
                .toBodilessEntity()

            dispatchReceiptRepository.delete(receipt)
        } catch (ex: Exception) {
            if (isPermanentFailure(ex)) {
                log.warn(
                    "Permanent dispatch failure, deleting receipt id={} callbackUrl={}",
                    receipt.id,
                    receipt.callbackUrl,
                    ex,
                )
                dispatchReceiptRepository.delete(receipt)
                return
            }
            throw ex
        }
    }

    private fun isPermanentFailure(ex: Exception): Boolean =
        when (ex) {
            is IllegalArgumentException -> true
            is ResourceAccessException -> true
            is RestClientResponseException -> isPermanentHttpStatus(ex)
            else -> false
        }

    private fun isPermanentHttpStatus(ex: RestClientResponseException): Boolean {
        val status = ex.statusCode
        if (!status.is4xxClientError) {
            return false
        }
        return status.value() != 408 && status.value() != 429
    }

    companion object {
        private val log = LoggerFactory.getLogger(SideDispatchService::class.java)
    }
}
