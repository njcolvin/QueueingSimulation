import java.util.ArrayList;

class QueueingSystem {
    
    // K = capacity of system, m = number of servers, N = number of customers in system
    int K, m, N;
    // µ = service rate of a server
    // γ = arrival rate of first machine
    // ρ = utilization of all servers
    float mu, gamma = 5, rho, clock, E_N, E_Tau, P_b;
    ArrayList<Event> elist;
    boolean done;
    // λ = computed arrival rate of second machine
    float getLambda() { return this.rho * this.m * this.mu; }
    ArrayList<Float> stateProbs;

    QueueingSystem(int K, int m, float mu, float rho) {
        this.K = K;
        this.m = m;
        this.mu = mu;
        this.rho = rho;
        this.N = 0;
        this.clock = 0;
        this.elist = new ArrayList<Event>();
        this.done = false;
        this.stateProbs = new ArrayList<Float>();
        this.computeStateProbs();
        this.computeMetrics();
    }

    void computeStateProbs() {
        ArrayList<Float> coefficients = new ArrayList<Float>();
        float numerator = this.getLambda() + this.gamma, denominator = this.mu;
        coefficients.add(numerator / denominator); // p1 = p0 * (λ + γ) / µ
        if (this.K > 1) {
            numerator *= this.getLambda() + this.gamma; // (λ + γ)^2
            denominator *= this.m > 1 ? 2 * this.mu : this.mu; // 2µ^2 or µ^2 
            coefficients.add(numerator / denominator); // p2 = ...
        }
        for (int i = 2; i < this.K; i++) {
            numerator *= this.getLambda();
            denominator *= this.m > i ? i * this.mu : this.m * this.mu;
            coefficients.add(numerator / denominator);
        }
        // compute p0
        float sum = 1;
        for (float f : coefficients)
            sum += f;
        this.stateProbs.add(1 / sum);
        // compute p1..pk
        for (int i = 0; i < coefficients.size(); i++)
            this.stateProbs.add(coefficients.get(i) * this.stateProbs.get(0));
    }

    void computeMetrics() {
        // E_N
        this.E_N = 0;
        for (int i = 0; i <= this.K; i++)
            E_N += i * this.stateProbs.get(i);
    }

    void insertEvent(Event e) {
        int i = 0;
        while (i < this.elist.size() && this.elist.get(i).time < e.time)
            i++;
        this.elist.add(i, e);
    }

    void run() {
        int numDep = 0, numArr = 0, numBlock = 0;
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
                        numArr++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR1, this.clock + Exponential.get(this.gamma)));
                        if (this.N <= this.m) // service
                            this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    }
                    break;
                case ARR2:
                    if (this.N == this.K) {
                        // discard i.e. block
                        numBlock++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR2, this.clock + Exponential.get(this.getLambda())));
                    } else {
                        this.N++;
                        numArr++;
                        // generate next arrival
                        this.insertEvent(new Event(EventType.ARR2, this.clock + Exponential.get(this.getLambda())));
                        if (this.N <= this.m) // service
                            this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    }
                    break;
                case DEP:
                    numDep++;
                    this.N--;
                    if (this.N >= this.m) // service next customer
                        this.insertEvent(new Event(EventType.DEP, this.clock + Exponential.get(this.mu)));
                    break;
            }
            if (numDep > 100000)
                this.done = true;
        }
        System.out.println("ρ = " + this.rho);
        System.out.println(" Expected E[n] = " + this.E_N);
        // E[n] = area / t_end
        System.out.println(" Actual E[n] = " + area / this.clock);
        // E[𝜏] = area / total # arrs
        System.out.println(" E[𝜏] = " + area / numArr);
        // P_block = total # blocks / total # arrs
        System.out.println(" P_b = " + (float) numBlock / numArr);
        System.out.println();
    }

    public static void main(String[] args) {
        if (args.length != 3)
            throw new IllegalArgumentException("usage: java QueueingSystem K m µ");
        
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