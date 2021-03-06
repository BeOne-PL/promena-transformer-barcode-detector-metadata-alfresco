# Promena Alfresco module - `barcode detector` metadata
This module registers [`BarcodeDetectorPromenaTransformationMetadataSaver`](./src/main/kotlin/pl/beone/promena/alfresco/module/transformer/barcodedetector/external/transformation/BarcodeDetectorPromenaTransformationMetadataSaver.kt) that saves [`barcode-detector-metadata`](https://github.com/BeOne-PL/promena-transformer-barcode-detector-metadata) (if it's returned) from a transformation in Alfresco in the following format:
* Each [`Metadata`](https://github.com/BeOne-PL/promena/blob/master/base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/model/Metadata.kt) of [`TransformedDataDescriptor`](https://github.com/BeOne-PL/promena/blob/master/base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/data/TransformedDataDescriptor.kt) is saved in the transformed node as the child association (`promenaBarcodeDetector:barcodes`) with type `promenaBarcodeDetector:barcode` and properties (the explanation of each element is described in [`barcode-detector-metadata`](https://github.com/BeOne-PL/promena-transformer-barcode-detector-metadata)):
    * `promenaBarcodeDetector:text`
    * `promenaBarcodeDetector:format`
    * `promenaBarcodeDetector:page`
    * Each vertex is saved as the child association (`promenaBarcodeDetector:contourVerticesOnPage`) with type `promenaBarcodeDetector:vertex` and properties:
        * `promenaBarcodeDetector:x`
        * `promenaBarcodeDetector:y`
* `promenaBarcodeDetector:metadata` aspect is added to each transformed node