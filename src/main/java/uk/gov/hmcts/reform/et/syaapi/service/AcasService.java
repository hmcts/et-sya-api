package uk.gov.hmcts.reform.et.syaapi.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.model.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.model.AcasCertificateRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This provides services to access the ACAS external service.
 */
@Service
public class AcasService {
    private final String acasBaseUrl = "https://api-dev-acas-01.azure-api.net/ECCLDev";   // todo - push this in to an env variable
    public static final String VALID_ACAS_NUMBER_REGEX = "[a-zA-Z]{1,2}[\\d]{6}/[\\d]{2}/[\\d]{2}";
    private final RestTemplate restTemplate;
    private static final String ACAS_KEY = "380e7fad52b2403abf42575ca8fba6e2";  // todo - config or secret vault

    /**
     * Returns a list of JSON objects.
     *
     * @param restTemplate (todo)
     */
    public AcasService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * This will call upon ACAS with a set of ACAS case numbers to retrieve their associated certificates, provided in.
     * JSON format containing BASE64 certificate data in the following format:
     * <pre>
     * [
     *     {
     *         "CertificateNumber": "R444444/89/74",
     *         "CertificateDocument": "JVBERi0xLjcNCiW1tbW1...
     *     },
     *     {
     *         "CertificateNumber": "R465745/34/08",
     *         "CertificateDocument": "JVBERi0xLjcNCiW1tbW...
     *    }
     * ]
     * </pre>
     *
     * <p> A CertificateDocument is the blob for the PDF document todo - JS to add in here relevant information (done) </p>
     *
     * @param acasNumbers are the ACAS numbers we are seeking Certificates for
     * @return a JSONArray of document data
     */
    public List<AcasCertificate> getCertificates(String... acasNumbers) throws InvalidAcasNumbersException {
        if (acasNumbers == null) {
            throw new NullPointerException("Null ACAS numbers");
        }

        AtomicInteger index = new AtomicInteger(0);
        List<String> invalidAcasNumbers = new ArrayList<>();

        Arrays.stream(acasNumbers).forEach(acasNumber -> validateAcasNumber(
            acasNumber,
            invalidAcasNumbers,
            index.getAndIncrement()
        ));

        if (!invalidAcasNumbers.isEmpty()) {
            throw new InvalidAcasNumbersException(invalidAcasNumbers);
        }
        // call ACAS with ALL numbers and retrieve the certs
        HttpHeaders headers = new HttpHeaders();
        headers.set("Ocp-Apim-Subscription-Key", ACAS_KEY);
        HttpEntity<AcasCertificateRequest> request = new HttpEntity<>(new AcasCertificateRequest(acasNumbers), headers);
        ResponseEntity<AcasCertificate> responseEntity = restTemplate.postForEntity(
            acasBaseUrl,
            request,
//            List<AcasCertificate>.class -- todo: couldn't find the solution due to type erasure
            null
        );

//        ResponseEntity<List<AcasCertificate>> responseEntity = restTemplate.exchange(
//            acasBaseUrl,
//            HttpMethod.POST,
//            request,
//            new ParameterizedTypeReference<List<AcasCertificate>>() {
//            }
//        );

//        List<AcasCertificate> certificates = responseEntity.getBody(); -- todo: uncomment

        // todo - what happens if the ACAS service isn't available?

        return null;
    }


    private void validateAcasNumber(String acasNumber, List<String> invalidAcasNumbers, int index) {
        if (acasNumber == null) {
            throw new NullPointerException("ACAS number at position #" + index + " must not be null");
        }
        if (!Pattern.compile(VALID_ACAS_NUMBER_REGEX).matcher(acasNumber).matches()) {
            invalidAcasNumbers.add(acasNumber);
        }
    }
}
