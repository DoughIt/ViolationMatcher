package cn.edu.fudan.violationtracker.entity.dto;

import cn.edu.fudan.violationtracker.entity.dbo.RawIssue;
import lombok.Data;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @date 2023/11/27 15:20
 */
@Data
public class RawIssueMatchResult {

    /**
     * 两个raw issue 是否完全一样
     */
    boolean isBestMatch = false;

    /**
     * 两个rawIssue的匹配度
     */
    double matchingDegree;

    /**
     * 匹配到的RawIssue
     */
    RawIssue rawIssue;


    private RawIssueMatchResult() {
    }

    public static RawIssueMatchResult newInstance(RawIssue rawIssue, double matchDegree) {
        RawIssueMatchResult result = new RawIssueMatchResult();
        result.setRawIssue(rawIssue);
        result.setMatchingDegree(matchDegree);
        return result;
    }
}
