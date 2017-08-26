# Beacon adapter for Ensembl API

Beacon adapter google genomics is a fork from [Beacon Adapter Variants](https://github.com/mcupak/beacon-adapter-com.dnastack.beacon.adapter.variants.variants) for Google Genomics API

Beacon adapter google genomics is an implementation of the [Beacon Adapter API](https://github.com/mcupak/beacon-adapter-api). This adapter wraps [Ensembl API](http://grch37.rest.ensembl.org/) and allows you to turn the API into a beacon via a compatible Beacon implementation, such as [JBDK](https://github.com/mcupak/beacon-java).

Prerequisites: Java 8

## Configuring the Adapter

In order to properly configure the adapter you must call the initAdapter method from the VariantsBeaconAdapter class, supplying it with an AdapterConfig object once a new adapter object has been created.
There is one required parameter for the configuration that must be supplied as ConfigValues to the AdapterConfig object:

#### Required one of the following
| Name | Value |
|--- | ---|
| "url" | URL for database |

##Building

Build the project:

    mvn install