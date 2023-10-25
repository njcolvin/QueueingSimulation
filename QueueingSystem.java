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
    // lambda = computed arrival rate of second machine
    float getLambda() { return this.rho * this.m * this.mu; }

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

    void run() {
        int num_dep = 0, num_arr = 0;
        // area = area under the graph of number of customers in system vs time
        float area = 0;
        // both machines produce components to begin
        this.insertEvent(new Event(EventType.ARR1, Exponential.get(this.gamma)));
        this.insertEvent(new Event(EventType.ARR2, Exponential.get(this.getLambda())));
        while (!this.done) {
            // pop off the event list and update clock
            Event currEvent = this.elist.remove(0);
            area += this.N * (currEvent.time - this.clock);
            this.clock = currEvent.time;
            // handle event
            switch (currEvent.type) {
                case ARR1:
                    if (this.N >= 2) { 
                        // discard i.e. block
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR1, this.clock + Exponential.get(this.gamma)));
                    } else {
                        this.N++;
                        num_arr++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR1, this.clock + Exponential.get(this.gamma)));
                        if (this.N <= this.m) // service
                            this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    }
                    break;
                case ARR2:
                    if (this.N == this.K) {
                        // discard i.e. block
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR2, this.clock + Exponential.get(this.getLambda())));
                    } else {
                        this.N++;
                        num_arr++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR2, this.clock + Exponential.get(this.getLambda())));
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
        System.out.println("ρ = " + this.rho);
        // E[n] = area / t_end
        System.out.println(" E[n] = " + area / this.clock);
        // E[𝜏] = area / total # arrs
        System.out.println(" E[𝜏] = " + area / num_arr);

        System.out.println();
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("usage: java QueueingSystem K m mu rho");
        
        int K = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        float mu = Float.parseFloat(args[2]);
        float rho;
        QueueingSystem sys;
        for (int i = 0; i < 10; i++) {
            rho = (i + 1) / 10f;
            sys = new QueueingSystem(K, m, mu, rho);
            sys.run();
        }
    }

}