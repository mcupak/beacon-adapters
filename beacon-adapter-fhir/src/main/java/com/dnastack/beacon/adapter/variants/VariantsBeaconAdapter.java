package com.dnastack.beacon.adapter.variants;

import com.dnastack.beacon.adapter.api.BeaconAdapter;
import com.dnastack.beacon.exceptions.BeaconException;
import com.dnastack.beacon.utils.AdapterConfig;
import org.ga4gh.beacon.Beacon;
import org.ga4gh.beacon.BeaconAlleleRequest;
import org.ga4gh.beacon.BeaconAlleleResponse;

import javax.enterprise.context.Dependent;
import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
@Dependent
public class VariantsBeaconAdapter implements BeaconAdapter {

    @Override
    public void initAdapter(AdapterConfig adapterConfig) {

    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(BeaconAlleleRequest beaconAlleleRequest) throws BeaconException {
        return null;
    }

    @Override
    public BeaconAlleleResponse getBeaconAlleleResponse(String referenceName, Long start, String referenceBases,
                                                        String alternateBases, String assemblyId, List<String> datasetIds,
                                                        Boolean includeDatasetResponses) throws BeaconException {
        return null;
    }

    @Override
    public Beacon getBeacon() throws BeaconException {
        return null;
    }

}
