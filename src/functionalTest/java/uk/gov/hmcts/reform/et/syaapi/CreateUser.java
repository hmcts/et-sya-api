package uk.gov.hmcts.reform.et.syaapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUser {
    private final String email;
    private final String forename;
    private final String surname;
    private final String password;
    private final List<Role> roles;

    public CreateUser(String email, String forename, String surname, String password, List<Role> roles) {
        this.email = email;
        this.forename = forename;
        this.surname = surname;
        this.password = password;
        this.roles = roles;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("forename")
    public String getForename() {
        return forename;
    }

    @JsonProperty("surname")
    public String getSurname() {
        return surname;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("roles")
    public List<Role> getRoles() {
        return roles;
    }

}
