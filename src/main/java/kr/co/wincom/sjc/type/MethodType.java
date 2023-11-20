package kr.co.wincom.sjc.type;

public enum MethodType {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    DELETE("DELETE");

    private final String code;

    MethodType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
