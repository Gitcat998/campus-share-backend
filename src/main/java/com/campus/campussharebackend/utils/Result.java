package com.campus.campussharebackend.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;  // 状态码：200成功，500失败
    private String msg;    // 消息
    private T data;        // 数据

    // 成功（无数据）
    public static Result success(String msg) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg(msg);
        return result;
    }

    // 成功（有数据，默认消息）
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 新增：成功（有数据+自定义消息）
    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    // 失败
    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}