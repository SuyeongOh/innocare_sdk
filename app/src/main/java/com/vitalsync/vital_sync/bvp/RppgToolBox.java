package com.vitalsync.vital_sync.bvp;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class RppgToolBox {
    //lambda POS = 100
    public static double[] detrend(double[] signal, double lambda){
        int length = signal.length;

        double[][] H = new double[signal.length][signal.length];
        for(int i = 0; i < length; i++){
            H[i][i] = 1;
        }
        RealMatrix HMatrix = new Array2DRowRealMatrix(H);

        double[][] sparse_array = new double[signal.length - 2][signal.length];

        for(int i = 0; i < signal.length - 2 - 2; i++){
            sparse_array[i][i] = 1;
            sparse_array[i][i+1] = -2;
            sparse_array[i][i+2] = 1;
        }
        sparse_array[signal.length - 4][signal.length - 4] = 1;
        sparse_array[signal.length - 4][signal.length - 3] = -2;
        sparse_array[signal.length - 3][signal.length - 3] = 1;
        //sparse_array 검증완료

        RealMatrix sparseMatrix = new Array2DRowRealMatrix(sparse_array);
        RealMatrix productMatrix = sparseMatrix.transpose().multiply(sparseMatrix);

        double normalValue = lambda * lambda;
        productMatrix = productMatrix.scalarMultiply(normalValue);

        productMatrix = productMatrix.add(HMatrix);

        RealMatrix invMatrix = MatrixUtils.inverse(productMatrix);

        invMatrix = HMatrix.subtract(invMatrix);
        RealMatrix signalMatrix = MatrixUtils.createColumnRealMatrix(signal);

        double[] signal_D = invMatrix.multiply(signalMatrix).transpose().getData()[0];
        double[] signal_remove = new double[signal_D.length - 30];
        for(int i = 0; i < signal_D.length-30; i++){
            signal_remove[i] = signal_D[i];
        }
        return signal_remove;
    }
}
