package entities;

public class ArithmeticMean {

    private double sum;
    private int n;

    public ArithmeticMean() {

        this.sum = 0.0;
        this.n = 0;
    }

    public void addValue(double value) {

        this.sum += value;
        this.n++;
    }

    public double getMean() {
        return this.sum / this.n;
    }
}
