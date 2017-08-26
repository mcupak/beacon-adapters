# Beacon adapter for FHIR

Beacon adapter fhir is an implementation of the [Beacon Adapter API](https://github.com/mcupak/beacon-adapter-api).

[Fhir Genomics Implementer Guidance](https://www.hl7.org/fhir/2017Jan/genomics.html)

Prerequisites: Java 8, [GA4GH schemas](https://github.com/ga4gh/ga4gh-schemas/releases/tag/v0.6.0a10).

## Configuring the Adapter

In order to properly configure the adapter you must call the initAdapter method from the VariantsBeaconAdapter class, supplying it with an AdapterConfig object once a new adapter object has been created.
There is one required parameter for the configuration that must be supplied as ConfigValues to the AdapterConfig object:

#### Required one of the following
| Name | Value |
|--- | ---|
| "beaconJsonFile" | Path to a JSON file that describes this beacon. |
| "beaconJson" | JSON string that describes this beacon |
| "url" | URL for database |

##Building

Build the project:

    mvn install