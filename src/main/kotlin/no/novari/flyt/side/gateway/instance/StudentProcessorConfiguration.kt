package no.novari.flyt.side.gateway.instance

import no.novari.flyt.gateway.webinstance.InstanceProcessor
import no.novari.flyt.gateway.webinstance.InstanceProcessorFactoryService
import no.novari.flyt.side.gateway.instance.mapping.StudentMappingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StudentProcessorConfiguration {
    @Bean
    fun sideStudentProcessor(
        instanceProcessorFactoryService: InstanceProcessorFactoryService,
        studentMappingService: StudentMappingService,
    ): InstanceProcessor<SideStudentInstance> {
        return instanceProcessorFactoryService.createInstanceProcessor(
            "sideStudent",
            { instance -> instance.instanceId },
            studentMappingService,
        )
    }
}
