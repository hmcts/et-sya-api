package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@ExtendWith(MockitoExtension.class)
class AcasServiceTest {

    private AcasService acasService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
//        when(restTemplate.)
        acasService = new AcasService(restTemplate);
    }

    @Test
    void getAcasCertWithNull_producesNullPointerException() {
        assertThrows(NullPointerException.class, () -> acasService.getCertificates(((String) null)));
    }

    @Test
    void getAcasCertWithMultipleNulls_producesNullPointerException() {
        assertThrows(NullPointerException.class, () -> acasService.getCertificates(null, null, null));
    }

    @Test
    void getAcasCertWithInvalidAcasNumber_producesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates("A123"));
        assertThat(exception.getInvalidAcasNumbers()).containsExactly("A123");
    }

    @Test
    void getAcasCertsWithInvalidAcasNumbers_producesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class, () -> acasService.getCertificates("A123", "Z456"));
        assertThat(exception.getInvalidAcasNumbers()).containsExactly("A123", "Z456");
    }

    @Test
    void getAcasCertWithValid13CharAcasNumber_producesCertFound() throws Exception {
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates("A123456/12/12")).hasSize(1);
    }

    @Test
    void getAcasCertWithValid14CharAcasNumber_producesCertFound() throws Exception {
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates("AB123456/12/12")).hasSize(1);
    }

    @Test
    void getAcasCertsWithMultipleValidAcasNumbers_producesCertsFound() throws Exception {
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates("A123456/12/12", "AB123456/12/12")).hasSize(2);
    }

    @Test
    void getAcasCertWithValid13CharAcasNumber_producesCertNotFound() throws Exception {
        // Valid: A123456/12/12
        assertThat(acasService.getCertificates("A123456/12/12")).isEmpty();
    }

    @Test
    void getAcasCertWithValid14CharAcasNumber_producesCertNotFound() throws Exception {
        // Valid: AB123456/12/12
        assertThat(acasService.getCertificates("AB123456/12/12")).isEmpty();
    }

    @Test
    void getAcasCertsWithMultipleValidAcasNumbers_producesCertsNotFound() throws Exception {
        // Valid: A123456/12/12, AB123456/12/12
        assertThat(acasService.getCertificates("A123456/12/12", "AB123456/12/12")).isEmpty();
    }

    @Test
    void getAcasCertsWithMultipleValidAcasNumbersWithOneEmpty_producesOneCertFound() throws Exception {
        // Valid: A123456/12/12
        // Inalid: ZZ123456/12/12
        assertThat(acasService.getCertificates("A123456/12/12", "ZZ123456/12/12")).hasSize(1);
    }

    @Test
    void getAcasCertWithLessThanMinLengthAcasNumber_producesInvalidAcasNumbersException() {
        // Min: 13 (including two fwd slashes)
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("A123456/12/1")
        );
        assertThat(exception.getInvalidAcasNumbers()).containsExactly("A123456/12/1");
    }

    @Test
    void getAcasCertWithMoreThanMaxLengthAcasNumber_producesInvalidAcasNumbersException() {
        // Max: 14 (including two fwd slashes)
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("AB123456/12/123")
        );
        assertThat(exception.getInvalidAcasNumbers()).containsExactly("AB123456/12/123");
    }

    @Test
    void getAcasCertsWithOneValidAndTwoInvalidAcasNumbers_producesInvalidAcasNumbersException() {
        InvalidAcasNumbersException exception = assertThrows(
            InvalidAcasNumbersException.class,
            () -> acasService.getCertificates("R123", "AB123456/12/12", "R456")
        );
        assertThat(exception.getInvalidAcasNumbers()).containsExactly("R123", "R456");
    }

    // todo remove this test before story completion
    @Test
    void getAcasCertWithOneValidAcasNumber_producesCertFound() throws InvalidAcasNumbersException {
        new AcasService(new RestTemplate()).getCertificates("R444444/89/74", "R465745/34/08");
    }
}
