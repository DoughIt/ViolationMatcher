package cn.edu.fudan.violationtracker.entity.dto;

import cn.edu.fudan.violationtracker.entity.dbo.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @date 2023/11/27 15:16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMatchResult {

    String matchedLocationId;
    Boolean bestMatch;
    Double matchingDegree;

    Location location;

    public static LocationMatchResult newInstance(Location location2, double matchDegree) {
        return LocationMatchResult.builder().location(location2)
                .matchedLocationId(location2.getUuid()).matchingDegree(matchDegree).build();
    }
}
