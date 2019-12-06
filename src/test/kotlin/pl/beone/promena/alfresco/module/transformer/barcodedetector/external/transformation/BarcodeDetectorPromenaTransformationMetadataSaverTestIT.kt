package pl.beone.promena.alfresco.module.transformer.barcodedetector.external.transformation

import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.collections.shouldNotContain
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.maps.shouldNotContainKey
import io.kotlintest.matchers.maps.shouldNotContainKeys
import io.mockk.mockk
import org.alfresco.model.ContentModel
import org.alfresco.model.ContentModel.PROP_NODE_DBID
import org.alfresco.model.ContentModel.TYPE_CONTENT
import org.alfresco.rad.test.AbstractAlfrescoIT
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator
import org.alfresco.service.cmr.model.FileExistsException
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.namespace.QName
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.ASPECT_METADATA
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.ASSOCIATION_BARCODES
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.ASSOCIATION_CONTOUR_VERTICES_ON_PAGE
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_FORMAT
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_PAGE
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_TEXT
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_X
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorModel.PROPERTY_Y
import pl.beone.promena.transformer.barcodedetector.metadata.*
import pl.beone.promena.transformer.contract.data.plus
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.metadata.emptyMetadata
import java.io.Serializable
import java.time.LocalDateTime

@RunWith(AlfrescoTestRunner::class)
class BarcodeDetectorPromenaTransformationMetadataSaverTestIT : AbstractAlfrescoIT() {

    companion object {
        private val metadataBarcode =
            barcode() addText "0123456789" addFormat "QR Code" addPage 1 addContourVertexOnPage (10 to 11) addContourVertexOnPage (20 to 21)
        private val metadataBarcode2 = barcode()
        private val metadata = barcodeDetectorMetadata() addBarcode metadataBarcode addBarcode metadataBarcode2

        private val metadata2Barcode = barcode() addText "01234565" addFormat "EAN-8"
        private val metadata2 = barcodeDetectorMetadata() addBarcode metadata2Barcode
    }

    @Test
    fun save_theSameNumberOfTransformedDataDescriptorsAndTransformedDataRefsElements() {
        val integrationTestsFolder = createOrGetIntegrationTestsFolder()
        val nodeRefs = listOf(integrationTestsFolder.createNode(), integrationTestsFolder.createNode())
        val transformedNodeRef = integrationTestsFolder.createNode()
        val transformedNodeRef2 = integrationTestsFolder.createNode()
        val transformedNodeRef3 = integrationTestsFolder.createNode()

        BarcodeDetectorPromenaTransformationMetadataSaver(serviceRegistry)
            .save(
                nodeRefs,
                mockk(),
                singleTransformedDataDescriptor(mockk(), metadata) +
                        singleTransformedDataDescriptor(mockk(), emptyMetadata()) +
                        singleTransformedDataDescriptor(mockk(), metadata2),
                listOf(transformedNodeRef, transformedNodeRef2, transformedNodeRef3)
            )

        nodeRefs.forEach(::validateNoMetadataAspectAndBarcodeAssociations)
        with(transformedNodeRef) {
            getAspects() shouldContain ASPECT_METADATA
            with(getBarcodeAssociationNodeRefs().sortByDbId()) {
                this shouldHaveSize 2
                validateMetadataBarcode(this[0])
                validateMetadataBarcode2(this[1])
            }
        }
        validateNoMetadataAspectAndBarcodeAssociations(transformedNodeRef2)
        with(transformedNodeRef3) {
            getAspects() shouldContain ASPECT_METADATA
            with(getBarcodeAssociationNodeRefs()) {
                this shouldHaveSize 1

                validateMetadata2Barcode(this[0])
            }
        }
    }

    @Test
    fun save_differentNumberOfTransformedDataDescriptorsAndTransformedDataRefsElements() {
        val integrationTestsFolder = createOrGetIntegrationTestsFolder()
        val nodeRefs = listOf(integrationTestsFolder.createNode(), integrationTestsFolder.createNode())
        val transformedNodeRef = integrationTestsFolder.createNode()

        BarcodeDetectorPromenaTransformationMetadataSaver(serviceRegistry)
            .save(
                nodeRefs,
                mockk(),
                singleTransformedDataDescriptor(mockk(), metadata) +
                        singleTransformedDataDescriptor(mockk(), emptyMetadata()) +
                        singleTransformedDataDescriptor(mockk(), metadata2),
                listOf(transformedNodeRef)
            )

        nodeRefs.forEach(::validateNoMetadataAspectAndBarcodeAssociations)
        with(transformedNodeRef) {
            getAspects() shouldContain ASPECT_METADATA
            with(getBarcodeAssociationNodeRefs().sortByDbId()) {
                this shouldHaveSize 3
                validateMetadataBarcode(this[0])
                validateMetadataBarcode2(this[1])
                validateMetadata2Barcode(this[2])
            }
        }
    }

