package micronaut.java.dynamodb.gradle.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;
import micronaut.java.dynamodb.gradle.request.UserRequest;
import micronaut.java.dynamodb.gradle.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    @NonNull
    List<User> findAll() throws JsonProcessingException;

    @NonNull
    Optional<User> findById(@NonNull @NotBlank String id) throws JsonProcessingException;

    void delete(@NonNull @NotBlank String id) throws JsonProcessingException;

    String save(UserRequest userRequest) throws JsonProcessingException;
}
