package kr.co.wincom.sjc.dto;

public class ResultDto {

    private String resData;
    private String errorMsg;

    public ResultDto(String resData, String errorMsg) {
        this.resData = resData;
        this.errorMsg = errorMsg;
    }

    public String getResData() {
        return resData;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "ResultDto [" +
            "resData='" + resData + '\'' +
            ", errorMsg='" + errorMsg + '\'' +
            ']';
    }
}
