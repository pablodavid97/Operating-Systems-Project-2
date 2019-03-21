
public enum Grain {
    CORN, BARLEY, RICE, WHEAT;
    static Grain randChoice() {
        Grain[] v = values();
        int n = P2.randInt(0, v.length - 1);
        return v[n];
    }
};
