package micronaut.java.dynamodb.gradle.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.NonNull;
import jakarta.validation.constraints.NotBlank;
import micronaut.java.dynamodb.gradle.model.Book;
import micronaut.java.dynamodb.gradle.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepositery {

    @NonNull
    List<Book> findAll() throws JsonProcessingException;

    @NonNull
    Optional<Book> findById(@NonNull @NotBlank String id) throws JsonProcessingException;

    void delete(@NonNull @NotBlank String id) throws JsonProcessingException;

    @NonNull
    String save(Employee employee) throws JsonProcessingException;

}
