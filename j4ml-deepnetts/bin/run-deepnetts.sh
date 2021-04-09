SCRIPT_HOME=`dirname $0`
java -cp $SCRIPT_HOME/../target/j4ml-deepnetts-0.9-SNAPSHOT-jar-with-dependencies.jar j4ml.deepnetts.network.DeepNettsInterface $*

