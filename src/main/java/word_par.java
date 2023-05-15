package main.java;

public class word_par {
    public double TF;
    public int size;
    public double score;
    public double TF_IDF;

    public word_par() {
        size=0;
        score=0;
        TF=0;
    }

//    @Override
//    public String toString() {
//        return "Pair{" +
//                "TF=" + TF +
//                ", size=" + size +
//                ", score=" + score +
//                ", index=" + index +
//                ", actualIndices=" + actualIndices +
//                ", TF_IDF=" + TF_IDF +
//                '}';
//    }

    public word_par(double TF, int size, double score) {
        this.TF = TF;
        this.size = size;
        this.score = score;
    }
}
