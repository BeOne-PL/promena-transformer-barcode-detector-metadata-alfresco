package pl.beone.promena.alfresco.module.transformer.barcodedetector.external.transformation

import mu.KotlinLogging
import org.alfresco.service.ServiceRegistry
import org.alfresco.service.cmr.repository.ChildAssociationRef
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.NamespaceService.CONTENT_MODEL_1_0_URI
import org.alfresco.service.namespace.QName
import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.ASSOCIATION_BARCODES
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.ASSOCIATION_CONTOUR_VERTICES_ON_PAGE
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_FORMAT
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_PAGE
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_TEXT
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_X
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_Y
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.TYPE_BARCODE
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.TYPE_VERTEX
import pl.beone.promena.transformer.barcodedetector.metadata.BarcodeDetectorMetadata
import pl.beone.promena.transformer.barcodedetector.metadata.BarcodeDetectorMetadata.Barcode
import pl.beone.promena.transformer.barcodedetector.metadata.BarcodeDetectorMetadata.Barcode.Vertex
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Metadata
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.io.Serializable

class BarcodeDetectorPromenaTransformationMetadataSaver(
    private val serviceRegistry: ServiceRegistry
) : PromenaTransformationMetadataSaver {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun save(
        sourceNodeRef: NodeRef,
        transformation: Transformation,
        transformedDataDescriptor: TransformedDataDescriptor,
        transformedNodeRefs: List<NodeRef>
    ) {
        val singleTransformedDataDescriptors = transformedDataDescriptor.descriptors

        if (transformedNodeRefs.isEmpty() || notContainAnyBarcodeDetectorMetadata(singleTransformedDataDescriptors)) {
            return
        }

        if (singleTransformedDataDescriptors.size != transformedNodeRefs.size) {
            logger.warn {
                "Transformed data descriptors and transformed node refs have different number of elements (<${singleTransformedDataDescriptors.size}> and <${transformedNodeRefs}>). " +
                        "Saving all metadata in first transformed node"
            }
            singleTransformedDataDescriptors.forEach { (_, metadata) -> saveMetadata(transformedNodeRefs[0], metadata) }
        } else {
            transformedNodeRefs.zip(singleTransformedDataDescriptors)
                .forEach { (nodeRef, singleTransformedDataDescriptor) -> saveMetadata(nodeRef, singleTransformedDataDescriptor.metadata) }
        }
    }

    private fun notContainAnyBarcodeDetectorMetadata(singleTransformedDataDescriptors: List<TransformedDataDescriptor.Single>): Boolean =
        singleTransformedDataDescriptors.asSequence()
            .map { (_, metadata) -> ifExists { BarcodeDetectorMetadata(metadata).getBarcodes() } ?: false }
            .contains(true)

    private fun saveMetadata(nodeRef: NodeRef, metadata: Metadata) {
        try {
            BarcodeDetectorMetadata(metadata).getBarcodes()
                .forEach { saveBarcode(nodeRef, it) }
        } catch (e: Exception) {
        }
    }

    private fun saveBarcode(parentNodeRef: NodeRef, barcode: Barcode): ChildAssociationRef? {
        val text = ifExists { barcode.getText() }
        val format = ifExists { barcode.getFormat() }
        val page = ifExists { barcode.getPage() }

        return serviceRegistry.nodeService.createNode(
            parentNodeRef,
            ASSOCIATION_BARCODES,
            createQName(CONTENT_MODEL_1_0_URI, createName(text, format, page)),
            TYPE_BARCODE,
            mapOf<QName, Serializable?>(
                PROPERTY_TEXT to text,
                PROPERTY_FORMAT to format,
                PROPERTY_PAGE to page
            ).filterNotNullValues()
        ).also { childAssociationRef -> barcode.getContourVerticesOnPage().forEach { saveVertex(childAssociationRef.childRef, it) } }
    }

    private fun saveVertex(parentNodeRef: NodeRef, vertex: Vertex) {
        val x = ifExists { vertex.getX() }
        val y = ifExists { vertex.getY() }

        serviceRegistry.nodeService.createNode(
            parentNodeRef,
            ASSOCIATION_CONTOUR_VERTICES_ON_PAGE,
            createQName(CONTENT_MODEL_1_0_URI, createName(x, y)),
            TYPE_VERTEX,
            mapOf(
                PROPERTY_X to x,
                PROPERTY_Y to y
            ).filterNotNullValues()
        )
    }

    private fun createName(vararg elements: Any?): String =
        elements.joinToString(", ")

    private fun <T> ifExists(toExecute: () -> T): T? =
        try {
            toExecute()
        } catch (e: NoSuchElementException) {
            null
        }

    private fun <K, V> Map<K, V>.filterNotNullValues(): Map<K, V> =
        filterValues { it != null }
}