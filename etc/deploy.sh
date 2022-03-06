rm cloud-http-java-sample.zip
zip -r -X  cloud-http-java-sample.zip ../src ../pom.xml  -x "*.DS_Store"

cd ..
gcloud functions deploy  cloud-http-java-sample-deploytest --region=europe-west1 --ingress-settings=all --memory=256MB   --entry-point=com.bere.cloud.functions.httpsample --runtime=java11 --trigger-http --allow-unauthenticated
cd etc
