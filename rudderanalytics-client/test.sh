VERSION=1.0.1
curl -T ~/.m2/repository/com/rudderstack/sdk/java/rudderanalytics-client/$VERSION/rudderanalytics-client-$VERSION.pom \
  -u "$BINTRAY_USER":"$BINTRAY_PASSWORD" \
  --url "https://api.bintray.com/maven/rudderstack/rudderstack/rudderanalytics-client/;publish=1/com/rudderstack/sdk/java/rudderanalytics-client/$VERSION/rudderanalytics-client-$VERSION.pom"

curl -T ~/.m2/repository/com/rudderstack/sdk/java/rudderanalytics-client/$VERSION/rudderanalytics-client-$VERSION.jar \
  -u "$BINTRAY_USER":"$BINTRAY_PASSWORD" \
  --url "https://api.bintray.com/maven/rudderstack/rudderstack/rudderanalytics-client/;publish=1/com/rudderstack/sdk/java/rudderanalytics-client/$VERSION/rudderanalytics-client-$VERSION.jar"
