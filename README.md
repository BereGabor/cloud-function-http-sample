# cloud-function-http-sample
HTTP Cloud function sample. The sample get an json and try to send to pub/sub, store in FireStore

#1. This Google Cloud Function with HTTP Trigger

You will find sample in this java code for:
- Java based Cloud Function with HTTP trigger
- Deploy script with gcloud cli
- Cloud logging config. Logs will sent to cloud logger directly!
- Publish message to a pub/sub topic
- Store message / document in FireStore database / collection
- Read Entity / Object from FireStore collection

 
#2. Sample logic
This HTTP sample wait a json in the request body, as MailRequest model.

MailRequest model is extends Mail what extends MailTemplate.

If the templateId is filled in request:
- Try to load MailTemplate from FireStore
	- If MailTemplate found in firestore it will used to prepare Mail structure.
	- If MailTemplate not found we will store the MailRequest as MailTemplate and this will be used to prepare Mail structure! The next call will find the tamplate in FireStore collection.

- Prepare Mail based on MailTemplate
	- If params object exists in MailRequest, the mail body will evaluate as velicity template and params fields will used for it.


After the Mail object prepared we can send to topic. 

The pub/sub topic is wait a Mail structured json!

On this topic is an python based cloud function, will send the mail exactly. 


#2. Samples

The samples json-s is in etc folder!

Send mail without template handling:

```json
{
        "to":"sample@gmail.com",
        "subject":"Test mail subject",
        "body":"Hello Gábor!\n \n Have a nice day! \n\n Best regards\nBere"
}
```

Send mail with template, and save tamplate if not exists.

```json
{
        "templateId":"simple-template",
        "from":"noreply@test.tk",
        "to":"bere.gabor@gmail.com",
        "subject":"Test mail subject",
        "body":"Hello Gábor!\n \n Have a nice day! \n\n Best regards\nBere"
}
```

Send mail used a template saved before.

```json
{
    "templateId":"simple-template",
    "to":"sampler_to@gmail.com",
    "params":{
        "name":"Collaegue",
        "sender_name":"Gabriel"
    }

}
```
