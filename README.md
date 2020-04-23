# Krypton Java Client

This repository is the core client library used to connect to [Krypton Authentification server](https://github.com/krypton-org/krypton-auth).
It is based on the [Krypton client specification](https://github.com/krypton-org/krypton-drafts/tree/master/client).

## Getting started

### Installation

TODO: This library requires Java 11 and higher?

#### Maven

TODO: add maven dependency commands

#### Gradle

TODO: add gradle dependency commands

### Usage

#### Basic example

Considering you have an instance of Krypton Auth server running on "http://localhost:5000/auth":
````java
import com.krypton.core.KryptonClient;

KryptonClient client = new KryptonClient("http://localhost:5000/auth");
client.register("toto@toto.com", "totototo");

//TODO: continue and comment this example
````

#### Advanced example

TODO

## API

List of available methods and classes. Probably generate JavaDoc and host it somewhere.
Write the link here.

## Contributing

### Contributing policy

TODO

### Development setup

Clone the repository and `cd` to the directory. 
To build the project, run:

`./gradlew build`

The first time you do so, Gradle should download all the dependencies on your computer.
It will compile the project and run the unit tests.

The project skeleton has been built following 
[the official Gradle documentation to build librairies](https://guides.gradle.org/building-java-libraries/).

Later, we need to follow [this guide](https://docs.gradle.org/5.0/userguide/publishing_overview.html#publishing_overview) to generate the right packages for Gradle and Maven.

Need to add a new dependency? Add it in [build.gradle](build.gradle).

## License and copyright

This project is licensed under the [MIT License](LICENSE).
No copyright assignment is necessary to contribute, so copyrights are shared among contributors.