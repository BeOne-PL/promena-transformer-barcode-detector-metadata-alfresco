package pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model

import org.alfresco.service.namespace.QName.createQName
import pl.beone.promena.alfresco.module.transformer.barcodedetector.applicationmodel.model.PromenaBarcodeDetectorNamespace.PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI

object PromenaBarcodeDetectorModel {

    @JvmField
    val ASPECT_METADATA = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "metadata")!!
    @JvmField
    val ASSOCIATION_BARCODES = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "barcodes")!!

    @JvmField
    val TYPE_BARCODE = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "barcode")!!
    @JvmField
    val PROPERTY_TEXT = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "text")!!
    @JvmField
    val PROPERTY_FORMAT = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "format")!!
    @JvmField
    val PROPERTY_PAGE = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "page")!!
    @JvmField
    val ASSOCIATION_CONTOUR_VERTICES_ON_PAGE = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "contourVerticesOnPage")!!

    @JvmField
    val TYPE_VERTEX = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "vertex")!!
    @JvmField
    val PROPERTY_X = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "x")!!
    @JvmField
    val PROPERTY_Y = createQName(PROMENA_BARCODE_DETECTOR_MODEL_1_0_URI, "y")!!
}