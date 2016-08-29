package com.huawei.espace.module.conference.logic;

/**
 * 
 * 会议模块逻辑类
 * @author yKF68604
 * @date 2012-6-18 下午7:06:38
 */
public class ConferenceLogic
{
    /**
     * 新建会议场景，页面跳转到会议中页面的请求码
     */
    public static final int REQUEST_CODE_FOR_SKIP_CONF_MANAGE_ACTIVITY = 0x01;
    /**
     * 新建会议场景，页面跳转到会议时间选择页面的请求码
     */
    public static final int REQUEST_CODE_FOR_SKIP_CONF_DATE_ACTIVITY = 0x02;
    /**
     * 新建会议跳转会议中页面，传递resultID的key
     */
    public static final String CONFERENCE_RESULT_ID = "conference_result_id";
    /**
     * 新建会议跳转会议中页面，传递subject的key
     */
    public static final String CONFERENCE_SUBJECT = "conference_subject";
    public static final String CONFERENCE_ESPACENUMBER = "conference_eSpaceNumber";
    /**
     * 服务器返回状态描述信息key
     */
    public static final String KEY_RESPONSE_DESC = "key_response_failed";
    /**
     * 服务器返回的响应码key
     */
    public static final String KEY_RESPONSE_CODE = "key_response_code";
    
    public static final String CONFERENCE_MEMBER_LIST = "conference_member_list";
}