# Human task with Custom object example

A simple example in order to test passing custom objects (_POJOS_) to the Kie Server. The idea is to create a process instance and then to complete a human task.

The project defines two Data Clases (_POJOS_):

* `Paciente`: A class that represents a patient, it has the following properties:
  * `numeroSocio`: [String] a identifier
  * `nombre`: [String] Name
  * `apellido`: [String] Surname
* `AutorizoReintegro`: A class that represent if the task was approved or rejected, it has the following properties:
  * `autorizoReintegro`: [Boolean] a flag to indicate if it was approved or rejected
  * `comentario`: [String] a comment
  
The business process is very simple. It has two variables `paciente` (`Paciente`) and `autorizoReintegro` (`AutorizoReintegro`). And a _Human Task_ (`EvaluarCobertura`) with one input variable (`paciente`, mapped to the process's variable with the same name) and one output varible (`autorizacionReintegro`, mapped to the process's variable with the same name). This last task has an output action (or script) which prints the value of `autorizoReintegro`.

When the process is started, the user (through a _task-form_) introduces the data of the _Paciente_, then the _User Task_ is created, it is assigned to the user whose name is `superuser`. This user has to start the task, update the output data of it and then completed. The `autorizacionReintegro` variable (introduced in this last task) is printed in the _EAP_ log after the task is completed.

If the process is instanciated through _bussiness central_ everything works as expected, e.g.: _task-forms_ are shown correctly, the user can input the data and finally the `autorizacionReintegro` variable is printed with the value entered by the user.

If the process is run through _kie server_ (using the _REST API_), although, the process can be started with a custom class, the task cannot be completed correctly. Through the _REST API_, the output data of the task can be setted and retrieved correctly. However, in the process, the variable `autorizacionReintegro` is `null`.


## How to reproduce the error?

The following code assumes:
* BPM Server is running locally
* a user called `superuser` exists
* the password of the user is `superuser12!`
* a container with the Business Process `BpmReintegros.Reintegros` is deployed in the _kie server_ and it's called `test`

Create a process instance
```shell
$ curl -u 'superuser:superuser12!' -X POST -H "Accept: application/json" -H "Content-type: application/json" -d '{"paciente":{"com.semperti.aleman.bpm.bpmreintegros.Paciente":{"apellido":"Apellido","nombre":"Nombre","numeroSocio":"123
"}}}' http://localhost:8080/kie-server/services/rest/server/containers/test/processes/BpmReintegros.Reintegros/instances
7
```

Start the task
```shell
$ curl -u 'superuser:superuser12!' -X PUT -H "Accept: application/json" -H "Content-type: application/json" http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/states/started
```

Retrieve Input content of task
```shell
$ curl -u 'superuser:superuser12!' -X GET -H "Accept: application/json" -H "Content-type: application/json" http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/contents/input
{
  "TaskName" : "EvaluarCobertura",
  "NodeName" : "Evaluar Cobertura",
  "paciente" : {"com.semperti.aleman.bpm.bpmreintegros.Paciente":{"numeroSocio":"1234","apellido":"Apellido","nombre":"Nombre"}},
  "Skippable" : "true",
  "ActorId" : "superuser"
}
```

Retrieve Output content of task
```shell
$ curl -u 'superuser:superuser12!' -X GET -H "Accept: application/json" -H "Content-type: application/json" http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/contents/output
{ }
```

Update Output content of task
```shell
$ curl -u 'superuser:superuser12!' -X PUT -H "Accept: application/json" -H "Content-type: application/json" -d '{"autorizacionReintegro":{"com.semperti.aleman.bpm.bpmreintegros.AutorizacionReintegro":{"autorizoReintegro": true,"comentario": "Si autorizo"}}}' http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/contents/output
```

Retrieve Ouput content of task
```shell
$ curl -u 'superuser:superuser12!' -X GET -H "Accept: application/json" -H "Content-type: application/json" http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/contents/output
{
  "autorizacionReintegro" : {"com.semperti.aleman.bpm.bpmreintegros.AutorizacionReintegro":{"autorizoReintegro":true,"comentario":"Si autorizo"}}
}
```

Complete the task
```shell
$ curl -u 'superuser:superuser12!' -X PUT -H "Accept: application/json" -H "Content-type: application/json" http://localhost:8080/kie-server/services/rest/server/containers/test/tasks/7/states/completed
```

Eap Log:
```
23:50:34,288 INFO  [stdout] (http-0.0.0.0:8080-9) Autorizacion reintegro: null
```

