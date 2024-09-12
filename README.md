# Concurrent Computation by Adam Cross

This is a learning project, a Spring Boot web
service that will perform various numerical 
computations using *concurrency*. This is a work
in progress started Sept 3, 2024.

# Summary

* RESTful Web service built using **Spring Boot with Web MVC**;
* Service computes prime factorization of
large integers using Java's **BigInteger**.
* Uses **Spring's** support for **asynchronous**
computation to perform computation off the main
request thread.
* Uses **ExecutorService** with fixed thread pool
to manage **multi-threaded** computation of prime
numbers.
* Basic validation of user inputs in place for
Primes in Range service.
* Allows **anonymous requests**, but puts **rate limits**
on them.

## Goals

The goal is to practice and demonstrate concurrency
in a web service that later I will wire up to
a React frontend.

Another goal is to really dig into using concurrency.
At the moment, I have started by using Spring's 
support for Async methods and learned the value of that,
but I plan to add more meaty explorations and use of
concurrency.

## Prime Factors Endpoint

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



## Compute Primes in Range Endpoint

I have begun building a service and endpoint at
`/api/primes` that computes the prime numbers in a given
input range. So far, it accepts a JSON request like this:

POST:
```
{
    "rangeMin" : "1",
    "rangeMax" : "10000"
}
```

Upon receiving this task request, it will create and
initiate the task and return immediately to give the
user a task ID.

In the background, computing primes in the range
is divided among different threads handled by an
ExecutorService with a fixed thread pool.

Each thread is given a sub-interval of the range
in which to compute the primes, and each thread
updates the database independently with the computed
primes.

The check the status and ultimately see the result
of the computation, the user can GET from the same
endpoint along with the task ID that was generated
above. The returned response will look something like
this:

```
{
    "id": 1,
    "isCompleted": true,
    "username": "adam",
    "rangeMin": "1",
    "rangeMax": "2000",
    "primes": [
        "4969",
        "3631",
        "2309",
       ...
    ]
}
```

Because each thread updates the database
independently, it is possible to view a partial
list of the primes if some threads finish before
others and the user queries at exactly the right
time. The user would know from the `isCompleted`
flag whether computation is still in progress.

I am currently building the logic and structure
to allow users to cancel/interrupt tasks in motion. This
will be initiated using a PATCH request from the
user, which will then attempt to interrupt all
associated running threads and cancel any that
are queued.

### To Do

Some basic elements and improvements are still
in progress here, as follows:

* Proper user authorization;
* Reasonable restrictions on usage;
* Validation of input values;
* Testing


## Current To-Do Items In Progress

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

