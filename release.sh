# curl -T ~/.m2/repository/com/rudderstack/sdk/java/sdk/1.0.0/sdk-1.0.0.pom \
#   -u $BINTRAY_USER:$BINTRAY_PASSWORD \
#   https://api.bintray.com/content/rudderstack/rudderstack/sdk/1.0.0/sdk-1.0.0.pom
# curl -T ~/.m2/repository/com/rudderstack/sdk/java/sdk/1.0.0/sdk-1.0.0.pom \
#   -u $BINTRAY_USER:$BINTRAY_PASSWORD \
#   -H "X-Bintray-Package:sdk" \
#   -H "X-Bintray-Version:1.0.0" \
#   https://api.bintray.com/content/rudderstack/rudderstack/com/rudderstack/sdk/java/1.0.0/sdk-1.0.0.pom

PACKAGE_NAME=sdk
VERSION_NAME=1.0.0


# curl -T ~/.m2/repository/com/rudderstack/sdk/java/sdk/$VERSION_NAME/sdk-$VERSION_NAME.pom \
#   -u $BINTRAY_USER:$BINTRAY_PASSWORD \
#   https://api.bintray.com/content/rudderstack/rudderstack/$PACKAGE_NAME/$VERSION_NAME/com/rudderstack/sdk/java/$VERSION_NAME/sdk-$VERSION_NAME.pom


# curl -T ~/.m2/repository/com/rudderstack/sdk/java/sdk-core/$VERSION_NAME/sdk-core-$VERSION_NAME.pom \
#   -u $BINTRAY_USER:$BINTRAY_PASSWORD \
#   https://api.bintray.com/content/rudderstack/rudderstack/$PACKAGE_NAME/$VERSION_NAME/com/rudderstack/sdk/java/sdk-core/$VERSION_NAME/sdk-core-$VERSION_NAME.pom

# curl -T ~/.m2/repository/com/rudderstack/sdk/java/sdk-core/$VERSION_NAME/sdk-core-$VERSION_NAME.jar \
#   -u $BINTRAY_USER:$BINTRAY_PASSWORD \
#   https://api.bintray.com/content/rudderstack/rudderstack/$PACKAGE_NAME/$VERSION_NAME/com/rudderstack/sdk/java/sdk-core/$VERSION_NAME/sdk-core-$VERSION_NAME.jar

curl -T ~/.m2/repository/com/rudderstack/sdk/java/rudderanalytics/1.0.0/rudderanalytics-1.0.0.pom \
  -u $BINTRAY_USER:$BINTRAY_PASSWORD \
  https://api.bintray.com/maven/rudderstack/rudderstack/sdk/;publish=1/com/rudderstack/sdk/java/rudderanalytics/1.0.0/rudderanalytics-1.0.0.pom
