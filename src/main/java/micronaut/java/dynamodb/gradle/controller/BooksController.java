package micronaut.java.dynamodb.gradle.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import jakarta.validation.constraints.NotBlank;
import micronaut.java.dynamodb.gradle.model.Book;
import micronaut.java.dynamodb.gradle.repo.BookRepository;

import java.util.List;
import java.util.Optional;

@ExecuteOn(TaskExecutors.BLOCKING)
@Controller("/books")
public class BooksController {

    private final BookRepository bookRepository;

    public BooksController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Get
    public List<Book> index() throws JsonProcessingException {
        return bookRepository.findAll();
    }

    @Post
    public HttpResponse<?> save(@Body("isbn") @NonNull @NotBlank String isbn,
                                @Body("name") @NonNull @NotBlank String name) throws JsonProcessingException {
        String id = bookRepository.save(isbn, name);
        return HttpResponse.created(UriBuilder.of("/books").path(id).build());
    }

    @Get("/{id}")
    public Optional<Book> show(@PathVariable @NonNull @NotBlank String id) throws JsonProcessingException {
        return bookRepository.findById(id);
    }

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NonNull @NotBlank String id) throws JsonProcessingException {
        bookRepository.delete(id);
    }
}