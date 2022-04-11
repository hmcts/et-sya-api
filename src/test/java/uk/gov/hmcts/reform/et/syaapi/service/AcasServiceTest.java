package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
class AcasServiceTest {

    private static final String ACAS_DEV_API_URL = "https://api-dev-acas-01.azure-api.net/ECCLDev";
    private static final String ACAS_API_KEY = "380e7fad52b2403abf42575ca8fba6e2";
    private static final String NO_CERTS_JSON = "[]";
    private static final String ONE_CERT_JSON =
        "[{\"CertificateNumber\":\"A123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"}]";
    private static final String TWO_CERTS_JSON =
        "[{\"CertificateNumber\":\"AB123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"},"
            + "{\"CertificateNumber\":\"A123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW...\"}]";
    public static final String A123 = "A123";
    public static final String Z456 = "Z456";
    public static final String A123456_12_12 = "A123456/12/12";
    public static final String AB123456_12_12 = "AB123456/12/12";
    private AcasService acasService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        acasService = new AcasService(restTemplate, ACAS_DEV_API_URL, ACAS_API_KEY);
        mockServer = MockRestServiceServer.createServer(restTemplate);
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
            InvalidAcasNumbersException.class, () -> acasService.getCertificates(A123456_12_12, null));
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

    @Test
    void theGetAcasCertsWithInvalidAcasNumbersProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates(A123, Z456));
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly(A123, Z456);
        assertThat(exception.getMessage())
            .isBlank();
    }

    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(A123456_12_12))
            .hasSize(1);
    }

    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(AB123456_12_12))
            .hasSize(1);
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(TWO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(A123456_12_12, AB123456_12_12))
            .hasSize(2);
    }

    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertNotFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(A123456_12_12))
            .isEmpty();
    }

    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertNotFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(AB123456_12_12))
            .isEmpty();
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsNotFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(A123456_12_12, AB123456_12_12))
            .isEmpty();
    }

    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersWithOneEmptyProducesOneCertFound()
        throws AcasException, InvalidAcasNumbersException {
        mockServer.expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: A123456/12/12
        // Inalid: ZZ123456/12/12
        assertThat(acasService.getCertificates(A123456_12_12, "ZZ123456/12/12"))
            .hasSize(1);
    }

    @Test
    void theGetAcasCertWithLessThanMinLengthAcasNumberProducesInvalidAcasNumbersException() {
        // Min: 13 (including two fwd slashes)
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("A123456/12/1")
        );
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly("A123456/12/1");
    }

    @Test
    void theGetAcasCertWithMoreThanMaxLengthAcasNumberProducesInvalidAcasNumbersException() {
        // Max: 14 (including two fwd slashes)
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("AB123456/12/123")
        );
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly("AB123456/12/123");
    }

    @Test
    void theGetAcasCertsWithOneValidAndTwoInvalidAcasNumbersProducesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("R123", AB123456_12_12, "R456")
        );
        assertThat(exception.getInvalidAcasNumbers())
            .containsExactly("R123", "R456");
    }

    @Test
    void theGetAcasCertWithWrongUrlProducesAcasException() {
        mockServer.expect(ExpectedCount.manyTimes(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(AcasException.class, () -> acasService.getCertificates(A123456_12_12));
    }

    @Test
    void theGetAcasCertWithBadApiKeyProducesAcasException() {
        mockServer.expect(ExpectedCount.manyTimes(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        assertThrows(AcasException.class, () -> acasService.getCertificates(A123456_12_12));
    }
}
