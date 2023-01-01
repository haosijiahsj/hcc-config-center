package com.hcc.config.center.domain.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * PushConfigNodeDataVo
 *
 * @author shengjun.hu
 * @date 2022/10/19
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PushConfigClientMsgVo extends BaseMsg {

    private String key;
    private String value;
    private Integer version;
    private Boolean forceUpdate;

}
