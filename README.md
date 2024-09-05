# Concurrent Computation by Adam Cross

This is a learning project, a Spring Boot web
service that will perform various numerical 
computations using *concurrency*. This is a work
in progress started Sept 3, 2024.

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


## TODO

Some known elements I should improve upon and will
given time.

* Validation of integer input and error handling for bad inputs.
* Add testing.

