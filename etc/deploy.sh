rm cloud-http-java-sample.zip
cd ..
zip -r etc/cloud-http-java-sample.zip pom.xml src -x *.DS_*
gcloud functions deploy  cloud-http-java-sample-deploytest --region=europe-west1 --ingress-settings=all --memory=256MB   --entry-point=com.bere.cloud.functions.httpsample --runtime=java11 --trigger-http --allow-unauthenticated --project=mlff-sb --max-instances=1000 --min-instances=10
cd etc