    @Test
    fun save_noBarcodeDetectorMetadataInTransformedData() {
        val integrationTestsFolder = createOrGetIntegrationTestsFolder()
        val nodeRefs = listOf(integrationTestsFolder.createNode(), integrationTestsFolder.createNode())
        val transformedNodeRef = integrationTestsFolder.createNode()

        BarcodeDetectorPromenaTransformationMetadataSaver(serviceRegistry)
            .save(
                nodeRefs,
                mockk(),
                singleTransformedDataDescriptor(mockk(), emptyMetadata()) +
                        singleTransformedDataDescriptor(mockk(), emptyMetadata()),
                listOf(transformedNodeRef)
            )

        nodeRefs.forEach(::validateNoMetadataAspectAndBarcodeAssociations)
        validateNoMetadataAspectAndBarcodeAssociations(transformedNodeRef)
    }

    @Test
    fun save_noTransformedNodeRefs() {
        val integrationTestsFolder = createOrGetIntegrationTestsFolder()
        val nodeRefs = listOf(integrationTestsFolder.createNode(), integrationTestsFolder.createNode())

        BarcodeDetectorPromenaTransformationMetadataSaver(serviceRegistry)
            .save(
                nodeRefs,
                mockk(),
                singleTransformedDataDescriptor(mockk(), metadata) +
                        singleTransformedDataDescriptor(mockk(), emptyMetadata()),
                emptyList()
            )

        nodeRefs.forEach(::validateNoMetadataAspectAndBarcodeAssociations)
    }

    private fun validateNoMetadataAspectAndBarcodeAssociations(nodeRef: NodeRef) {
        with(nodeRef) {
            getAspects() shouldNotContain ASPECT_METADATA
            getBarcodeAssociationNodeRefs() shouldHaveSize 0
        }
    }

    private fun validateMetadataBarcode(nodeRef: NodeRef) {
        with(nodeRef) {
            getProperties() shouldContainAll mapOf(
                PROPERTY_TEXT to metadataBarcode.getText(),
                PROPERTY_FORMAT to metadataBarcode.getFormat(),
                PROPERTY_PAGE to metadataBarcode.getPage()
            )
            with(getContourVerticesOnPageAssociationNodeRefs()) {
                this shouldHaveSize 2
                with(this[0]) {
                    getProperties() shouldContainAll mapOf(
                        PROPERTY_X to metadataBarcode.getContourVerticesOnPage()[0].getX(),
                        PROPERTY_Y to metadataBarcode.getContourVerticesOnPage()[0].getY()
                    )
                }
                with(this[1]) {
                    getProperties() shouldContainAll mapOf(
                        PROPERTY_X to metadataBarcode.getContourVerticesOnPage()[1].getX(),
                        PROPERTY_Y to metadataBarcode.getContourVerticesOnPage()[1].getY()
                    )
                }
            }
        }
    }

    private fun validateMetadataBarcode2(nodeRef: NodeRef) {
        with(nodeRef) {
            getProperties().shouldNotContainKeys(PROPERTY_TEXT, PROPERTY_FORMAT, PROPERTY_PAGE)
            getContourVerticesOnPageAssociationNodeRefs() shouldHaveSize 0
        }
    }

    private fun validateMetadata2Barcode(nodeRef: NodeRef) {
        with(nodeRef) {
            with(getProperties()) {
                this shouldContainAll mapOf(
                    PROPERTY_TEXT to metadata2Barcode.getText(),
                    PROPERTY_FORMAT to metadata2Barcode.getFormat()
                )
                this shouldNotContainKey PROPERTY_PAGE
            }
            getContourVerticesOnPageAssociationNodeRefs() shouldHaveSize 0
        }
    }

    private fun createOrGetIntegrationTestsFolder(): NodeRef =
        try {
            serviceRegistry.fileFolderService.create(getCompanyHomeNodeRef(), "Integration test", ContentModel.TYPE_FOLDER)
                .nodeRef
        } catch (e: FileExistsException) {
            serviceRegistry.fileFolderService.searchSimple(getCompanyHomeNodeRef(), "Integration test")
        }

    private fun getCompanyHomeNodeRef(): NodeRef =
        serviceRegistry.nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null)

    private fun NodeRef.createNode(): NodeRef =
        serviceRegistry.fileFolderService.create(this, LocalDateTime.now().toString().replace(":", "_"), TYPE_CONTENT).nodeRef

    private fun NodeRef.getAspects(): List<QName> =
        serviceRegistry.nodeService.getAspects(this).toList()

    private fun NodeRef.getProperties(): Map<QName, Serializable> =
        serviceRegistry.nodeService.getProperties(this)

    private fun NodeRef.getBarcodeAssociationNodeRefs(): List<NodeRef> =
        serviceRegistry.nodeService.getChildAssocs(this)
            .filter { it.typeQName == ASSOCIATION_BARCODES }
            .map { it.childRef }
            .toList()

    private fun NodeRef.getContourVerticesOnPageAssociationNodeRefs(): List<NodeRef> =
        serviceRegistry.nodeService.getChildAssocs(this)
            .filter { it.typeQName == ASSOCIATION_CONTOUR_VERTICES_ON_PAGE }
            .map { it.childRef }
            .toList()

    private fun List<NodeRef>.sortByDbId(): List<NodeRef> =
        sortedBy { it.getProperties()[PROP_NODE_DBID]?.toString()?.toInt() }
}