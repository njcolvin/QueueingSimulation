import java.util.ArrayList;

class QueueingSystem {
    
    // K = capacity of system, m = number of servers, N = number of customers in system
    int K, m, N;
    // µ = service rate of a server
    // γ = arrival rate of first machine
    // ρ = utilization of all servers
    float mu, gamma = 5, rho, clock, expectedNumCust, expectedTimeCust, expectedProbBlock;
    // λ = computed arrival rate of second machine
    float getLambda() { return this.rho * this.m * this.mu; }
    // state parameters
    ArrayList<Event> elist;
    ArrayList<Float> stateProbs;
    boolean done;

    QueueingSystem(int K, int m, float mu, float rho) {
        this.K = K;
        this.m = m;
        this.N = 0;
        this.mu = mu;
        this.rho = rho;
        this.clock = 0;
        this.elist = new ArrayList<Event>();
        this.stateProbs = new ArrayList<Float>();
        this.done = false;
        this.computeStateProbs();
        this.computeMetrics();
    }

    void computeStateProbs() {
        ArrayList<Float> coefficients = new ArrayList<Float>();
        float numerator = this.getLambda() + this.gamma, denominator = this.mu;
        // both machines working, p(1) = p(0) * (λ + γ) / µ
        coefficients.add(numerator / denominator); 
        numerator *= this.getLambda() + this.gamma;
        // multiply µ by number of workers
        denominator *= this.m > 1 ? 2 * this.mu : this.mu;
        // both machines working, p(2) = p(0) * (λ + γ)^2 / if(m > 1, 2µ^2, µ^2)
        coefficients.add(numerator / denominator);
        for (int i = 2; i < this.K; i++) {
            // only machine 2 working
            numerator *= this.getLambda();
            denominator *= this.m > i ? i * this.mu : this.m * this.mu;
            // for i >= 2, p(i) = p(0) * coef(p(i-1)) * λ / if(m > i, iµ, mµ)
            coefficients.add(numerator / denominator); 
        }
        // compute p(0) = 1 / (1 + (λ + γ) / µ + (λ + γ)^2 / if(m > 1, 2µ^2, µ^2) + ...)
        float sum = 1;
        for (float f : coefficients)
            sum += f;
        this.stateProbs.add(1 / sum);
        // compute p(1)..p(K)
        for (int i = 0; i < coefficients.size(); i++)
            this.stateProbs.add(coefficients.get(i) * this.stateProbs.get(0));
    }

    void computeMetrics() {
        // E[n] = i * p(i) for i = 0..K
        this.expectedNumCust = 0;
        for (int i = 0; i <= this.K; i++)
            expectedNumCust += i * this.stateProbs.get(i);
        // E[𝜏] = E[n] / λ_avg (Little's Law)
        // λ_avg = ((λ + γ)p(0) + (λ + γ)p(1) + λp(2) + ... + λp(k))
        float lambdaAvg = 0;
        lambdaAvg += (this.getLambda() + this.gamma) * this.stateProbs.get(0);
        lambdaAvg += (this.getLambda() + this.gamma) * this.stateProbs.get(1);
        for (int i = 2; i <= this.K; i++)
            lambdaAvg += this.getLambda() * this.stateProbs.get(i);
        this.expectedTimeCust = this.expectedNumCust / lambdaAvg;
        // P(block) = λp(k) / λ_avg
        this.expectedProbBlock = this.getLambda() * this.stateProbs.get(this.K) / lambdaAvg;
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
                        // discard i.e. block but dont count for simulation
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
        System.out.println(" State Probabilities");
        for (int i = 0; i <= this.K; i++)
            System.out.println("  p(" + i + ") = " + this.stateProbs.get(i));
        System.out.println(" Expected E[n] = " + this.expectedNumCust);
        // E[n] = area / t_end
        System.out.println(" Actual E[n]   = " + area / this.clock);
        // E[𝜏] = area / total # arrs
        System.out.println(" Expected E[𝜏] = " + this.expectedTimeCust);
        System.out.println(" Actual E[𝜏]   = " + area / numArr);
        // P(block) = total # blocks / total # arrs\
        System.out.println(" Expected P(block) = " + this.expectedProbBlock);
        System.out.println(" Actual P(block)   = " + (float) numBlock / numArr);
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