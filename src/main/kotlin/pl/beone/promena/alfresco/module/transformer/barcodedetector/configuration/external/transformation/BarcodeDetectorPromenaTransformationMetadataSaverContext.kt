package pl.beone.promena.alfresco.module.transformer.barcodedetector.configuration.external.transformation

import org.alfresco.service.ServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.transformer.barcodedetector.external.transformation.BarcodeDetectorPromenaTransformationMetadataSaver

@Configuration
class BarcodeDetectorPromenaTransformationMetadataSaverContext {

    @Bean
    fun barcodeDetectorPromenaTransformationMetadataSaver(
        serviceRegistry: ServiceRegistry
    ) =
        BarcodeDetectorPromenaTransformationMetadataSaver(
            serviceRegistry
        )
}