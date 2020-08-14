# j4ml
Java machine learning tools and libraries.

# Building the Library

To build the library clone the repository and compile it with maven.

> git clone https://github.com/gavalian/j4ml.git
> mvn

# Running examples

Example codes are located in directory j4ml/examples, to see examples
classes use:

> jar -tf target/core-0.9-SNAPSHOT.jar | grep examples

To run one of the examples use:

>java -cp target/core-0.9-SNAPSHOT-jar-with-dependencies.jar j4ml.examples.DataClassification




