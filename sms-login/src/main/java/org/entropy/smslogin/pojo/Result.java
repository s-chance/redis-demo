package org.entropy.smslogin.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private String msg;
    private T data;

    public static <E> Result<E> success(E data) {
        return new Result<>("操作成功", data);
    }

    public static <E> Result<E> failure(E data) {
        return new Result<>("操作失败", data);
    }
}
