**Overview**

event-driven simulation
system state updated when events occur
when an event occurs, update system state:
    1. update system time to time at which the event occurred
    2. update other state parameters (number of customers in the queue)
    3. generate new events based on current event
move on to the next event in chronological order

**Queueing System**

two machines generate components for packaging:
    first machine has rate gamma components / min
    second machine has rate lambda components / min

if there are >= 2 components in the system,
    the first machine stops
if there are >= k (k >= 2) components in the system,
    second machine generates components but they don't enter the system

there are m workers who package components
each workers packaging time is exponentially distributed with average of 1/mu minutes

**Simulation**

two types of events:
    * arrivals
    * departures
when an arrival occurs:
    1. update system time to reflect the time of arrival
    2. increment number of customers in the system if not at full capacity and the arrival is not blocked
    3. if there is an idle server to accept the arriving customer, then generate a departure event for the new arrival. the departure time is the current system time plus an exponentially distributed length of time with parameter mu
    4. generate the next arrival event if applicable. the time of the next arrival will be the current system time plus an exponentially distributed amount of time
when a departure occurs:
    1. update the system time
    2. decrement the number of customers in the system
    3. if there are customers in the queue, and a server is available, then one customer enters service. generate a departure event for this customer.
    4. if applicable, generate an arrival event (may not be necessary depending on the implementation)

maintain an event list consisting of a linked list whose elements are data structures indicating the type of event and time at which it occurs.
by sorting in chronological order, the next event is selected from the head of the event list.
newly generated events are placed in correct chronological order.
the event list does not represent the state of the queueing system.

**Performance Measures**

maintain different metrics:
    * average number of jobs in the system
    * average time a job spends in the system
    * blocking probability versus rho, rho = lambda / (m * mu)
        * for each plot, rho should range between 0.1 and 1.0 with at least 10 data points
        * values of lambda can be determined from mu, rho, and m (run the simulation for lambda = 0.1 * m * mu, 0.2 * m * mu, etc.)
        * for each data point, run the simulation for >= 100000 departures

**Experiments**

do not hard code values of K, m, and mu into the program.
these are user inputs to the program.

1. let mu = 4 components / min, gamma = 5 components / min, plot the average number of components in the system vs rho for m = 2 and K = 4. include theoretical values of the expected number of customers versus rho. calculate these by programming or by hand.
2. plot average time spent in the system versus rho with mu = 4, gamma = 5, m = 2, and K = 4. plot theoretical values on the same graph.
3. plot the fraction of components that are blocked (discarded) versus rho, same parameters and include theoretical values.
4. plot total utilization of the system versus rho similarly.