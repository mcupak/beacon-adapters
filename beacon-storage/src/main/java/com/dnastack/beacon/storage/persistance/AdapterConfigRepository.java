package com.dnastack.beacon.storage.persistance;

import com.dnastack.beacon.storage.dao.AdapterConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Repository
public interface AdapterConfigRepository extends MongoRepository<AdapterConfig, String> {

    AdapterConfig getAdapterConfigById(String id);

}
