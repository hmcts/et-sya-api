package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AcasServiceTest {

    private static final String ACAS_DEV_API_URL = "https://api-dev-acas-01.azure-api.net/ECCLDev";
    private static final String ACAS_API_KEY = "ecd18c987120415b95f60b844e6730ef";
    private static final String NO_CERTS_JSON = "[]";
    private static final String ONE_CERT_JSON =
        "[{\"CertificateNumber\":\"A123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"}]";
    private static final String TWO_CERTS_JSON =
        "[{\"CertificateNumber\":\"AB123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"},"
            + "{\"CertificateNumber\":\"A123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW...\"}]";
    public static final String A123 = "A123";
    public static final String Z456 = "Z456";
    public static final String R123456_11_12 = "R123456/11/12";
    public static final String R123456_13_14 = "R123456/13/14";
    private AcasService acasService;
    private RestTemplate restTemplate;
    private TestData testData;

    @BeforeEach
    void setup() {
        testData = new TestData();
        restTemplate = new RestTemplate();
        acasService = new AcasService(restTemplate, ACAS_DEV_API_URL, ACAS_API_KEY);
    }

    @Test
    void theGetAcasCertWithNullProducesInvalidAcasNumbersException() {

        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates((String) null));
        assertThat(exception.getMessage())
            .isEqualTo("[ACAS number at position #0 must not be null]");
        assertThat(exception.getInvalidAcasNumbers())
            .isEmpty();
    }

    @Test
    void theGetAcasCertWithOneNullProducesInvalidAcasNumbersException() {

        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates(R123456_11_12, null));
        assertThat(exception.getMessage())
            .isEqualTo("[ACAS number at position #1 must not be null]");
        assertThat(exception.getInvalidAcasNumbers())
            .isEmpty();
    }

    @Test
    void theGetAcasCertWithMultipleNullsProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates(null, null, null));
        assertThat(exception.getMessage())
            .isEqualTo("[ACAS number at position #0 must not be null][ACAS number at position #1 must not be "
                + "null][ACAS number at position #2 must not be null]");
        assertThat(exception.getInvalidAcasNumbers())
            .isEmpty();
    }

    @Test
    void theGetAcasCertWithInvalidAcasNumberProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates(A123));
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly(A123);
        assertThat(exception.getMessage())
            .isBlank();
    }

    // testing acas number too long, too short and wrong format entirely
    @ParameterizedTest
    @ValueSource(strings = {"AB123456/12/123", "A123456/12/1", "ACAS1234567890"})
    void theGetAcasCertWithInvalidAcasNumberProducesInvalidAcasNumbersException(String value) {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates(value)
        );
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly(value);
    }

    @Test
    void theGetAcasCertsWithInvalidAcasNumbersProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates(A123, Z456));
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly(A123, Z456);
        assertThat(exception.getMessage())
            .isBlank();
    }

    private MockRestServiceServer getMockServer() {
        return MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12))
            .hasSize(1);
    }

    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(R123456_13_14))
            .hasSize(1);
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(TWO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .hasSize(2);
    }

    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertNotFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12))
            .isEmpty();
    }

    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertNotFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(R123456_13_14))
            .isEmpty();
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsNotFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .isEmpty();
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersWithOneEmptyProducesOneCertFound()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: A123456/12/12
        // Inalid: ZZ123456/12/12
        List<AcasCertificate> acasCertificates = acasService.getCertificates(R123456_11_12, R123456_13_14);
        assertThat(acasCertificates)
            .hasSize(1);
    }

    @Test
    void theGetAcasCertsWithOneValidAndTwoInvalidAcasNumbersProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("R123", R123456_13_14, "R456", "R123456/13/1/")
        );
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly("R123", "R456", "R123456/13/1/");
    }

    @Test
    void theGetAcasCertWithWrongUrlProducesAcasException() {
        getMockServer().expect(ExpectedCount.manyTimes(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(AcasException.class, () -> acasService.getCertificates(R123456_11_12));
    }

    @Test
    void theGetAcasCertWithBadApiKeyProducesAcasException() {
        getMockServer().expect(ExpectedCount.manyTimes(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(AcasException.class, () -> acasService.getCertificates(R123456_11_12));
    }

    @Test
    void theGetAcasCertWithBadApiKeyFirstTimeProducesCertificates()
        throws AcasException, InvalidAcasNumbersException {
        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(new DelegateResponseCreator(withStatus(HttpStatus.UNAUTHORIZED),
                                                    withStatus(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(TWO_CERTS_JSON)));
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .hasSize(2);
    }

    @Test
    void theGetAcasCertificatesByCaseDataProducesTwoAcasCertificates() throws
        AcasException, InvalidAcasNumbersException {
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(testData.getCaseData());
        assertThat(acasCertificates).hasSize(2);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenNullRespondentCollection() throws
        AcasException, InvalidAcasNumbersException {
        testData.getCaseData().setRespondentCollection(null);
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(testData.getCaseData());
        assertThat(acasCertificates).hasSize(0);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenEmptyRespondentCollection() throws
        AcasException, InvalidAcasNumbersException {
        testData.getCaseData().setRespondentCollection(new ArrayList<>());
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(testData.getCaseData());
        assertThat(acasCertificates).hasSize(0);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenNullRespondentSumTypeItem() throws
        AcasException, InvalidAcasNumbersException {
        testData.getCaseData().getRespondentCollection().get(0).setValue(null);
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(testData.getCaseData());
        assertThat(acasCertificates).hasSize(1);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenEmptyRespondentSumTypeItem() throws
        AcasException, InvalidAcasNumbersException {
        testData.getCaseData().getRespondentCollection().get(0).getValue().setRespondentAcas("");
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(testData.getCaseData());
        assertThat(acasCertificates).hasSize(1);
    }

    public static class DelegateResponseCreator implements ResponseCreator {
        private final ResponseCreator[] delegates;
        private int toExecute;

        public DelegateResponseCreator(ResponseCreator... delegates) {
            this.delegates = Arrays.copyOf(delegates, delegates.length);
        }

        @Override
        public ClientHttpResponse createResponse(ClientHttpRequest request)
            throws IOException {
            return this.delegates[toExecute++ % delegates.length]
                .createResponse(request);
        }
    }
}
