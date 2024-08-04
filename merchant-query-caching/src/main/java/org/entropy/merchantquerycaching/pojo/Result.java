package org.entropy.merchantquerycaching.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private String msg;
    private T data;

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(msg, data);
    }

    public static Result<Void> failure(String msg) {
        return new Result<>(msg, null);
    }
}
