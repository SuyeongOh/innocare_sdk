package com.innopia.vital_sync.video;

import android.content.Context;

import com.innopia.vital_sync.VitalLagacy;
import com.innopia.vital_sync.data.Constant;
import com.innopia.vital_sync.data.ResultVitalSign;
import com.innopia.vital_sync.utils.CsvFileReader;
import com.innopia.vital_sync.utils.CsvFileWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class PureDataset {

    private Context mContext;
    private final String PureCSVHeader =  "report_pure_";
    private final String PureLabelName = "PURE_W72_H72_Length600_TEST.csv";
    private final String[] Header = new String[]{
        "subject_number", "chunk_index",
        "fft_hr_label", "fft_hr_test", "fft_hr_error",
        "ibi_hr_label", "ibi_hr_test", "ibi_hr_error",
        "mean_ibi_label", "mean_ibi_test", "mean_ibi_error",
        "sdnn_label", "sdnn_test", "sdnn_error"
    };

    public PureDataset(Context context){
        mContext = context;
    }

    public void run(String fileName){
        String id = fileName.split("_")[2];
        String slice = fileName.split("_")[4].split("\\.")[0];

        CommonEvaluationMetrics measure = new CommonEvaluationMetrics();
        measure.subject_id = id;
        measure.slice_id = slice;
        measure.label_ibi_hr = ResultVitalSign.vitalSignData.IBI_HR;
        measure.label_fft_hr = ResultVitalSign.vitalSignData.HR_result;
        measure.label_ibi_hrv = ResultVitalSign.vitalSignData.sdnn_result;
        measure.label_ibi_mean = ResultVitalSign.vitalSignData.IBI_mean;
        calculateError(measure);
    }

    private void calculateError(CommonEvaluationMetrics measure){
        CommonEvaluationMetrics labelEvaluation = getEvaluationMetrics(measure.subject_id, measure.slice_id);

        double error_ibi_hr = measure.label_ibi_hr - labelEvaluation.label_ibi_hr;
        double error_ibi_hrv = measure.label_ibi_hrv - labelEvaluation.label_ibi_hrv;
        double error_fft_hr = measure.label_fft_hr - labelEvaluation.label_fft_hr;
        double error_ibi_mean = measure.label_ibi_mean - labelEvaluation.label_ibi_mean;

        //오늘 날짜 report_pure_yyyymmddHHmm.csv
        //만약 존재하면 append, 없으면 신규생성
        // 현재 날짜와 시간을 가져옵니다.
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault());
        String formattedDate = sdf.format(now);

        String fileName = PureCSVHeader + formattedDate + Constant.CSV_FOOTER;
        CsvFileWriter.writeCsvFile(mContext, fileName);
    }

    private CommonEvaluationMetrics getEvaluationMetrics(String id, String slice){
        ArrayList<String[]> labels = new ArrayList<>();
        labels.addAll(CsvFileReader.readCsvFromAssets(mContext, PureLabelName));

        CommonEvaluationMetrics metrics = new CommonEvaluationMetrics();
        for(String[] label : labels){
            if(label[0].contains(id) && label[1].equals(slice)){
                metrics.label_fft_hr = Float.parseFloat(label[2]);
                metrics.label_ibi_hr = Float.parseFloat(label[3]);
                metrics.label_ibi_mean = Float.parseFloat(label[4]);
                metrics.label_ibi_hrv = Float.parseFloat(label[5]);
            }
        }
        return metrics;
    }
}
