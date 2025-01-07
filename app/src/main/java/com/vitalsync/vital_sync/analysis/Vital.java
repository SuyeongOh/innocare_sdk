package com.vitalsync.vital_sync.analysis;

import android.graphics.Bitmap;
import android.util.Log;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.github.psambit9791.jdsp.signal.Detrend;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.github.psambit9791.jdsp.signal.peaks.FindPeak;
import com.github.psambit9791.jdsp.signal.peaks.Peak;
import com.github.psambit9791.jdsp.transform.DiscreteFourier;
import com.github.psambit9791.jdsp.transform.FastFourier;
import com.github.psambit9791.jdsp.transform.Hilbert;
import com.vitalsync.vital_sync.bvp.BandPassFilter;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.ResultVitalSign;
import com.vitalsync.vital_sync.data.Rppg;
import com.vitalsync.vital_sync.data.VitalChartData;
import com.vitalsync.vital_sync.service.vital.VitalClient;
import com.vitalsync.vital_sync.service.vital.VitalResponse;
import com.vitalsync.vital_sync.utils.DoubleUtils;
import com.vitalsync.vital_sync.utils.RppgUtils;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import jsat.linear.DenseMatrix;
import jsat.linear.DenseVector;
import jsat.linear.Matrix;
import jsat.linear.Vec;

import static java.lang.Math.abs;
import static java.lang.Math.log10;

public class Vital {
    public static final int BUFFER_SIZE = Config.ANALYSIS_TIME * Config.TARGET_FRAME;
    public static int VIDEO_FRAME_RATE = 30;

    private static final int DETREND_POWER = 6;

    public static class Result {
        public float LF_HF_ratio = 0;
        public double HR_result = 0;
        public double RR_result = 0;
        public double spo2_result = 0;
        public double sdnn_result = 0;
        public double BP = 0;
        public double SBP = 0;
        public double DBP = 0;
    }

    private final Result lastResult = new Result();

    private long firstFrameTime = 0;
    private long lastFrameTime = 0;
    final float r_banddy = -2335.36371041202f;
    final float b_banddy = -2335.36371041202f;
    private int bufferIndex = 0;
    private int pixelIndex = 0;

    public Rppg rPPG = new Rppg(BUFFER_SIZE);

