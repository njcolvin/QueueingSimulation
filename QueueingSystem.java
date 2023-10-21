import java.util.ArrayList;

class QueueingSystem {
    
    int K, m, N;
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
        float effective_rate = (float) this.rho * this.m * this.mu; // lambda
        if (this.N < 2) // effective rate is lambda + gamma
            effective_rate += this.gamma;
        return effective_rate;
    }

    void run() {
        int num_dep = 0;
        this.insertEvent(new Event(EventType.ARR, Exponential.get(this.getLambda())));
        while (!this.done) {
            Event currEvent = this.elist.remove(0);
            this.clock = currEvent.time;
            switch (currEvent.type) {
                case ARR:
                    // generate next arrival
                    this.insertEvent(new Event(EventType.ARR, this.clock + Exponential.get(this.getLambda())));
                    if (this.N >= this.K) // discard i.e. block
                        break;
                    this.N++;
                    if (this.N <= this.m) // service
                        this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
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
    }

    public static void main(String[] args) {
        if (args.length != 4)
            throw new IllegalArgumentException("usage: java QueueingSystem K m mu rho");
        
        int K = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        float mu = Float.parseFloat(args[2]);
        float rho = Float.parseFloat(args[3]); 
        
        QueueingSystem sys = new QueueingSystem(K, m, mu, rho);
        sys.run();
    }

}