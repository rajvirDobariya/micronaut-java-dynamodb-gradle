package micronaut.java.dynamodb.gradle.model;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class User implements Identified {

    private String id;

    @NonNull
    @NotBlank
    private String email;

    @NonNull
    @NotBlank
    private String password;

    private String firstName;
    private String lastName;
    private String countryCode;
    private String phoneNo;

    public User(String id, String email, String password, String firstName, String lastName, String countryCode, String phoneNo) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.countryCode = countryCode;
        this.phoneNo = phoneNo;
    }

    public User() {

    }

    // Getters and setters for firstName, lastName, countryCode, and phoneNo
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
// Other existing methods (getId, setId, getEmail, getPassword, setEmail, setPassword) remain unchanged
}
