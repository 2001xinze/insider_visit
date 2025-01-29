package com.dianping.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String city;
    private String introduce;
    private Integer fans;
    private Integer followee;
    /**
     * 性别，0：男，1：女
     */
    private Boolean gender;
    private LocalDate birthday;
    /**
     * 积分
     */
    private Integer credits;
    /**
     * 会员级别，0~9级,0代表未开通会员
     */
    private Boolean level;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
