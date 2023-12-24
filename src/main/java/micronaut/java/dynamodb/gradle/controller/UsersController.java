package micronaut.java.dynamodb.gradle.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import micronaut.java.dynamodb.gradle.request.UserRequest;
import micronaut.java.dynamodb.gradle.model.User;
import micronaut.java.dynamodb.gradle.repo.UserRepository;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/users")
public class UsersController {

    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Get
    public List<User> index() throws JsonProcessingException {
        return userRepository.findAll();
    }

    @Post
    public HttpResponse<?> save(@Body UserRequest userRequest) throws JsonProcessingException {
        System.out.println("user add");
        String id = userRepository.save(userRequest);
        return HttpResponse.created(UriBuilder.of("/users").path(id).build());
    }

    @Get("/{id}")
    public Optional<User> show(@PathVariable @NonNull @NotBlank String id) throws JsonProcessingException {
        return userRepository.findById(id);
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NonNull @NotBlank String id) throws JsonProcessingException {
        userRepository.delete(id);
    }
}
