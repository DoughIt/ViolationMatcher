package cn.edu.fudan.violationtracker.util;

import cn.edu.fudan.violationtracker.entity.dbo.Location;
import cn.edu.fudan.violationtracker.entity.dbo.LogicalStatement;
import cn.edu.fudan.violationtracker.entity.dbo.RawIssue;
import cn.edu.fudan.violationtracker.util.stat.LogicalStatementUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @desc
 * @date 2023/11/27 14:09
 */

@Slf4j
public class AnalyzerUtil {
    private AnalyzerUtil() {
        // utility class
    }

    public static void addExtraAttributeInRawIssues(List<RawIssue> tempRawIssues, String scanRepoPath) {
        final Map<String, List<RawIssue>> file2RawIssuesMap = tempRawIssues.stream().collect(Collectors.groupingBy(rawIssue -> rawIssue.getLocations().get(0).getSonarRelativeFilePath()));
        for (Map.Entry<String, List<RawIssue>> file2RawIssues : file2RawIssuesMap.entrySet()) {
            final List<RawIssue> rawIssues = file2RawIssues.getValue();
            final List<Location> locations = rawIssues.stream().map(RawIssue::getLocations).flatMap(Collection::stream).collect(Collectors.toList());
            final List<Integer> beginLines = locations.stream().map(Location::getStartLine).collect(Collectors.toList());
            final List<Integer> endLines = locations.stream().map(Location::getEndLine).collect(Collectors.toList());
            final List<Integer> startTokens = locations.stream().map(Location::getStartToken).collect(Collectors.toList());
            String realFilePath = scanRepoPath + File.separator + locations.get(0).getSonarRelativeFilePath();

            log.debug("cur file  {}, rawIssueTotal is {}", realFilePath, rawIssues.size());
            try {
                List<LogicalStatement> logicalStatements = LogicalStatementUtil.getLogicalStatements(realFilePath, beginLines, endLines, startTokens);
                List<String> codeList = logicalStatements.stream().map(LogicalStatement::getContent).collect(Collectors.toList());
                List<String> anchorNameList = logicalStatements.stream().map(LogicalStatement::getAnchorName).collect(Collectors.toList());
                List<Integer> anchorOffsetList = logicalStatements.stream().map(LogicalStatement::getAnchorOffset).collect(Collectors.toList());
                List<String> classNameList = logicalStatements.stream().map(LogicalStatement::getClassName).collect(Collectors.toList());
                for (int i1 = 0; i1 < locations.size(); i1++) {
                    final Location location = locations.get(i1);
                    location.setAnchorName(anchorNameList.get(i1));
                    location.setOffset(anchorOffsetList.get(i1));
                    location.setClassName(classNameList.get(i1));
                    location.setCode(codeList.get(i1));
                }
            } catch (Exception e) {
                log.error("parse file {} failed! rawIssue num is {}", realFilePath, rawIssues.size());
                log.error("parse message: {} ...", StringsUtil.firstLine(e.getMessage()));
            }
        }
    }
}
