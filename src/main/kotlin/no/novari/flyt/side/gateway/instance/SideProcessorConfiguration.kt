package no.novari.flyt.side.gateway.instance

import no.novari.flyt.gateway.instance.InstanceProcessor
import no.novari.flyt.gateway.instance.InstanceProcessorFactoryService
import no.novari.flyt.side.gateway.instance.mapping.SideMappingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SideProcessorConfiguration {
    @Bean
    fun sideProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        sideMappingService: SideMappingService,
    ): InstanceProcessor<SideInstance> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            "side",
            { instance -> instance.instanceId },
            sideMappingService,
        )
    }
}
