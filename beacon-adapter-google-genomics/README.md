# Beacon adapter for Google Genomics API

Beacon adapter google genomics is a fork from [Beacon Adapter Variants](https://github.com/mcupak/beacon-adapter-variants) for Google Genomics API

Beacon adapter google genomics is an implementation of the [Beacon Adapter API](https://github.com/mcupak/beacon-adapter-api). This adapter wraps [GA4GH Variants API](http://ga4gh-schemas.readthedocs.io/en/latest/api/variants.html) and allows you to turn the API into a beacon via a compatible Beacon implementation, such as [JBDK](https://github.com/mcupak/beacon-java).

Prerequisites: Java 8, [GA4GH schemas v0.5.1](https://github.com/ga4gh/ga4gh-schemas/releases/tag/v0.5.1).

## Configuring the Adapter

In order to properly configure the adapter you must call the initAdapter method from the VariantsBeaconAdapter class, supplying it with an AdapterConfig object once a new adapter object has been created.
There is one required parameter for the configuration that must be supplied as ConfigValues to the AdapterConfig object:

#### Required one of the following
| Name | Value |
|--- | ---|
| "beaconJsonFile" | Path to a JSON file that describes this beacon. |
| "beaconJson" | JSON string that describes this beacon |
| "apiKey" | [Google application API Key](https://cloud.google.com/genomics/auth#APIKey) |
| "projectId" | [The Google Cloud project ID to list datasets for](https://cloud.google.com/genomics/reference/rest/v1/datasets/list) |

## Building

Build the project:

    mvn install