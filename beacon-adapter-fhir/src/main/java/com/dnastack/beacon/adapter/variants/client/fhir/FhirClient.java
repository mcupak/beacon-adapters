package com.dnastack.beacon.adapter.variants.client.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Sequence;
import org.hl7.fhir.dstu3.model.Sequence.SequenceVariantComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
public class FhirClient {

    private IGenericClient client;

    public FhirClient(String url) {
        FhirContext fhirContext = FhirContext.forDstu3();

        client = fhirContext.newRestfulGenericClient(url);
    }

    public List<SequenceVariantComponent> getVariants(Long start, Long end, String refAllele, String obsAllele) {
        Bundle results = client
                .search()
                .forResource(Sequence.class)
                .returnBundle(Bundle.class)
                .execute();

        List<SequenceVariantComponent> variants = new ArrayList<>();

        for (Bundle.BundleEntryComponent bundleEntryComponent : results.getEntry()) {
            Sequence sequence = (Sequence) bundleEntryComponent.getResource();

            variants.addAll(sequence.getVariant().stream()
                    .filter(sequenceVariantComponent -> filterVariants(start, end, refAllele, obsAllele, sequenceVariantComponent))
                    .collect(Collectors.toList()));
        }

        return variants;

    }

    private boolean filterVariants(Long start, Long end, String refAllele, String obsAllele, SequenceVariantComponent sequenceVariantComponent) {
        return sequenceVariantComponent.getStart() == start
                && sequenceVariantComponent.getEnd() == end
                && sequenceVariantComponent.getReferenceAllele().equalsIgnoreCase(refAllele)
                && sequenceVariantComponent.getObservedAllele().equalsIgnoreCase(obsAllele);
    }

}
