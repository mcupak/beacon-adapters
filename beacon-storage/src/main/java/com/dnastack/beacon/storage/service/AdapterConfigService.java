package com.dnastack.beacon.storage.service;

import com.dnastack.beacon.storage.dao.AdapterConfig;
import com.dnastack.beacon.storage.persistance.AdapterConfigRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Service
public class AdapterConfigService {

    @Autowired
    private AdapterConfigRepository repository;

    public AdapterConfig getAdapterConfigById(@NonNull String id) {
        return repository.getAdapterConfigById(id);
    }

    public String saveAdapterConfig(@NonNull AdapterConfig adapterConfig) {
        AdapterConfig savingAdapterConfig = repository.save(adapterConfig);
        return savingAdapterConfig.getId();
    }

    public List<AdapterConfig> findAll() {
        return repository.findAll();
    }

}
