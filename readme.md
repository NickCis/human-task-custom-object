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
$ curl -u 'superuser:superuser12!' -X POST -H "Accept: application/json" -H "Content-type: application/json" -d '{"paciente":{"com.semperti.aleman.bpm.bpmreintegros.Paciente":{"apellido":"Apellido","nombre":"Nombre","numeroSocio":"1234"}}}' http://localhost:8080/kie-server/services/rest/server/containers/test/processes/BpmReintegros.Reintegros/instances
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