    public Result calculateVital(FaceImageModel model) throws IllegalArgumentException {
        if (pixelIndex == 0) firstFrameTime = model.frameUtcTimeMs;

        rPPG.frameTimeArray[pixelIndex] = model.frameUtcTimeMs;
        Bitmap bitmap = model.bitmap;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float totalPixel = (float) width * height;
        float totalR = 0, totalG = 0, totalB = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixels_buffer = bitmap.getPixel(i, j);

                int r = (pixels_buffer & 0xFF0000) >> 16;
                int g = (pixels_buffer & 0x00FF00) >> 8;
                int b = (pixels_buffer & 0x0000FF);

                totalR += r;
                totalG += g;
                totalB += b;
            }
        }
        totalR = totalR / totalPixel;
        totalG = totalG / totalPixel;
        totalB = totalB / totalPixel;

        rPPG.f_pixel_buff[0][pixelIndex] = totalR;
        rPPG.f_pixel_buff[1][pixelIndex] = totalG;
        rPPG.f_pixel_buff[2][pixelIndex] = totalB;

        if (model.isFinish) {
            lastFrameTime = model.frameUtcTimeMs;
            VIDEO_FRAME_RATE = (int) (1000 / ((float) ((lastFrameTime - firstFrameTime) / (float) pixelIndex)));
            //TODO interpolation
            long[] interpolatedTimeArray = RppgUtils.interpolateTime(rPPG.frameTimeArray, VIDEO_FRAME_RATE);
            double[][] interpolatedSignal = RppgUtils.interpolateSignal(rPPG.f_pixel_buff, rPPG.frameTimeArray, interpolatedTimeArray, VIDEO_FRAME_RATE);
            VitalChartData.frameTimeArray = rPPG.frameTimeArray;

            //TODO preprocessing은 interpolated signal을 이용

            requestToServer();

            double[] pre_processed = preprocessing_omit(interpolatedSignal, interpolatedTimeArray);
            VitalChartData.FFT_SIGNAL = pre_processed;
            rPPG.lastHrSignal = pre_processed;
            lastResult.HR_result = get_HR(pre_processed);
            Log.d("BPM", "HR : " + lastResult.HR_result);
            lastResult.RR_result = get_RR(VitalChartData.CORE_SIGNAL);
            Log.d("BPM", "RR : " + lastResult.RR_result);


            //--------SDNN --------------------//
            lastResult.sdnn_result = HRV_IBI(rPPG.lastBvpSignal, rPPG, lastResult.HR_result);
            lastResult.sdnn_result = Math.round(lastResult.sdnn_result);
            //--------SDNN --------------------//
            lastResult.LF_HF_ratio = LF_HF_ratio(VitalChartData.DETREND_SIGNAL);

            Log.d("vital", String.format(
                    "HR : %f, hrv : %f"
                    , lastResult.HR_result, lastResult.sdnn_result));

            try {
                lastResult.spo2_result = spo2(rPPG.f_pixel_buff[0], rPPG.f_pixel_buff[1], rPPG.f_pixel_buff[2], VIDEO_FRAME_RATE);
                lastResult.spo2_result = Math.round(lastResult.spo2_result);
            } catch (Exception e) {
                lastResult.spo2_result = 0;
            }


            double[] avg = get_peak_avg(VitalChartData.DETREND_SIGNAL, lastResult.HR_result);
            double peak_avg = avg[0];
            double valley_avg = avg[1];
            Log.d("BP", "" + peak_avg + ":" + valley_avg);
            double bmi = Config.USER_BMI;

            if (bmi == 0) bmi = 20.1f;

            //Resistivity of blood https://github.com/miller619/bloodPressure_FYP2_2017
            double ROB = 18.31;
            //Ejection Time
            double ET = (364.5-1.23 * lastResult.HR_result);
            //Body Surface Area
            double BSA = 0.007184*(Math.pow(Config.USER_WEIGHT,0.425))*(Math.pow(Config.USER_HEIGHT,0.725));
            //Stroke volume
            double SV = (-6.6 + (0.25*(ET-35)) - (0.62*lastResult.HR_result) + (40.4*BSA) - (0.51*Config.USER_AGE));
            //Pulse Pressure
            double PP = SV / ((0.013 * Config.USER_WEIGHT - 0.007*Config.USER_AGE-0.004 * lastResult.HR_result)+1.307);

            //심박출량 : 각 심실에서 1분에 폐나 순환계로 뿜 어 내보내는 이완기 혈액의 양(정상치: 4-8L/min)
            double user_cardiac_output = 6;
            double MAP = user_cardiac_output * ROB;

            double user_mbp = (Config.USER_SBP + Config.USER_DBP * 2) / 3;

            MAP = (user_mbp == 0) ? MAP : user_mbp;

            int SP = (int) (MAP + 3/2*PP);
            int DP = (int) (MAP - PP/3);

            Log.d("BPM ", "Sys "+SP+" Dys "+DP+" Beats "+lastResult.HR_result);
            lastResult.SBP = SP;
            lastResult.DBP = DP;

//            lastResult.SBP = 23.7889 + (95.4335 * peak_avg) + (4.5958 * bmi) - (5.109 * peak_avg * bmi);
//            lastResult.DBP = -17.3772 - (115.1747 * valley_avg) + (4.0251 * bmi) + (5.2825 * valley_avg * bmi);
            lastResult.BP = lastResult.SBP * 0.33 + lastResult.DBP * 0.66;

        }
        pixelIndex++;
        return lastResult;
    }

    private void requestToServer(){
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String measureTime = sdf.format(currentDate);
        Config.Measure_Time = measureTime;
        //Web server prototype
        if (Config.SERVER_RESPONSE_MODE) {
            try{
                VitalResponse response = VitalClient.getInstance().requestSyncAnalysis(rPPG.f_pixel_buff, measureTime).body();
                ResultVitalSign.vitalSignServerData.HR = response.hr;
                ResultVitalSign.vitalSignServerData.RR = response.rr;
                ResultVitalSign.vitalSignServerData.HRV = response.hrv;
                ResultVitalSign.vitalSignServerData.SpO2 = response.spo2;
                ResultVitalSign.vitalSignServerData.STRESS = (float) response.stress;
                ResultVitalSign.vitalSignServerData.SBP = response.sbp;
                ResultVitalSign.vitalSignServerData.DBP = response.dbp;
                ResultVitalSign.vitalSignServerData.BP = response.bp;
            } catch (Exception e){
                Log.e("VitalServer", e.getMessage());
            }
        }
    }
    public double[] preprocessing_omit(double[][] pixel, long[] InterpolatedFrameTime) {
        //pixel = setRGB.setRGB();
        VitalChartData.R_SIGNAL = pixel[0];
        VitalChartData.G_SIGNAL = pixel[1];
        VitalChartData.B_SIGNAL = pixel[2];

        double[][] smoothPixel = new double[pixel.length][pixel[0].length];
        smoothPixel[0] = new Smooth(pixel[0], 4, "rectangular").smoothSignal();
        smoothPixel[1] = new Smooth(pixel[1], 4, "rectangular").smoothSignal();
        smoothPixel[2] = new Smooth(pixel[2], 4, "rectangular").smoothSignal();

        VitalChartData.SMOOTH_R_SIGNAL = smoothPixel[0];
        VitalChartData.SMOOTH_G_SIGNAL = smoothPixel[0];
        VitalChartData.SMOOTH_B_SIGNAL = smoothPixel[0];

        //Time Smoothing
        double[] timeToDouble = new double[InterpolatedFrameTime.length];
        for (int i = 0; i < timeToDouble.length; i++) {
            timeToDouble[i] = InterpolatedFrameTime[i];
        }
        rPPG.frameDoubleTimeArray = new Smooth(timeToDouble, 4, "rectangular").smoothSignal();
        Vec v = new DenseVector(1);
        v.mutableAdd(1);

        DenseMatrix signal = new DenseMatrix(smoothPixel);

        Matrix[] qr = signal.qr();
        Matrix q = qr[0];

        Vec sVec = q.getColumn(0);

        DenseMatrix S = new DenseMatrix(v, sVec);

        DenseMatrix identify = DenseMatrix.eye(3);
        Matrix P = identify.subtract(S.transpose().multiply(S));

        Matrix Y = P.multiply(signal);

        Vec bvpVec = Y.getRow(1);

        VitalChartData.CORE_SIGNAL = bvpVec.arrayCopy();

        Detrend d2 = new Detrend(bvpVec.arrayCopy(), DETREND_POWER);
        double[] d_g = d2.detrendSignal();
//        double[] d_g = RppgToolBox.detrend(bvpVec.arrayCopy(), 100);

        VitalChartData.DETREND_SIGNAL = d_g;

        BandPassFilter bpf_hr = new BandPassFilter(Config.MAX_HR_FREQUENCY, Config.MIN_HR_FREQUENCY);
        BandPassFilter bpf_rr = new BandPassFilter(Config.MAX_RR_FREQUENCY, Config.MIN_RR_FREQUENCY);
        double[] bpf_hr_signal = new double[d_g.length];
        double[] bpf_rr_signal = new double[d_g.length];
        for (int i = 1; i < d_g.length; i++) {
            bpf_hr_signal[i] = bpf_hr.filter(d_g[i], rPPG.frameDoubleTimeArray[i] - rPPG.frameDoubleTimeArray[i - 1]);
            bpf_rr_signal[i] = bpf_rr.filter(d_g[i], rPPG.frameDoubleTimeArray[i] - rPPG.frameDoubleTimeArray[i - 1]);
        }

        VitalChartData.BPF_SIGNAL = bpf_hr_signal;
        rPPG.lastBvpSignal = bpf_hr_signal;
        rPPG.lastRrSignal = bpf_rr_signal;
        VitalChartData.BVP_SIGNAL = rPPG.lastBvpSignal;

        FastFourier fft = new FastFourier(bpf_hr_signal);
        fft.transform();

        return fft.getMagnitude(true);
    }

    public ArrayList<ArrayList<Integer>> get_peak_idx(double[] signal, double hr){
        int minWindowSize = (int) (VIDEO_FRAME_RATE * 60 / hr * 0.8);
        int maxWindowSize = (int) (VIDEO_FRAME_RATE * 60 / hr * 1.2);

        ArrayList<Integer> peakArray = new ArrayList<>();
        ArrayList<Integer> valleyArray = new ArrayList<>();
        //peak detect
        int firstIndex = 0;
        double[] slice = Arrays.copyOfRange(signal, 0, maxWindowSize);
        FindPeak peak = new FindPeak(slice);
        int[] WindowArray = peak.detectRelativeMaxima();

        for (int i = 0; i < WindowArray.length; i++) {
            if (signal[0] < signal[WindowArray[i]]) {
                firstIndex = i;
            }
        }

        peakArray.add(firstIndex);

        if (!(firstIndex == 1 || firstIndex == 0)) {
            firstIndex = firstIndex - 2;
        }
        slice = Arrays.copyOfRange(signal, firstIndex, signal.length - 1);
        peak = new FindPeak(slice);
        WindowArray = peak.detectPeaks().filterByPeakDistance(minWindowSize);

        for (int peakIndex : WindowArray) {
            int realIndex = peakIndex + firstIndex;
            peakArray.add(realIndex);
        }

        //valley detect

        for (int i = 1; i < peakArray.size(); i++) {
            double[] targetArray;
            try {
                targetArray = Arrays.copyOfRange(signal, peakArray.get(i - 1), peakArray.get(i));
            } catch (Exception e) {
                continue;
            }

            FindPeak findValley = new FindPeak(targetArray);
            int[] valleys = findValley.detectRelativeMinima();
            if (valleys.length == 0) continue;
            int minIndex = valleys[0];
            for (int j = 0; j < valleys.length; j++) {
                if (targetArray[minIndex] > targetArray[valleys[j]]) {
                    minIndex = j;
                }
            }
            valleyArray.add(peakArray.get(i - 1) + minIndex);
        }

        ArrayList<ArrayList<Integer>> peak_N_valley = new ArrayList<>();

        peak_N_valley.add(peakArray);
        peak_N_valley.add(valleyArray);

        return peak_N_valley;


    }

    public double[] get_peak_avg(double[] signalG, double hr) { // flag 0 : vally 1 : peak
        ArrayList<ArrayList<Integer>> peak_N_valley = get_peak_idx(signalG, hr);

        ArrayList<Double> peakPower = new ArrayList<>();
        ArrayList<Double> valleyPower = new ArrayList<>();

        //[0] : peak, [1] : valley
        for(int peak : peak_N_valley.get(0)){
            peakPower.add(signalG[peak]);
        }
        for(int valley : peak_N_valley.get(1)){
            valleyPower.add(signalG[valley]);
        }
        double[] resultArray = new double[2];

        resultArray[0] = peakPower.stream().mapToDouble(e -> e).average().orElse(0.0);
        resultArray[1] = valleyPower.stream().mapToDouble(e -> e).average().orElse(0.0);


        //double[0] = peak, double[1] = valley
        return resultArray;
    }


    public float get_HR(double[] real_dft) {
        ArrayList<Double> hr_signal = new ArrayList<>();
        int max_index = 0;
        float max_val = 0;
        float frequency_interval = VIDEO_FRAME_RATE / (float) (real_dft.length * 2);
        VitalChartData.FREQUENCY_INTERVAL = frequency_interval;
        VitalChartData.FRAME_RATE = VIDEO_FRAME_RATE;
        for (int i = 0; i < real_dft.length; i++) {
            if (i * frequency_interval < 0.75)
                continue;
            else if (i * frequency_interval > 2.5) {
                break;
            } else {
                if (VitalChartData.START_FILTER_INDEX == 0) {
                    VitalChartData.START_FILTER_INDEX = i;
                }
                hr_signal.add(real_dft[i]);
                if (real_dft[i] > max_val) {
                    max_val = (float) real_dft[i];
                    max_index = i;
                }
            }
        }

        VitalChartData.HR_SIGNAL = new double[hr_signal.size()];
        for (int i = 0; i < hr_signal.size(); i++) {
            VitalChartData.HR_SIGNAL[i] = hr_signal.get(i);
        }
        return max_index * frequency_interval * 60;
    }

    public float get_RR(double[] rrSignal) {
        int max_index = 0;
        float max_val = 0;

        FastFourier fft = new FastFourier(rrSignal);
        fft.transform();
        double[] rrFFT = fft.getMagnitude(true);

        float frequency_interval = 10 / (float) rrFFT.length;
        for (int i = 0; i < rrFFT.length; i++) {
            if (i * frequency_interval < 0.18)
                continue;
            else if (i * frequency_interval > 0.5) {
                break;
            } else {
                if (rrFFT[i] > max_val) {
                    max_val = (float) rrFFT[i];
                    max_index = i;
                }
            }
        }
        return max_index * frequency_interval * 48;
    }

    public float LF_HF_ratio(double[] real_dft) {
        float LF = 0.0f;
        float HF = 0.0f;
        float filter_interval = VIDEO_FRAME_RATE / (float)real_dft.length;

        BandPassFilter bpf_lf = new BandPassFilter(Config.MAX_LF_FREQUENCY, Config.MIN_LF_FREQUENCY);
        BandPassFilter bpf_hf = new BandPassFilter(Config.MAX_HF_FREQUENCY, Config.MIN_HF_FREQUENCY);
        double[] bpf_lf_signal = new double[real_dft.length];
        double[] bpf_hf_signal = new double[real_dft.length];
        for (int i = 1; i < real_dft.length; i++) {
            bpf_lf_signal[i] = bpf_lf.filter(real_dft[i], rPPG.frameDoubleTimeArray[i] - rPPG.frameDoubleTimeArray[i - 1]);
            bpf_hf_signal[i] = bpf_hf.filter(real_dft[i], rPPG.frameDoubleTimeArray[i] - rPPG.frameDoubleTimeArray[i - 1]);
        }

        FastFourier fft_lf = new FastFourier(bpf_lf_signal);
        FastFourier fft_hf = new FastFourier(bpf_hf_signal);

        fft_lf.transform();
        fft_hf.transform();

        double[] power_lf = fft_lf.getMagnitude(true);
        double[] power_hf = fft_hf.getMagnitude(true);

        for( int i =0 ; i < bpf_lf_signal.length ; i++){
            if(Config.MIN_LF_FREQUENCY <= i * filter_interval
                    && i * filter_interval < Config.MAX_LF_FREQUENCY)
                LF += power_lf[i];
            else if(Config.MIN_HF_FREQUENCY <= i * filter_interval
                    && i * filter_interval <= Config.MAX_HF_FREQUENCY)
                HF += power_hf[i];
        }
        Log.d("Vital", "LF : " + LF + ", HF : " + HF);
        return LF/HF;
    }

    public double HRV_IBI(double[] signalG, Rppg rppg, double hr) {
        double hrv = 0;
        //peak detect
        ArrayList<Long> peakTimes = new ArrayList<>();

        int minWindowSize = (int) (VIDEO_FRAME_RATE * 60 / hr * 0.8);
        int maxWindowSize = (int) (VIDEO_FRAME_RATE * 60 / hr * 1.2);
        ArrayList<Integer> peakArray = new ArrayList<>();

        int firstIndex = 0;
        double[] slice = Arrays.copyOfRange(signalG, 0, maxWindowSize);
        FindPeak peak = new FindPeak(slice);
        int[] WindowArray = peak.detectRelativeMaxima();

        for (int i = 0; i < WindowArray.length; i++) {
            if (signalG[0] < signalG[WindowArray[i]]) {
                firstIndex = i;
            }
        }

        peakArray.add(firstIndex);
        peakTimes.add(rppg.frameTimeArray[firstIndex]);

        if (!(firstIndex == 1 || firstIndex == 0)) {
            firstIndex = firstIndex - 2;
        }
        slice = Arrays.copyOfRange(signalG, firstIndex, signalG.length - 1);
        peak = new FindPeak(slice);
        WindowArray = peak.detectPeaks().filterByPeakDistance(minWindowSize);

        for (int peakIndex : WindowArray) {
            int realIndex = peakIndex + firstIndex;
            peakArray.add(realIndex);
            peakTimes.add(rppg.frameTimeArray[realIndex]);
        }

        VitalChartData.HRV_PEAK = peakArray.stream().mapToInt(e -> e).toArray();

        ArrayList<Long> rrIntervals = new ArrayList<>();
        if (peakTimes.size() > 6) {
            for (int i = 0; i < peakTimes.size() - 2; i++) {
                rrIntervals.add(peakTimes.get(i + 1) - peakTimes.get(i));
            }
        }

        if (rrIntervals.size() == 0) return 0;

        //rr_interval 0.4~1.3
        ArrayList<Long> FilteredRRInterval = new ArrayList<>();

        int upperBound;
        int lowerBound;
        upperBound = Math.min(1300, (int) (1.2 * 60 / hr * 1000));
        lowerBound = Math.max(400, (int) (0.8 * 60 / hr * 1000));
        for (int i = 0; i < rrIntervals.size(); i++) {
            long interval = rrIntervals.get(i);
            if (interval > lowerBound && interval < upperBound) {
                FilteredRRInterval.add(interval);
            }

        }


        if (FilteredRRInterval.isEmpty()) return 0.0;
        double mean = FilteredRRInterval.stream().mapToDouble(a -> a).average().orElse(0.0);
        ResultVitalSign.vitalSignData.IBI_mean = mean;
        ResultVitalSign.vitalSignData.IBI_HR = 1000 / mean * 60;
        double meanOfSquaredDifferences = FilteredRRInterval.stream().mapToDouble(a -> a - mean).map(a -> a * a).average().orElse(0.0);
        Log.d("vital", "IBI : " + mean + "IBI-HR : " + 60000/mean);
        return Math.sqrt(meanOfSquaredDifferences);
    }
    public double spo2(double[] pixel_R,
                       double[] pixel_G,
                       double[] pixel_B,
                       int VIDEO_FRAME_RATE){
        Butterworth butterworth = new Butterworth((double)VIDEO_FRAME_RATE/2);
        double scale = 10000;

        double[] HbO2 = MathArrays.ebeAdd(MathArrays.ebeAdd(
                MathArrays.scale(0.13214285714285712, pixel_R), MathArrays.scale(0.11964285714285716, pixel_G)), MathArrays.scale(-0.251785714 , pixel_B));
        double[] Hb = MathArrays.ebeAdd(MathArrays.ebeAdd(
                MathArrays.scale(-0.019642857, pixel_R), MathArrays.scale(-0.0125, pixel_G)), MathArrays.scale(0.032142857, pixel_B));

        if(new PearsonsCorrelation().correlation(HbO2, Hb) < 0){
            Hb = MathArrays.scale(-1, Hb);
        }

        HbO2 = butterworth.bandPassFilter(HbO2, 6 , Config.SPO2_LOW_CUTOFF_FREQUENCY, Config.SPO2_HIGH_CUTOFF_FREQUENCY);
        Hb = butterworth.bandPassFilter(Hb, 6 , Config.SPO2_LOW_CUTOFF_FREQUENCY, Config.SPO2_HIGH_CUTOFF_FREQUENCY);

        Detrend det_HbO2 = new Detrend(HbO2, 6);
        Detrend det_Hb = new Detrend(Hb, 6);

        HbO2 = det_HbO2.detrendSignal();
        Hb = det_Hb.detrendSignal();

        double hbo2_mean = DoubleUtils.calculateMean(HbO2);
        double hb_mean = DoubleUtils.calculateMean(Hb);

        for(int i = 0; i < HbO2.length; i++){
            HbO2[i] = (HbO2[i] - hbo2_mean);
            Hb[i] = (Hb[i] - hb_mean);
        }

        ArrayList<ArrayList<Integer>> peak_N_valley_HbO2 = get_peak_idx(HbO2, 96);
        ArrayList<ArrayList<Integer>> peak_N_valley_Hb = get_peak_idx(Hb, 96);

        ArrayList<Integer> commonPeaks = RppgUtils.intersect1d(peak_N_valley_HbO2.get(0), peak_N_valley_Hb.get(0));
        ArrayList<Integer> commonValleys = RppgUtils.intersect1d(peak_N_valley_HbO2.get(1), peak_N_valley_Hb.get(1));

        Map<Integer, Integer> closestMap = RppgUtils.closestPairs(commonPeaks, commonValleys);

        ArrayList<Integer> filtered_peaks = new ArrayList<>();
        ArrayList<Integer> filtered_valleys = new ArrayList<>();
        if(!closestMap.entrySet().isEmpty()){
            for(Map.Entry<Integer, Integer> item : closestMap.entrySet()){
                filtered_peaks.add(item.getKey());
                filtered_valleys.add(item.getValue());
            }
        }

        if(filtered_peaks.isEmpty() || filtered_valleys.isEmpty()) {
            return 0;
        }

        double baseline_correction = Math.abs(
                Math.min(DoubleUtils.findMin(HbO2), DoubleUtils.findMin(Hb))) + 1;

        ArrayList<Double> p_power = new ArrayList<>();
        ArrayList<Double> v_power = new ArrayList<>();

        for(int peak : filtered_peaks){
            p_power.add(HbO2[peak] + baseline_correction);
        }
        for(int valley : filtered_valleys){
            v_power.add(HbO2[valley] + baseline_correction);
        }
        double[] divide_HbO2_pv = MathArrays.ebeDivide(p_power.stream().mapToDouble(Double:: doubleValue).toArray()
                , v_power.stream().mapToDouble(Double:: doubleValue).toArray());
        double[] divide_HbO2_log_pv = new double[divide_HbO2_pv.length];

        for(int i = 0; i < divide_HbO2_pv.length; i++){
            divide_HbO2_log_pv[i] = log10(divide_HbO2_pv[i]);
        }

        p_power.clear(); v_power.clear();

        for(int peak : filtered_peaks){
            p_power.add(Hb[peak] + baseline_correction);
        }
        for(int valley : filtered_valleys){
            v_power.add(Hb[valley] + baseline_correction);
        }

        double[] divide_Hb_pv = MathArrays.ebeDivide(p_power.stream().mapToDouble(Double:: doubleValue).toArray()
                , v_power.stream().mapToDouble(Double:: doubleValue).toArray());
        double[] divide_Hb_log_pv = new double[divide_HbO2_pv.length];

        for(int i = 0; i < divide_Hb_pv.length; i++){
            divide_Hb_log_pv[i] = log10(divide_Hb_pv[i]);
        }

        double RoR = RppgUtils.getSpO2RoR(divide_HbO2_log_pv, divide_Hb_log_pv);

        double spo2 = -0.7836 * RoR + 102.4280;
        if(spo2 > 100){
            spo2 = 100;
        }
        return spo2;
    }
    public double spo2_lagacy(double[] spo2_pixel_buff_R, double[] spo2_pixel_buff_B, int VIDEO_FRAME_RATE) {

        //---------BPF_FILTER-----------//
        double[] R_kernel = RppgUtils.bandPassKernel(VIDEO_FRAME_RATE, 0.3d / (VIDEO_FRAME_RATE / 2), 2.5 / (VIDEO_FRAME_RATE / 2)); //BPF 생성 0.3/15~ 2.5/15사이
        double[] B_kernel = RppgUtils.bandPassKernel(VIDEO_FRAME_RATE, 0.3d / (VIDEO_FRAME_RATE / 2), 2.5 / (VIDEO_FRAME_RATE / 2));
        double[] R_result = RppgUtils.filter(spo2_pixel_buff_R, R_kernel); //BPF 통과_ 특정 주파수의 색만 남는다고함..주파수( 색의 파장 )
        double[] B_result = RppgUtils.filter(spo2_pixel_buff_B, B_kernel);
        double R_reult_mean = 0;
        double B_reult_mean = 0;
        //--------SIGNAL SMOOTHING---// --> BPF의 평균값 제거를 통한 노이즈 제거
        for (double r_tmp : R_result) {
            R_reult_mean += r_tmp;
        }
        for (double b_tmp : B_result) {
            B_reult_mean += b_tmp;
        }
        R_reult_mean /= VIDEO_FRAME_RATE;
        B_reult_mean /= VIDEO_FRAME_RATE;
        for (int i = 0; i < R_result.length; i++) {
            R_result[i] -= R_reult_mean;
        }
        for (int i = 0; i < B_result.length; i++) {
            B_result[i] -= B_reult_mean;
        }
        //-------SECONDARY BPF(ButterWorth)-----// --> ButterWorth 필터로 2차 필터링 통과대역외의 일정 부분을 살리기 위함
        uk.me.berndporr.iirj.Butterworth butterworth = new uk.me.berndporr.iirj.Butterworth();
        butterworth.bandPass(9, VIDEO_FRAME_RATE, 0.1, 0.1);
        for (int i = 0; i < R_result[i]; i++) {
            R_result[i] = butterworth.filter(R_result[i]);
        }
        for (int i = 0; i < B_result[i]; i++) {
            B_result[i] = butterworth.filter(B_result[i]);
        }
        //----ENVELOPE 신호 포락선(상부, 하부 포함) ------// --> 필터링 결과의 포락선 제거
        Hilbert hilbert_R = new Hilbert(R_result);
        hilbert_R.transform();
        double[] analytical_signal_R = hilbert_R.getAmplitudeEnvelope(); //--> Hilbert transform이 각 신호의 othogonal한 성분을 뽑아줌으로 결과 값 자체가 envelope한 신호임
        double R_envelope_mean = 0.0d;
        for (int i = 0; i < analytical_signal_R.length; i++) {
            R_envelope_mean += analytical_signal_R[i];
        }
        R_envelope_mean /= analytical_signal_R.length; // 포락선의 평균 측정


        Hilbert hilbert_B = new Hilbert(R_result);
        hilbert_B.transform();
        double[] analytical_signal_B = hilbert_R.getAmplitudeEnvelope();
        Log.d("A", "AA");
        double B_envelope_mean = 0.0d;
        for (int i = 0; i < analytical_signal_R.length; i++) {
            B_envelope_mean += analytical_signal_R[i];
        }
        B_envelope_mean /= analytical_signal_B.length;

        //--- DETREND ---//{ 확인 필요 }
        //--R
        Detrend d_R = new Detrend(R_result, DETREND_POWER);
        double[] out_R = d_R.detrendSignal();

        ArrayList<Double> r_list = new ArrayList<Double>();
        for (int i = 0; i < out_R.length; i++) {
            if (abs(out_R[i]) < R_envelope_mean)
                r_list.add(R_envelope_mean);
        }
        //--B
        Detrend d_B = new Detrend(B_result, DETREND_POWER);
        double[] out = d_B.detrendSignal();

        ArrayList<Double> b_list = new ArrayList<Double>();
        for (int i = 0; i < out.length; i++) {
            if (abs(out[i]) < B_envelope_mean)
                b_list.add(B_envelope_mean);
        }

        //---BANDY FILTER---//
        double[] banddy_r = new double[r_list.size()];
        for (int i = 0; i < r_list.size(); i++) {
            banddy_r[i] = r_list.get(i) * r_banddy;
        }

        double[] banddy_b = new double[b_list.size()];
        for (int i = 0; i < b_list.size(); i++) {
            banddy_b[i] = b_list.get(i) * b_banddy;
        }

        //                            Hilbert hilbert = new Hilbert(R_result);
//                            hilbert.getOutput();
        //--- SpO2 estimate ---//
        //--- DFT ---//
        DiscreteFourier fft_r = new DiscreteFourier(banddy_r);
        fft_r.transform();
        double[] out_dft_r_real = fft_r.getMagnitude(true);
        double[][] out_dft_r = fft_r.getComplex2D(true);

        DiscreteFourier fft_b = new DiscreteFourier(banddy_b);
        fft_b.transform();
        double[] out_dft_b_real = fft_b.getMagnitude(true);
        double[][] out_dft_b = fft_b.getComplex2D(true);

        //--- Find Peak  & DEVIDED---//
        if (out_dft_r_real.length == 0) return 0.0;
        FindPeak fp_r = new FindPeak(out_dft_r_real);
        Peak out_r = fp_r.detectPeaks();
        int[] peaks_r = out_r.getPeaks();
        double[] through_dft_r = new double[peaks_r.length];
        for (int i = 0; i < peaks_r.length; i++) {
            through_dft_r[i] = abs(out_dft_r_real[peaks_r[i]] / out_dft_r_real[0]);
        }

        if (out_dft_b_real.length == 0) return 0.0;
        FindPeak fp_b = new FindPeak(out_dft_b_real);
        Peak out_b = fp_b.detectPeaks();
        int[] peaks_b = out_b.getPeaks();
        double[] through_dft_b = new double[peaks_b.length];
        for (int i = 0; i < peaks_b.length; i++) {
            through_dft_b[i] = abs(out_dft_b_real[peaks_b[i]] / out_dft_b_real[0]);
        }
        //--- CAL SPO2 RAW ---//
        int spo2_len = 0, spo2_cnt = 0;
        double spo2 = 0.0d;
        spo2_len = Math.min(through_dft_r.length, through_dft_b.length);
        for (int i = 0; i < spo2_len; i++) {
            double spo2_raw_tmp = 96.58d - -0.015 * through_dft_r[i] / through_dft_b[i] * 100;
            if (spo2_raw_tmp < 100) {
                spo2 += spo2_raw_tmp;
                spo2_cnt++;
            }
        }

        spo2 /= spo2_cnt;

        return spo2;

    }


    public static ResultVitalSign toResultVitalSign(Result result) {
        ResultVitalSign convert = new ResultVitalSign();
        convert.HR = result.HR_result;
        convert.RR = result.RR_result;
        convert.BP = result.BP;
        convert.DBP = result.DBP;
        convert.SBP = result.SBP;
        convert.STRESS = result.LF_HF_ratio;
        convert.HRV = result.sdnn_result;
        convert.SpO2 = result.spo2_result;
        convert.IBI_HR = ResultVitalSign.vitalSignData.IBI_HR;
        convert.IBI_mean = ResultVitalSign.vitalSignData.IBI_mean;
        return convert;
    }

    private int[] getAdjustPixel(Bitmap bitmap, int x, int y) {
        int[] adjustPixel = new int[9];
        if (x >= 1) {
            if (y >= 1) {
                adjustPixel[0] = bitmap.getPixel(x - 1, y - 1);
                adjustPixel[6] = bitmap.getPixel(x - 1, y + 1);
                adjustPixel[3] = bitmap.getPixel(x - 1, y);
                adjustPixel[1] = bitmap.getPixel(x, y - 1);
                adjustPixel[4] = bitmap.getPixel(x, y);
                adjustPixel[7] = bitmap.getPixel(x, y + 1);
                adjustPixel[2] = bitmap.getPixel(x + 1, y - 1);
                adjustPixel[5] = bitmap.getPixel(x + 1, y);
                adjustPixel[8] = bitmap.getPixel(x + 1, y + 1);
            }
        }
        return adjustPixel;
    }

    public void clearAnalysis() {
        firstFrameTime = 0;
        lastFrameTime = 0;
        rPPG = new Rppg(BUFFER_SIZE);
        bufferIndex = 0;
        pixelIndex = 0;
    }
}
