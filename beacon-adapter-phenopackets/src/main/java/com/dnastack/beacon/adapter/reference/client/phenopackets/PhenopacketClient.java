package com.dnastack.beacon.adapter.reference.client.phenopackets;

import org.phenopackets.api.PhenoPacket;
import org.phenopackets.api.model.entity.Variant;

import java.util.List;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
public class PhenopacketClient {

    private PhenoPacket phenoPacket;

    public PhenopacketClient(PhenoPacket phenoPacket) {
        this.phenoPacket = phenoPacket;
    }

    public List<Variant> getVariants() {
        return phenoPacket.getVariants();
    }

}
