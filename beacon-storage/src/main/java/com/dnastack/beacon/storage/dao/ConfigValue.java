package com.dnastack.beacon.storage.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Config value model
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Document
@Builder
@Getter
@ToString
class ConfigValue {

    @Id
    private final String id;

    @NonNull
    private final String name;

    @NonNull
    private final String value;

}
