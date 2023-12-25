package micronaut.java.dynamodb.gradle.model;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

@Serdeable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Department {

    private String id;
    private String name;

}
