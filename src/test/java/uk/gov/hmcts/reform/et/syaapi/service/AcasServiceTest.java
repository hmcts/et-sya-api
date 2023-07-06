package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SuppressWarnings({"PMD.TooManyMethods"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AcasServiceTest {

    private static final String ACAS_DEV_API_URL = "https://api-dev-acas-01.azure-api.net/ECCLDev";
    private static final String ACAS_API_KEY = "dummyApiKey";
    private static final String NO_CERTS_JSON = "[]";

    private static final String CERT_JSON_OBJECT = "{\"CertificateNumber\":\"A123456/12/12\","
        + "\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"}";
    private static final String ONE_CERT_JSON = "[" + CERT_JSON_OBJECT + "]";
    private static final String TWO_CERTS_JSON = "["
        + String.join(", ", CERT_JSON_OBJECT, CERT_JSON_OBJECT) + "]";
    private static final String THREE_CERTS_JSON = "["
        + String.join(", ", CERT_JSON_OBJECT, CERT_JSON_OBJECT, CERT_JSON_OBJECT) + "]";
    private static final String FOUR_CERTS_JSON = "["
        + String.join(", ", CERT_JSON_OBJECT, CERT_JSON_OBJECT, CERT_JSON_OBJECT, CERT_JSON_OBJECT) + "]";
    private static final String FIVE_CERTS_JSON = "["
        + String.join(", ", CERT_JSON_OBJECT, CERT_JSON_OBJECT, CERT_JSON_OBJECT, CERT_JSON_OBJECT,
                      CERT_JSON_OBJECT) + "]";
    private static final String A123 = "A123";
    private static final String Z456 = "Z456";
    private static final String R123456_11_12 = "R123456/11/12";
    private static final String R123456_13_14 = "R123456/13/14";
    private static final String R600227_21_75 = "R600227/21/75";
    private static final String R600227_21_76 = "R600227/21/76";
    private static final String R600227_21_77 = "R600227/21/77";
    public static final String DUMMY_ACAS_NUMBER = "dummy acas number";
    private AcasService acasService;
    private RestTemplate restTemplate;
    private CaseTestData caseTestData;

    @BeforeEach
    void setup() {
        caseTestData = new CaseTestData();
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

    @SneakyThrows
    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12))
            .hasSize(1);
    }

    @SneakyThrows
    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ONE_CERT_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(R123456_13_14))
            .hasSize(1);
    }

    @SneakyThrows
    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(TWO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .hasSize(2);
    }

    @SneakyThrows
    @Test
    void theGetAcasCertWithValid13CharAcasNumberProducesCertNotFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12))
            .isEmpty();
    }

    @SneakyThrows
    @Test
    void theGetAcasCertWithValid14CharAcasNumberProducesCertNotFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates(R123456_13_14))
            .isEmpty();
    }

    @SneakyThrows
    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersProducesCertsNotFound() {
        getMockServer().expect(ExpectedCount.once(), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(NO_CERTS_JSON));
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .isEmpty();
    }

    @SneakyThrows
    @Test
    void theGetAcasCertsWithMultipleValidAcasNumbersWithOneEmptyProducesOneCertFound() {
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

    @SneakyThrows
    @Test
    void theGetAcasCertWithBadApiKeyFirstTimeProducesCertificates() {
        JSONObject expectedBody = new JSONObject();
        expectedBody.put("certificateNumbers", List.of(R123456_11_12, R123456_13_14));
        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(r -> {
                String requestBody = ((ByteArrayOutputStream) r.getBody()).toString(StandardCharsets.UTF_8);
                assertThat(requestBody).isEqualTo(expectedBody.toString());
            })
            .andRespond(new DelegateResponseCreator(withStatus(HttpStatus.UNAUTHORIZED),
                                                    withStatus(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(TWO_CERTS_JSON)));
        assertThat(acasService.getCertificates(R123456_11_12, R123456_13_14))
            .hasSize(2);
    }

    @Test
    void theGetAcasCertificatesByCaseDataProducesTwoAcasCertificates() {
        JSONObject expectedBody = new JSONObject();
        expectedBody.put("certificateNumbers", List.of(R600227_21_75, R600227_21_76,
                                                       R600227_21_77, R600227_21_77, R600227_21_77));

        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(r -> {
                String requestBody = ((ByteArrayOutputStream) r.getBody()).toString(StandardCharsets.UTF_8);
                assertThat(requestBody).isEqualTo(expectedBody.toString());
            })
            .andRespond(new DelegateResponseCreator(withStatus(HttpStatus.UNAUTHORIZED),
                                                    withStatus(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(FIVE_CERTS_JSON)));
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
        assertThat(acasCertificates).hasSize(5);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenNullRespondentCollection() {
        caseTestData.getCaseData().setRespondentCollection(null);
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
        assertThat(acasCertificates).isEmpty();
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenEmptyRespondentCollection() {
        caseTestData.getCaseData().setRespondentCollection(new ArrayList<>());
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
        assertThat(acasCertificates).isEmpty();
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenNullRespondentSumTypeItem() {
        caseTestData.getCaseData().getRespondentCollection().get(0).setValue(null);
        caseTestData.getCaseData().getRespondentCollection().get(1).setValue(null);
        JSONObject expectedBody = new JSONObject();
        expectedBody.put("certificateNumbers", List.of(R600227_21_77, R600227_21_77, R600227_21_77));

        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(r -> {
                String requestBody = ((ByteArrayOutputStream) r.getBody()).toString(StandardCharsets.UTF_8);
                assertThat(requestBody).isEqualTo(expectedBody.toString());
            })
            .andRespond(new DelegateResponseCreator(withStatus(HttpStatus.UNAUTHORIZED),
                                                    withStatus(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(THREE_CERTS_JSON)));
        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());

        assertThat(acasCertificates).hasSize(3);
    }

    @Test
    void theGetAcasCertificatesByCaseDataDoesNotProduceAcasCertificatesWhenEmptyRespondentSumTypeItem() {
        caseTestData.getCaseData().getRespondentCollection().get(0).getValue().setRespondentAcas("");
        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(new DelegateResponseCreator(withStatus(HttpStatus.UNAUTHORIZED),
                                                    withStatus(HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(FOUR_CERTS_JSON)));

        List<AcasCertificate> acasCertificates = acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
        assertThat(acasCertificates).hasSize(4);
    }

    @Test
    void theGetAcasCertificatesByCaseDataThrowsAcasException() {
        RestTemplate tmpRestTemplate = Mockito.mock(RestTemplate.class);
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            when(tmpRestTemplate
                     .exchange(anyString(), eq(HttpMethod.POST), any(), eq(new ParameterizedTypeReference<>() {})))
                .thenThrow(new RestClientException("Test rest client exception"));
            acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(), anyString(), anyString(), anyString(), anyString()),
                times(1)
            );
        }
    }

    @Test
    void theGetAcasCertificatesByCaseDataThrowsInvalidAcasNumbersException() {
        caseTestData.getCaseData().getRespondentCollection().get(0).getValue().setRespondentAcas(DUMMY_ACAS_NUMBER);
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData());
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(), anyString(), anyString(), anyString(), anyString()),
                times(1)
            );
        }
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
