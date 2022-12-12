package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificateRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * This provides services to access the ACAS external service for retrieving ACAS Certificate's held in {@link
 * AcasCertificate} objects.
 */
@Slf4j
@Service
public class AcasService {

    public static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    public static final String VALID_ACAS_NUMBER_REGEX = "\\A[a-zA-Z]{1,2}\\d{6}/\\d{2}/\\d{2}\\z";
    public static final int MAX_ACAS_RETRIES = 5;
    private final RestTemplate restTemplate;
    private final String acasApiUrl;
    private final String acasApiKey;

    /**
     * Constructs an {@link AcasService} instance with the RestTemplate to use for talking with the ACAS service.
     *
     * @param restTemplate the RestTemplate to use for talking with the ACAS service
     * @param acasApiUrl   the URL to access the ACAS API
     * @param acasApiKey   the OCP APIM Subscription Key used in the header to authenticate when contacting ACAS
     */
    public AcasService(RestTemplate restTemplate,
                       @Value("${acas.api.url}") String acasApiUrl,
                       @Value("${acas.api.key}") String acasApiKey) {
        this.restTemplate = restTemplate;
        this.acasApiUrl = acasApiUrl;
        this.acasApiKey = acasApiKey;
    }

    /**
     * This will call upon ACAS with a set of ACAS case numbers to retrieve their associated certificates. Validation of
     * the ACAS numbers is first applied and may result in an {@link InvalidAcasNumbersException} being thrown should
     * there be any problems found. If all ACAS numbers are valid, then the service will attempt to retrieve a list of
     * available {@link AcasCertificate}'s associated to the ACAS numbers provided. The service will retry up to 5 times
     * to retrieve them if the call results in an error before then throwing an {@link AcasException} with the
     * associated cause.
     *
     * @param acasNumbers are the ACAS numbers we are seeking Certificates for
     * @return a List of {@link AcasCertificate}'s associated to the provided acasNumbers that are available at ACAS
     * @throws AcasException               if a problem occurs obtaining the certificates.
     * @throws InvalidAcasNumbersException if any of the acas numbers provided are invalid.
     */
    public List<AcasCertificate> getCertificates(String... acasNumbers)
        throws InvalidAcasNumbersException, AcasException {
        List<AcasCertificate> acasCertificates = new ArrayList<>();
        if (acasNumbers != null && acasNumbers.length > 0) {
            validateAcasNumbers(acasNumbers);
            acasCertificates = attemptWithRetriesToFetchAcasCertificates(0, acasNumbers);
        }
        return acasCertificates;
    }

    private List<AcasCertificate> attemptWithRetriesToFetchAcasCertificates(int attempts, String... acasNumbers)
        throws AcasException {
        try {
            List<AcasCertificate> acasCertificates = fetchAcasCertificates(acasNumbers).getBody();
            log.info("Retrieved AcasCertificates: {} using AcasNumbers: {}",
                     acasCertificates,
                     acasNumbers
            );
            return acasCertificates;
        } catch (RestClientResponseException e) {
            if (attempts < MAX_ACAS_RETRIES) {
                return attemptWithRetriesToFetchAcasCertificates(attempts + 1, acasNumbers);
            }
            log.info("AcasCertificates retrieval for AcasNumbers: {} has failed after {} attempts with "
                         + "the exception: {}",
                     acasNumbers,
                     attempts,
                     e);
            throw new AcasException("Failed to obtain certificates for acas numbers" + Arrays.toString(acasNumbers), e);
        }
    }

    private ResponseEntity<List<AcasCertificate>> fetchAcasCertificates(String... acasNumbers) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(OCP_APIM_SUBSCRIPTION_KEY, acasApiKey);
        AcasCertificateRequest acasCertificateRequest = new AcasCertificateRequest();
        acasCertificateRequest.setCertificateNumbers(acasNumbers);
        HttpEntity<AcasCertificateRequest> request = new HttpEntity<>(acasCertificateRequest, headers);
        return restTemplate.exchange(
            acasApiUrl,
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<>() {
            }
        );
    }

    private void validateAcasNumbers(String... acasNumbers) throws InvalidAcasNumbersException {
        AtomicInteger index = new AtomicInteger(0);
        StringBuilder nullValueError = new StringBuilder();
        List<String> invalidAcasNumbers = new ArrayList<>();

        Arrays.stream(acasNumbers).forEach(acasNumber -> validateAcasNumber(
            acasNumber,
            invalidAcasNumbers,
            nullValueError,
            index.getAndIncrement()
        ));
        if (!invalidAcasNumbers.isEmpty() || nullValueError.length() > 0) {
            throw new InvalidAcasNumbersException(nullValueError.toString(), invalidAcasNumbers.toArray(new String[0]));
        }
    }

    private void validateAcasNumber(String acasNumber, List<String> invalidAcasNumbers,
                                    StringBuilder nullValue, int index) {
        if (acasNumber == null) {
            nullValue.append("[ACAS number at position #")
                .append(index)
                .append(" must not be null]");
            return;
        }
        char lastChar = acasNumber.charAt(acasNumber.length() - 1);
        if (!Pattern.compile(VALID_ACAS_NUMBER_REGEX).matcher(acasNumber).matches() || lastChar == '/') {
            invalidAcasNumbers.add(acasNumber);
        }
    }

    /**
     * This will iterate through each respondent in the case and extract the acas certificate numbers. This
     * list of certificate numbers are converted into {@link AcasCertificate}
     * @param caseData passed to extract Acas Certificates from
     * @return the list of {@link AcasCertificate}
     * @throws AcasException if a problem occurs obtaining the certificates.
     * @throws InvalidAcasNumbersException if any of the acas numbers provided are invalid.
     */
    public List<AcasCertificate> getAcasCertificatesByCaseData(CaseData caseData)
        throws AcasException, InvalidAcasNumbersException {
        List<String> acasCertificateNumbers = new ArrayList<>();
        if (caseData.getRespondentCollection() != null && !caseData.getRespondentCollection().isEmpty()) {
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                if (respondentSumTypeItem.getValue() != null
                    && !StringUtils.isEmpty(respondentSumTypeItem.getValue().getRespondentAcas())) {
                    acasCertificateNumbers.add(respondentSumTypeItem.getValue().getRespondentAcas());
                }
            }
        }
        return getCertificates(acasCertificateNumbers.toArray(new String[0]));
    }

}
