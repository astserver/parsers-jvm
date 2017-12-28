# This script does a full build of the AST server and compares input and output

# Fail if any command fails in the script
echo "Starting integration test..."
set -e

# Print the output of commands as they run, including variables
set -x

# Build a fat jar file that contains application and all dependencies
./gradlew shadowJar
FAT_JAR_FILE=build/libs/astserver-jvm-all.jar 

# Run the server with a test input file
INPUT_FILE=src/test/resources/integration-test-data/test_input.txt
EXPECTED_OUTPUT_FILE=src/test/resources/integration-test-data/expected_output.txt
ACTUAL_OUTPUT_FILE=build/tmp/test_output.txt
mkdir -p build/tmp
cat $INPUT_FILE | java -cp "" -jar $FAT_JAR_FILE > $ACTUAL_OUTPUT_FILE

# Check that the actual output matches the expected output
diff $EXPECTED_OUTPUT_FILE $ACTUAL_OUTPUT_FILE
echo "Expected and actual outputs match."
