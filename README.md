# Concurrent Computation by Adam Cross

This is a learning project, a Spring Boot web
service that will perform various numerical 
computations using *concurrency*. This is a work
in progress started Sept 3, 2024.

Summary:

* Web service built using **Spring Boot with Web MVC**;
* Service computes prime factorization of
large integers using Java's **BigInteger**.
* Uses Spring's support for **asynchronous**
computation to perform computation off the main
request thread..

So far, this service can accept a request at the
endpoint `api/factor` to factor an integer into
primes, which will be done asynchronously. If 
you submit (post) `{"intToFactor" : "69938475523236412"}`,
the service will initiate the computation and
immediately send back an ID for the task.

Afterward, you can get the status/result of the
task by calling `api/factor/{id}`, which will
return a result something like this:

```
{
    "id": 2,
    "isCompleted": true,
    "number": "69938475523236412",
    "factors": [
        "2",
        "2",
        "4861",
        "39779",
        "90422537"
    ]
}
```


The goal is to practice and demonstrate concurrency
in a web service that later I will wire up to
a React frontend.

Another goal is to really dig into using concurrency.
At the moment, I have started by using Spring's 
support for Async methods and learned the value of that,
but I plan to add more meaty explorations and use of
concurrency.


## Current Tasks In Progress

### Task Limits via User Registration and Anonymous Use

I'm adding some limits on the tasks a user can
initiate. This means adding a whole user
authentication system (with **Spring Security**), but
I also want to support some anonymous use to
reduce friction for demonstrating the service. I
for one hate having to register for literally
everything on the Internet.

I'm going to use **JWT** for authentication.

I'll create some more delicate handling of threads
to manage the tasks allowed for any one user.


## TODO

Some known elements I should improve upon and will
given time.

* Validation of integer input and error handling for bad inputs.
* Add testing.
* Add ability to cancel a running task.
* Add some limits on usage to prevent abuse.

