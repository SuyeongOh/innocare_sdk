package com.innopia.vital_sync.video;

import android.content.Context;

import com.innopia.vital_sync.data.Constant;
import com.innopia.vital_sync.data.ResultVitalSign;
import com.innopia.vital_sync.utils.CsvFileReader;
import com.innopia.vital_sync.utils.CsvFileWriter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PureDataset {

    private Context mContext;
    private final String PureCSVHeader =  "report_pure_";
    private final String PureLabelName = "PURE_W72_H72_Length600_TEST.csv";
    private final String DatasetHeader = "pure";
    private final String[] Header = new String[]{
        "subject_number", "chunk_index",
        "fft_hr_label", "fft_hr_test", "fft_hr_error",
        "ibi_hr_label", "ibi_hr_test", "ibi_hr_error",
        "mean_ibi_label", "mean_ibi_test", "mean_ibi_error",
        "sdnn_label", "sdnn_test", "sdnn_error",
        "ibi_fft_error"
    };

    public PureDataset(Context context){
        mContext = context;
    }

    public void run(String fileName){
        String id = fileName.split("_")[2];
        String slice = fileName.split("_")[4].split("\\.")[0];

        CommonEvaluationMetrics analysis = new CommonEvaluationMetrics();
        analysis.subject_id = id;
        analysis.slice_id = slice;
        analysis.label_ibi_hr = ResultVitalSign.vitalSignData.IBI_HR;
        analysis.label_fft_hr = ResultVitalSign.vitalSignData.HR_result;
        analysis.label_ibi_hrv = ResultVitalSign.vitalSignData.sdnn_result;
        analysis.label_ibi_mean = ResultVitalSign.vitalSignData.IBI_mean;
        calculateError(analysis);
    }

    private void calculateError(CommonEvaluationMetrics analysis){
        CommonEvaluationMetrics labelEvaluation = getEvaluationMetrics(analysis.subject_id, analysis.slice_id);

        double error_ibi_hr = Math.abs(analysis.label_ibi_hr - labelEvaluation.label_ibi_hr);
        double error_ibi_hrv = Math.abs(analysis.label_ibi_hrv - labelEvaluation.label_ibi_hrv);
        double error_fft_hr = Math.abs(analysis.label_fft_hr - labelEvaluation.label_fft_hr);
        double error_ibi_mean = Math.abs(analysis.label_ibi_mean - labelEvaluation.label_ibi_mean);

        CommonEvaluationMetrics error = new CommonEvaluationMetrics();
        error.label_ibi_hr = error_ibi_hr;
        error.label_ibi_hrv = error_ibi_hrv;
        error.label_fft_hr = error_fft_hr;
        error.label_ibi_mean = error_ibi_mean;

        writePureResult(labelEvaluation, analysis, error);
    }

    private void writePureResult(CommonEvaluationMetrics label,
                                    CommonEvaluationMetrics analysis,
                                    CommonEvaluationMetrics error){
        //오늘 날짜 report_pure_yyyymmddHHmm.csv
        //만약 존재하면 append, 없으면 신규생성
        // 현재 날짜와 시간을 가져옵니다.
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String formattedDate = sdf.format(now);

        String fileName = PureCSVHeader + formattedDate + Constant.CSV_FOOTER;
        ArrayList<String[]> inputData = new ArrayList<>();
        File csvFile = new File(mContext.getFilesDir(), fileName);
        if(!csvFile.exists()){
            inputData.add(Header);
        }
        ArrayList<String> inputLine = new ArrayList<>();
        inputLine.add(label.subject_id);
        inputLine.add(label.slice_id);
        for(int i = 0; i < label.getValueStringArray().length; i++){
            inputLine.add(label.getValueStringArray()[i]);
            inputLine.add(analysis.getValueStringArray()[i]);
            inputLine.add(error.getValueStringArray()[i]);
        }
        double ibi_fft_err = Math.abs(analysis.label_fft_hr - analysis.label_ibi_hr);
        inputLine.add(Double.toString(ibi_fft_err));
        inputData.add(inputLine.toArray(new String[inputLine.size()]));

        CsvFileWriter.writeCsvFile(mContext, fileName, inputData);
    }

    private CommonEvaluationMetrics getEvaluationMetrics(String id, String slice){
        ArrayList<String[]> labels = new ArrayList<>();
        labels.addAll(CsvFileReader.readCsvFromAssets(mContext, DatasetHeader + "/" + PureLabelName));

        CommonEvaluationMetrics metrics = new CommonEvaluationMetrics();
        for(String[] label : labels){
            if(label[0].contains(id) && label[1].equals(slice)){
                metrics.subject_id = label[0];
                metrics.slice_id = label[1];
                metrics.label_fft_hr = Float.parseFloat(label[2]);
                metrics.label_ibi_hr = Float.parseFloat(label[3]);
                metrics.label_ibi_mean = Float.parseFloat(label[4]);
                metrics.label_ibi_hrv = Float.parseFloat(label[5]);
            }
        }
        return metrics;
    }
}
