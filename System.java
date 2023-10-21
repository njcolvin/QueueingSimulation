
class System {
    
    int K, m, mu, gamma = 5, N;
    float rho, clock;

    System(int K, int m, int mu, int rho) {
        this.K = K;
        this.m = m;
        this.mu = mu;
        this.rho = rho;
        this.N = 0;
        this.clock = 0;
    }

    float getLambda() {
        return (float) this.rho * this.m * this.mu;
    }

    public static void main(String[] args) {
        if (args.length != 5)
            throw new IllegalArgumentException("usage: java System K m mu rho");        
    }

}