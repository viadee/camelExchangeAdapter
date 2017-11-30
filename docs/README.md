## Camel Component: Microsoft Exchange

This Camel Component allows access to the tasks of a single user stored in a Microsoft Exchange Server. The [Exchange Web Service (EWS) Java API](https://github.com/OfficeDev/ews-java-api/) is used. 
The tasks may be received from the server or write back with updated status. The tasks are returned in JSON Format.
The Component is a producer component only, i.e. it is not possible to use it in an ".from" statement!

This is an instructional piece of software meant as a proof-of-concept (with limited maintenance). 
Feel free to use it or extend it. We are looking forward to your feedback.

### Installation
Maven users need to add the following dependency to their `pom.xml` for this component:

```
<dependency>
    <groupId>de.viadee</groupId>
    <artifactId>ms-exchange</artifactId>
    <version>...</version>
</dependency>
```

### Insert user credentials
The user credentials are externalized to file `userCredentials.properties`, which needs to be created. The following shows an example. 

````
#EWS
ews.username = JohnDoe
ews.password = verySecretpassword
ews.name = John Doe
ews.url = https://foo.bar/EWS/Exchange.asmx
ews.mail = JohnDoe@example.de
````

The information are inserted by the `PropertiesComponent` as seen in `src/test/java/de/viadee/msExchange/TaskGetterProducerTest.java`and `src/test/java/de/viadee/msExchange/TaskUpdateProducerTest.java`

### Insert URL for OWA

To get the URL of the task in OWA, you need to add the URL in `/src/main/java/de/viadee/msExchange/TaskGetterProducer.java` in method `createURL(String id)`. You can find this when opening OWA in your Browser and clicking on one task. It opens a new tab. Copy the URL till `IPM.Task`.
Example:

```
      StringBuilder sb = new StringBuilder();
        sb.append("https://insert-URL.com/owa/?ae=Item&a=Open&t=IPM.Task"); 
        sb.append("id=").append(owaId);
```

### Modify Exchange Server Version

In class `/src/main/java/de/viadee/msExchange/ExchangeProducer.java`, Method `public static ExchangeService connectViadirectURL(String url_str, String user, String password)` you can modify the Exchange Server Version to the one you are working with: 

```
ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2); // TODO enter the Exchange
                                                                                 // Version you're using
 ```

### URI Format

To get the tasks assigned to a specfic user, use the following URI: 

```
ms-exchange:GETTASKS?url=foo&username=bar&password=secret&name=myName&mail=myMail
```

To write tasks back to the server, use the following URI:

```
ms-exchange:UPDATETASK?url=%s&username=%s&password=%s&name=%s&mail=%s
```

At the moment, it is only possible to update the status of a task. If updating a task, it is necessary to pass header values as described below:

| Name       | Type   | Description                                                     |
|------------|--------|-----------------------------------------------------------------|
| id_ext     | String | *Mandatory*: The id of the task as given by the Exchange Server |
| new_Status | String | *Mandatory*: The new status of the task                         |


#### Query Parameters

| Name      | Type   | Description                                                           |
|-----------|--------|-----------------------------------------------------------------------|
| password  | String | *Required* Password for account.                                      |
| username  | String | *Required* Username for account.                                      |
| mail      | String | *Required* e-mail address of account.                                 |
| name      | String | *Required* First and last name of user as declared in Exchange Server |
| url       | String | *Required URL of Exchange EWS, e.g. https://foo.bar/EWS/Exchange.asmx |
| openTasks | Boolean| true, if only the not completed or deffered tasks should be returned | 



### Examples
This example will get all open tasks assigned to user johnDoe, returned in JSON Format and stored in directory work/exchange/tasks.
The component is triggered by a timer called msExchange every 60 seconds.

```  
    private String url = context.resolvePropertyPlaceholders("{{ews.url}}");
    private String username = context.resolvePropertyPlaceholders("{{ews.username}}");
    private String password = context.resolvePropertyPlaceholders("{{ews.password}}");
    private String name = context.resolvePropertyPlaceholders("{{ews.name}}");
    private String mail = context.resolvePropertyPlaceholders("{{ews.mail}}");

from("timer://msExchange?fixedRate=true&period=60s")
     .toF("ms-exchange:GETTASKS?url=%s&username=%s&password=%s&name=%s&mail=%s&openTasks=true", url, username, password, name, mail)
     .to("log:response")
     .to("file:work/exchange/tasks/");
```

This example will update the task with the id '12345' with new status 'complete'. The component is triggered by a timer called msExchange every 60 seconds.


```
    private String url = context.resolvePropertyPlaceholders("{{ews.url}}");
    private String username = context.resolvePropertyPlaceholders("{{ews.username}}");
    private String password = context.resolvePropertyPlaceholders("{{ews.password}}");
    private String name = context.resolvePropertyPlaceholders("{{ews.name}}");
    private String mail = context.resolvePropertyPlaceholders("{{ews.mail}}");
   
from("timer://msExchange?fixedRate=true&period=60s")
    .setHeader("id_ext", constant(id_ext))
    .setHeader("new_Status", constant("Complete"))
    .toF("ms-exchange:UPDATETASK?url=%s&username=%s&password=%s&name=%s&mail=%s", url, username, password, name, mail)
    .to("log:response")                    
```

## Class Diagram

<a href="Exchange_ component.png?raw=true" target="_blank"><img src="Exchange_ component.png" 
alt="Architecture Overview" border="2" /></a>