If the process instance's task created through the kie server's rest api is tried to be completed through the business central web interface, when the task is opened, the following error is displayed on the EAP log (the task cannot be completed, as the _task-form_ is never shown):
```
00:29:49,803 WARN  [org.jbpm.kie.services.impl.UserTaskServiceImpl] (http-0.0.0.0:8080-6) Cannot find runtime manager for task 7
00:29:49,804 WARN  [org.jbpm.kie.services.impl.UserTaskServiceImpl] (http-0.0.0.0:8080-6) Cannot find runtime manager for task 7
00:29:49,804 WARN  [org.jbpm.kie.services.impl.UserTaskServiceImpl] (http-0.0.0.0:8080-6) Cannot find runtime manager for task 7
00:29:49,805 WARN  [org.jbpm.kie.services.impl.UserTaskServiceImpl] (http-0.0.0.0:8080-6) Cannot find runtime manager for task 7
00:29:49,817 WARN  [org.jbpm.services.task.utils.ContentMarshallerHelper] (http-0.0.0.0:8080-6) Exception while unmarshaling content: java.lang.ClassNotFoundException: com.semperti.aleman.bpm.bpmreintegros.Paciente from [Module "deployment.business-central.war:main" from Service Module Loader]
	at org.jboss.modules.ModuleClassLoader.findClass(ModuleClassLoader.java:213) [jboss-modules.jar:1.3.7.Final-redhat-1]
	at org.jboss.modules.ConcurrentClassLoader.performLoadClassUnchecked(ConcurrentClassLoader.java:459) [jboss-modules.jar:1.3.7.Final-redhat-1]
	at org.jboss.modules.ConcurrentClassLoader.performLoadClassChecked(ConcurrentClassLoader.java:408) [jboss-modules.jar:1.3.7.Final-redhat-1]
	at org.jboss.modules.ConcurrentClassLoader.performLoadClass(ConcurrentClassLoader.java:389) [jboss-modules.jar:1.3.7.Final-redhat-1]
	at org.jboss.modules.ConcurrentClassLoader.loadClass(ConcurrentClassLoader.java:134) [jboss-modules.jar:1.3.7.Final-redhat-1]
	at java.lang.Class.forName0(Native Method) [rt.jar:1.8.0_111]
	at java.lang.Class.forName(Class.java:348) [rt.jar:1.8.0_111]
	at org.drools.core.util.ClassUtils.getClassFromName(ClassUtils.java:759) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.drools.core.common.DroolsObjectInputStream.resolveClass(DroolsObjectInputStream.java:57) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.drools.core.common.DroolsObjectInputStream.resolveClass(DroolsObjectInputStream.java:62) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:1620) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1521) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1781) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readObject(ObjectInputStream.java:373) [rt.jar:1.8.0_111]
	at java.util.ArrayList.readObject(ArrayList.java:791) [rt.jar:1.8.0_111]
	at sun.reflect.GeneratedMethodAccessor330.invoke(Unknown Source) [:1.8.0_111]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [rt.jar:1.8.0_111]
	at java.lang.reflect.Method.invoke(Method.java:498) [rt.jar:1.8.0_111]
	at java.io.ObjectStreamClass.invokeReadObject(ObjectStreamClass.java:1058) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:1909) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:1808) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1353) [rt.jar:1.8.0_111]
	at java.io.ObjectInputStream.readObject(ObjectInputStream.java:373) [rt.jar:1.8.0_111]
	at org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy$SerializablePlaceholderStrategyContext.read(SerializablePlaceholderResolverStrategy.java:93) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.drools.core.marshalling.impl.PersisterHelper.loadStrategiesIndex(PersisterHelper.java:327) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.drools.core.marshalling.impl.PersisterHelper.loadStrategiesCheckSignature(PersisterHelper.java:273) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.drools.core.marshalling.impl.PersisterHelper.readFromStreamWithHeaderPreloaded(PersisterHelper.java:289) [drools-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.jbpm.services.task.utils.ContentMarshallerHelper.unmarshall(ContentMarshallerHelper.java:115) [jbpm-human-task-core-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.jbpm.kie.services.impl.form.FormProviderServiceImpl.getFormDisplayTask(FormProviderServiceImpl.java:124) [jbpm-kie-services-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.jbpm.services.cdi.impl.form.FormProvidesServiceCDIImpl$Proxy$_$$_WeldClientProxy.getFormDisplayTask(FormProvidesServiceCDIImpl$Proxy$_$$_WeldClientProxy.java) [jbpm-services-cdi-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.jbpm.console.ng.ht.forms.backend.server.FormServiceEntryPointImpl.getFormDisplayTask(FormServiceEntryPointImpl.java:49) [jbpm-console-ng-human-tasks-forms-backend-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at org.jbpm.console.ng.ht.forms.backend.server.FormServiceEntryPointImpl$Proxy$_$$_WeldClientProxy.getFormDisplayTask(FormServiceEntryPointImpl$Proxy$_$$_WeldClientProxy.java) [jbpm-console-ng-human-tasks-forms-backend-6.5.0.Final-redhat-2.jar:6.5.0.Final-redhat-2]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [rt.jar:1.8.0_111]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) [rt.jar:1.8.0_111]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [rt.jar:1.8.0_111]
	at java.lang.reflect.Method.invoke(Method.java:498) [rt.jar:1.8.0_111]
	at org.jboss.errai.bus.server.io.AbstractRPCMethodCallback.invokeMethodFromMessage(AbstractRPCMethodCallback.java:48) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.io.ValueReplyRPCEndpointCallback.callback(ValueReplyRPCEndpointCallback.java:24) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.io.RemoteServiceCallback.callback(RemoteServiceCallback.java:54) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.cdi.server.CDIExtensionPoints$2.callback(CDIExtensionPoints.java:396) [errai-weld-integration-3.0.6.Final-redhat-1.jar:3.0.6.Final-redhat-1]
	at org.jboss.errai.bus.server.DeliveryPlan.deliver(DeliveryPlan.java:47) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.ServerMessageBusImpl.sendGlobal(ServerMessageBusImpl.java:296) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.SimpleDispatcher.dispatchGlobal(SimpleDispatcher.java:46) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.service.ErraiServiceImpl.store(ErraiServiceImpl.java:97) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.service.ErraiServiceImpl.store(ErraiServiceImpl.java:114) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at org.jboss.errai.bus.server.servlet.DefaultBlockingServlet.doPost(DefaultBlockingServlet.java:142) [errai-bus-3.2.4.Final-redhat-1.jar:3.2.4.Final-redhat-1]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:754) [jboss-servlet-api_3.0_spec-1.0.2.Final-redhat-2.jar:1.0.2.Final-redhat-2]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:847) [jboss-servlet-api_3.0_spec-1.0.2.Final-redhat-2.jar:1.0.2.Final-redhat-2]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:295) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:214) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.uberfire.ext.security.server.SecureHeadersFilter.doFilter(SecureHeadersFilter.java:69) [uberfire-servlet-security-0.9.0.Final-redhat-3.jar:0.9.0.Final-redhat-3]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:246) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:214) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.uberfire.ext.security.server.SecurityIntegrationFilter.doFilter(SecurityIntegrationFilter.java:57) [uberfire-servlet-security-0.9.0.Final-redhat-3.jar:0.9.0.Final-redhat-3]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:246) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:214) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:231) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:149) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:512) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.jboss.as.jpa.interceptor.WebNonTxEmCloserValve.invoke(WebNonTxEmCloserValve.java:50) [jboss-as-jpa-7.5.7.Final-redhat-3.jar:7.5.7.Final-redhat-3]
	at org.jboss.as.jpa.interceptor.WebNonTxEmCloserValve.invoke(WebNonTxEmCloserValve.java:50) [jboss-as-jpa-7.5.7.Final-redhat-3.jar:7.5.7.Final-redhat-3]
	at org.jboss.as.web.security.SecurityContextAssociationValve.invoke(SecurityContextAssociationValve.java:169) [jboss-as-web-7.5.7.Final-redhat-3.jar:7.5.7.Final-redhat-3]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:150) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:97) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.authenticator.SingleSignOn.invoke(SingleSignOn.java:419) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:102) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:344) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:854) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.process(Http11Protocol.java:654) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at org.apache.tomcat.util.net.JIoEndpoint$Worker.run(JIoEndpoint.java:926) [jbossweb-7.5.15.Final-redhat-1.jar:7.5.15.Final-redhat-1]
	at java.lang.Thread.run(Thread.java:745) [rt.jar:1.8.0_111]

00:29:50,538 WARN  [org.jbpm.kie.services.impl.UserTaskServiceImpl] (http-0.0.0.0:8080-11) Cannot find runtime manager for task 7
```

## Expected behaviour

As it was previously said, if the the process instance is started and the task is updated and completed throught the business central web interface, the variable `autorizacionReintegro` is setted correctly.

In this case, the log displays the following:
```
00:36:48,516 INFO  [stdout] (http-0.0.0.0:8080-6) Autorizacion reintegro: [ AutorizacionReintegro: autorizada comentario: 'Autorizo el reintegro!' ]
```
