package com.teachingplatform.util;

public class Result {
    private int code;
    private String message;
    private Object data;

    private Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static Result ok(Object data) {
        return new Result(200, "ok", data);
    }

    public static Result ok() {
        return new Result(200, "ok", null);
    }

    public static Result error(int code, String message) {
        return new Result(code, message, null);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}
