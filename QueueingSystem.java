import java.util.ArrayList;

class QueueingSystem {
    
    int K, m, mu, gamma = 5, N;
    float rho, clock;
    ArrayList<Event> elist;

    QueueingSystem(int K, int m, int mu, float rho) {
        this.K = K;
        this.m = m;
        this.mu = mu;
        this.rho = rho;
        this.N = 0;
        this.clock = 0;
        this.elist = new ArrayList<Event>();
    }

    float getLambda() {
        return (float) this.rho * this.m * this.mu;
    }

    public static void main(String[] args) {
        if (args.length != 4)
            throw new IllegalArgumentException("usage: java System K m mu rho");
        
        int K = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        int mu = Integer.parseInt(args[2]);
        float rho = Float.parseFloat(args[3]); 
        
        QueueingSystem sys = new QueueingSystem(K, m, mu, rho);
        System.out.println(sys.getLambda());
    }

}