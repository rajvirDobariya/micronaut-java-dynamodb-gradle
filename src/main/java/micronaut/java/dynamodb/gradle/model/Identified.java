package micronaut.java.dynamodb.gradle.model;

import io.micronaut.core.annotation.NonNull;

public interface Identified {

    String getId();

    void setId(String id);

}