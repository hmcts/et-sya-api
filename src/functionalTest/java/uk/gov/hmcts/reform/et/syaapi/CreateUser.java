package uk.gov.hmcts.reform.et.syaapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateUser {
    private final String email;
    private final String forename;
    private final String surname;
    private final String password;
    private final List<Role> roles;
}
