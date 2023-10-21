public class Exponential {

    // generator params
    static long k = 16807, m = Integer.MAX_VALUE, s0 = 1234;
    
    private static float getUniform() {
        // s_n = k * s_0 mod m
        long sn = (k * s0) % m;
        // r_n = s_n / m
        float rn = (float) sn / m;
        // iterate
        s0 = sn;
        return rn;
    }

    public static float get(float lambda) {
        // generate uniform RV
        float u = getUniform();
        // generate exponential RV
        return (float) (-(1 / lambda) * Math.log(u));
    }
}
