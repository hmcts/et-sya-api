package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class VerifyTokenServiceTest {

    @InjectMocks
    private VerifyTokenService verifyTokenService;

    @Test
    void verifyTokenSignature() {
        assertFalse(verifyTokenService.verifyTokenSignature(
            "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJ"
                + "TMjU2In0.eyJzdWIiOiJzc2NzLWNpdGl6ZW40QGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6"
                + "Ijc1YzEyMTk3LWFjYmYtNDg2Zi1iNDI5LTJlYWEwZjMyNWVkMCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5h"
                + "bS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV"
                + "0aEdyYW50SWQiOiIwMGZhYThiNy03OWY5LTRiZWQtODI1OS0zZDE0MDEzOGYzZjIiLCJhdWQiOiJzc2NzIiwibmJmIjox"
                + "NTc4NTAwNDU0LCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSI"
                + "sInJvbGVzIl0sImF1dGhfdGltZSI6MTU3ODUwMDQ1MTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1Nzg1MjkyNTQsIml"
                + "hdCI6MTU3ODUwMDQ1NCwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6ImNkMTgxODM3LTdlMmUtNDY1Ny05ZTgwLTk4NWE3Zj"
                + "VmZDMzYiJ9.SZOd981fC1bdMWehXKsUl0B9vEXRr7-NBKl6IaFIoS573rNjKgcIzChMaxcmc-anOxJqgF8Lan7RdMCIb4Y-"
                + "zGG3TzfGAG7elpmXJVsogPKCWJlGFCJm_wU-h_cqAcL2llgqnNkkms43lgvyfIdiXv3J-00qBHzMy3jG5mLOE5YZet1LKf3Ii"
                + "RNZxI5Vx6L2Afdox1jiKGQGGt2bNx7-rcYS8VVVZI-ovo7lbbWU6Mi5lWI19q2AS9jGcK5U4hcIU06JzoWGsh-Ob1xkq7VtJ"
                + "KyrOSiUth-SjY5PqQzjvpuEO8MrLWTI0sCaWRHbmbF0bHICGO17bQ42_PfTHgza4A"));
    }
}
