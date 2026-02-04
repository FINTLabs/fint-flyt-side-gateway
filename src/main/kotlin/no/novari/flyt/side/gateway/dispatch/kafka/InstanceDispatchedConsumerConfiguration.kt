package no.novari.flyt.side.gateway.dispatch.kafka

import no.novari.flyt.kafka.instanceflow.consuming.InstanceFlowListenerFactoryService
import no.novari.flyt.side.gateway.dispatch.SideDispatchService
import no.novari.kafka.consuming.ErrorHandlerConfiguration
import no.novari.kafka.consuming.ErrorHandlerFactory
import no.novari.kafka.consuming.ListenerConfiguration
import no.novari.kafka.topic.EventTopicService
import no.novari.kafka.topic.configuration.EventCleanupFrequency
import no.novari.kafka.topic.configuration.EventTopicConfiguration
import no.novari.kafka.topic.name.EventTopicNameParameters
import no.novari.kafka.topic.name.TopicNamePrefixParameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.util.backoff.FixedBackOff
import java.time.Duration

@Configuration
@ConditionalOnProperty(
    prefix = "novari.flyt.side.dispatch",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class InstanceDispatchedConsumerConfiguration(
    private val eventTopicService: EventTopicService,
    private val sideDispatchService: SideDispatchService,
) {
    @Bean
    fun instanceDispatchedConsumer(
        instanceFlowListenerFactoryService: InstanceFlowListenerFactoryService,
        errorHandlerFactory: ErrorHandlerFactory,
        @Value("\${novari.flyt.side.dispatch.retry-interval:10s}") retryInterval: Duration,
        @Value("\${novari.flyt.side.dispatch.retry-attempts:3}") retryAttempts: Long,
    ): ConcurrentMessageListenerContainer<String, Any> {
        val topic =
            EventTopicNameParameters
                .builder()
                .topicNamePrefixParameters(
                    TopicNamePrefixParameters
                        .stepBuilder()
                        .orgIdApplicationDefault()
                        .domainContextApplicationDefault()
                        .build(),
                ).eventName("instance-dispatched")
                .build()

        eventTopicService.createOrModifyTopic(
            topic,
            EventTopicConfiguration
                .stepBuilder()
                .partitions(PARTITIONS)
                .retentionTime(RETENTION_TIME)
                .cleanupFrequency(CLEANUP_FREQUENCY)
                .build(),
        )

        val errorHandler =
            errorHandlerFactory
                .createErrorHandler(
                    ErrorHandlerConfiguration
                        .builder<Any>()
                        .defaultBackoff(FixedBackOff(retryInterval.toMillis(), retryAttempts))
                        .build(),
                ).apply { isAckAfterHandle = false }

        return instanceFlowListenerFactoryService
            .createRecordListenerContainerFactory(
                Any::class.java,
                { record -> sideDispatchService.handleInstanceDispatched(record.instanceFlowHeaders) },
                ListenerConfiguration
                    .stepBuilder()
                    .groupIdApplicationDefault()
                    .maxPollRecordsKafkaDefault()
                    .maxPollIntervalKafkaDefault()
                    .continueFromPreviousOffsetOnAssignment()
                    .build(),
                errorHandler,
            ).createContainer(topic)
    }

    companion object {
        private const val PARTITIONS = 1
        private val RETENTION_TIME = Duration.ofDays(7)
        private val CLEANUP_FREQUENCY = EventCleanupFrequency.NORMAL
    }
}
