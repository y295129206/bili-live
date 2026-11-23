package com.bililive.entity.constants;

public class Constants {
    //正则表达式。限制密码
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";

    /**
     * QQ 邮箱正则：仅接受数字@qq.com（注册邮箱验证码功能要求只支持 QQ 邮箱）
     * 使用 ^...$ 严格匹配整个字符串，避免 xxx@qq.com.evil.com 这类绕过
     */
    public static final String REGEX_QQ_EMAIL = "^[1-9]\\d{4,10}@qq\\.com$";

    public static final Integer ONE=1;

    public static final Integer ZERO=0;

    public static final Integer LENGTH_10 = 10;

    public static final Integer REDIS_KEY_EXPIRES_ONE_SECONDS = 1000;

    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60000;

    public static final Integer REDIS_KEY_EXPIRES_ONE_DAY = REDIS_KEY_EXPIRES_ONE_MIN*24*60;

    /** 邮箱验证码有效期：5 分钟（毫秒） */
    public static final Integer EMAIL_CODE_EXPIRE = REDIS_KEY_EXPIRES_ONE_MIN * 5;

    /** 邮箱验证码发送间隔：60 秒（毫秒），用于限流 */
    public static final Integer EMAIL_CODE_SEND_INTERVAL = REDIS_KEY_EXPIRES_ONE_MIN;

    /** 邮箱验证码连续输错的最大次数，达到后验证码作废 */
    public static final Integer EMAIL_CODE_MAX_FAIL_COUNT = 5;

    public static final Integer UPDATE_NICK_NAME_COIN = 5;

    public static final String REDIS_KEY_PREFIX= "bililive:";

    public static String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX+"checkCode:";

    /** 邮箱验证码：值为 EmailCodeDto（验证码 + 失败次数），TTL 5 分钟 */
    public static String REDIS_KEY_EMAIL_CODE = REDIS_KEY_PREFIX+"emailCode:";

    /** 邮箱验证码发送限流：value 为发送时间戳，TTL 60 秒 */
    public static String REDIS_KEY_EMAIL_CODE_LIMIT = REDIS_KEY_PREFIX+"emailCodeLimit:";

    public static String REDIS_KEY_UPLOADING_FILE = REDIS_KEY_PREFIX+"uploadingFile:";

    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX+"token:web:";

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX+"token:admin:";

    public static final String REDIS_KEY_SYS_SETTING = REDIS_KEY_PREFIX+"sysSetting";

    public static final String REDIS_KEY_FILE_DEL = REDIS_KEY_PREFIX+"file:list:del:";

    public static final String REDIS_KEY_QUEUE_TRANSFER = REDIS_KEY_PREFIX+"queue:transfer:";

    public static final String TOKEN_WEB = "token";

    public static final String TOKEN_ADMIN = "admintoken";

    public static final String FILE_FOLDER = "file/";

    public static final String FILE_VIDEO = "video/";

    public static final String FILE_COVER = "cover/";

    public static final String FILE_FOLDER_TEMP = "temp/";

    public static final String TEMP_VIDEO_NAME = "/temp.mp4";

    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail.jpg";

    public static final String VIDEO_CODE_HEVC = "hevc";

    public static final String VIDEO_CODE_TEMP_FILE_SUFFIX = "_temp";

    public static final String TS_NAME = "index.ts";

    public static final String M3U8_NAME = "index.m3u8";

    //视频在线
    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX = REDIS_KEY_PREFIX+"video:play:online:";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX+"count:%s";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX = "user:";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT_USER = REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX+REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX+"%s:%s";

    public static final String REDIS_KEY_VIDEO_SEARCH_COUNT = REDIS_KEY_PREFIX+"video:search:";

    public static final String REDIS_KEY_QUEUE_VIDEO_PLAY = REDIS_KEY_PREFIX+"queue:video:play:";

    public static final String REDIS_KEY_VIDEO_PLAY_COUNT = REDIS_KEY_PREFIX+"video:playcount:";

}
