package uk.gov.hmcts.reform.et.syaapi.service;

import uk.gov.hmcts.reform.et.syaapi.models.DocmosisDocument;

/**
 * This is a service to generate documents given relevant data and the necessary template.
 * <p/>
 * This service relies upon Docmosis as it's engine to generate required documents.
 * <p/>
 * This relies upon the following configurations to be set at an environment level:
 * <ul>
 *     <li>TORNADO_URL</li>
 *     <li>TORNADO_ACCESS_KEY</li>
 * </ul>
 * </p>
 * Docmosis is typically installed on all environments within HMCTS.  You can see more information about these
 * environments at: https://tools.hmcts.net/confluence/pages/viewpage.action?pageId=1343291506
 * </p>
 * <b>Note:</b></br>
 * The templates are stored within the repo: https://github.com/hmcts/rdo-docmosis
 * </br>
 * There is a catch.  This applies to all NON-PRODUCTION environments.  The production environment follows a different
 * path, whereby the template would need to be uploaded to a sharepoint location which is documented in the page above.
 */
public class DocumentGenerationService {

    /**
     * This will generate a document based upon the template name provided and the source data to populate elements
     * within the template.  The response from this will be a byte array of the PDF document.
     *
     * @param templateName   the name of the template that the Docmosis instance is aware of.
     * @param outputFileName the filename of the output document we are generating
     * @param sourceData     the {@link DocmosisDocument} that contains all data to be populated in the template, in
     *                       the structure expected
     * @return a byte array of the generated document in raw format
     * @throws DocumentGenerationException should there be a problem with generating the document
     */
    public byte[] genPdfDocument(String templateName, String outputFileName, DocmosisDocument sourceData)
        throws DocumentGenerationException {
        throw new DocumentGenerationException("blah");
    }
}
