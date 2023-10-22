import java.util.ArrayList;

class QueueingSystem {
    
    // K = capacity of system, m = number of servers, N = number of customers in system
    int K, m, N;
    // mu = service rate of a server
    // gamma = arrival rate of first machine
    // rho = utilization of all servers
    float mu, gamma = 5, rho, clock;
    ArrayList<Event> elist;
    boolean done;

    QueueingSystem(int K, int m, float mu, float rho) {
        this.K = K;
        this.m = m;
        this.mu = mu;
        this.rho = rho;
        this.N = 0;
        this.clock = 0;
        this.elist = new ArrayList<Event>();
        this.done = false;
    }

    void insertEvent(Event e) {
        int i = 0;
        while (i < this.elist.size() && this.elist.get(i).time < e.time)
            i++;
        this.elist.add(i, e);
    }

    float getLambda() {
        // lambda = computed arrival rate of second machine
        float effective_rate = this.rho * this.m * this.mu;
        if (this.N < 2) // effective arrival rate = lambda + gamma (superposition)
            effective_rate += this.gamma;
        return effective_rate;
    }

    void run() {
        int num_dep = 0;
        // area = area under the graph of number of customers in system vs time
        float area = 0;
        // insert first arrival
        this.insertEvent(new Event(EventType.ARR, Exponential.get(this.getLambda())));
        while (!this.done) {
            // pop off the event list and update clock
            Event currEvent = this.elist.remove(0);
            area += this.N * (currEvent.time - this.clock);
            this.clock = currEvent.time;
            switch (currEvent.type) {
                case ARR:
                    if (this.N == this.K) {
                        // discard i.e. block
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR, this.clock + Exponential.get(this.getLambda())));
                    } else {
                        this.N++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR, this.clock + Exponential.get(this.getLambda())));
                        if (this.N <= this.m) // service
                            this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    }
                    break;
                case DEP:
                    num_dep++;
                    this.N--;
                    if (this.N >= this.m) // service next customer
                        this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    break;
            }
            if (num_dep > 100000)
                this.done = true;
        }
        System.out.println(area / this.clock);
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("usage: java QueueingSystem K m mu rho");
        
        int K = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        float mu = Float.parseFloat(args[2]);
        float rho = 0;
        QueueingSystem sys;
        for (int i = 0; i < 10; i++) {
            rho += 0.1;
            sys = new QueueingSystem(K, m, mu, rho);
            sys.run();
        }
    }

}