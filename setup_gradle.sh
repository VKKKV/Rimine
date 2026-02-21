#!/bin/bash
set -e

echo "Cleaning up previous failed Gradle attempts..."
rm -rf .gradle gradle gradlew gradlew.bat

echo "Creating Gradle wrapper directory structure..."
mkdir -p gradle/wrapper

echo "Generating gradle-wrapper.properties..."
cat <<EOF > gradle/wrapper/gradle-wrapper.properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.8-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

echo "Downloading standard Gradle 8.8 wrapper scripts..."
# We use the official Gradle GitHub repository to fetch the wrapper scripts
curl -sLo gradlew https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradlew
curl -sLo gradlew.bat https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradlew.bat
curl -sLo gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v8.8.0/gradle/wrapper/gradle-wrapper.jar

echo "Setting executable permissions on gradlew..."
chmod +x gradlew

echo "------------------------------------------------"
echo "Setup complete! You can now build the project using:"
echo "./gradlew build"
echo "------------------------------------------------"
