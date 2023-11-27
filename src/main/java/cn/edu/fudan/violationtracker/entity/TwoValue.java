package cn.edu.fudan.violationtracker.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @desc 工具类 pojo  用来存储只有两个值的对象
 * @date 2023/11/27 15:22
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TwoValue<T1, T2> {
    T1 first;
    T2 second;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TwoValue<?, ?> twoValue = (TwoValue<?, ?>) o;
        return Objects.equals(first, twoValue.first) && Objects.equals(second, twoValue.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
