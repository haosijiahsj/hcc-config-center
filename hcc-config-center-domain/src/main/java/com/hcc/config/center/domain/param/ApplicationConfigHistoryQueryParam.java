package com.hcc.config.center.domain.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ApplicationConfigHistoryQueryParam
 *
 * @author shengjun.hu
 * @date 2022/10/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ApplicationConfigHistoryQueryParam extends PageParam {

    private Long applicationConfigId;

}
