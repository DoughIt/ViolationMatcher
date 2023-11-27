package cn.edu.fudan.violationtracker.core.matcher;


import cn.edu.fudan.violationtracker.core.process.RawIssueMatcher;
import cn.edu.fudan.violationtracker.entity.dbo.RawIssue;
import cn.edu.fudan.violationtracker.util.JavaAstParserUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @date 2023/11/27 15:05
 */
public interface Matcher {

    /**
     * 匹配两个 raw issue list
     *
     * @param preRawIssues preRawIssues
     * @param curRawIssues curRawIssues
     * @param repoPath     repoPath
     */
    default void mapRawIssues(List<RawIssue> preRawIssues, List<RawIssue> curRawIssues, String repoPath) {
        Map<String, List<RawIssue>> preRawIssueMap = preRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));
        Map<String, List<RawIssue>> curRawIssueMap = curRawIssues.stream().collect(Collectors.groupingBy(RawIssue::getFileName));

        preRawIssueMap.entrySet().stream()
                .filter(e -> curRawIssueMap.containsKey(e.getKey()))
                .forEach(
                        pre -> {
                            Set<String> methodsAndFields = Collections.emptySet();
                            try {
                                methodsAndFields =
                                        JavaAstParserUtil.getAnchors(repoPath + File.separator + pre.getKey());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            RawIssueMatcher.match(
                                    pre.getValue(), curRawIssueMap.get(pre.getKey()), methodsAndFields);
                        });
    }

    /**
     * 为匹配文件重命名预处理
     *
     * @param preRawIssues preRawIssues
     * @param map          map
     */
    default void renameHandle(List<RawIssue> preRawIssues, Map<String, String> map) {
        preRawIssues.stream()
                .filter(r -> map.containsKey(r.getFileName()))
                .forEach(rawIssue -> {
                    rawIssue.getLocations().forEach(location -> location.setFilePath(map.get(rawIssue.getFileName())));
                    rawIssue.setFileName(map.get(rawIssue.getFileName()));
                });
    }

}
