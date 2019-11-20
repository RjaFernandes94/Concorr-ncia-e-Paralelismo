#!/bin/sh

java="java -enableassertions -verbose:gc -Xms1024M"

warmup=2000
duration=${1}
nr_threads=${2:-1}
write_perc=${3:-50}


# list_impl=LinkedList
 list_impl=Synchronized
# list_impl=GlobalLock
# list_impl=GlobalRWLock
# list_impl=PerNodeLock
# list_impl=OptimisticPerNodeLock
# list_impl=LazyPerNodeLock
# list_impl=LockFree
value_range=262144
initial_size=256

echo "Running: "${java} -cp bin cp.benchmark.Driver -d ${duration} -w ${warmup} -n ${nr_threads} \
                cp.benchmark.intset.Benchmark ${list_impl} \
                -r ${value_range} -i ${initial_size} -w ${write_perc}""

${java} -cp bin cp.benchmark.Driver -d ${duration} -w ${warmup} -n ${nr_threads} \
                cp.benchmark.intset.Benchmark ${list_impl} \
                -r ${value_range} -i ${initial_size} -w ${write_perc}
