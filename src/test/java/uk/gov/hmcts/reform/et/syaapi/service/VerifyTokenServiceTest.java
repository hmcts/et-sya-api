package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class VerifyTokenServiceTest {


    @InjectMocks
    private VerifyTokenService verifyTokenService;

    @BeforeEach
    public void setUp() {
        verifyTokenService = new VerifyTokenService();
        ReflectionTestUtils.setField(verifyTokenService, "idamJwkUrl", "http://localhost:5555/o/jwks");
    }

    @Test
    @SneakyThrows
    void verifyTokenSignature() {
        assertFalse(verifyTokenService.verifyTokenSignature(
            "Bearer eyJraWQiOiIyMzQ1Njc4OSIsImFsZyI6IlJTMjU2In0."
                + "eyJzdWIiOiJDQ0RfU3R1YiIsImlzcyI6Imh0dHA6XC9cL2ZyLWFtOjgwODBcL29wZW5hbVwvb2F1dGgyXC9obWN0cyIsIn"
                + "Rva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsImV4cCI6MTY0NjY1NzUwNSwiaWF0IjoxNjQ2NjQzMTA1fQ.IFvpJxJl9Qq6dBbN"
                + "_QazfbbAFztNPEtPa_5qVhPJcTlX2SDPkhYz4EGH4196_YbHmGghLQQpn1lscCb2hq27c3xXrVkYx-h6Dm0RXJXaz8Fktiw4a"
                + "BkZ4ZvDxSD_3Q1Hj534Qe-XoS5WelXh4xGD2ay4DrPqtlE4BKwfBGTA_Dbpu6Iree7S7e149zzUaXYbJGoyBv9x_j4Zy2advD9"
                + "FK52R1CXVCHIa-aPmW7vCkucLxKMr8ktyd5NFnAgNo-XqXMmnAmYp-MYf_-6SHaPLsDsHqO18F68-VA2Rj2WG3S58u6XwvRYJ3"
                + "28R9yqHZVo_fojRY9pRhFzlVr1Yy9FYOg"));
    }
}
