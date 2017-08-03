package com.dnastack.beacon.storage.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter config model
 *
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Document
@Builder
@Getter
@ToString
public class AdapterConfig {

    @Id
    private final String id;

    @NonNull
    private final String name;

    @NonNull
    private final String adapterClass;

    @NonNull
    @Builder.Default
    private List<ConfigValue> configValues = new ArrayList<>();

}
