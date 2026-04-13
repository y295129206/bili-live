package com.bililive.web.component;
import com.bililive.component.RedisComponent;
import com.bililive.entity.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpireationListener extends KeyExpirationEventMessageListener{
    @Autowired
    private RedisComponent redisComponent;
    public RedisKeyExpireationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }
    @Override
    public void onMessage(Message message, byte[] pattern){
        String key = message.toString();
        if(!key.startsWith(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX+Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX)){
            return;
        }
        Integer userKeyIndex = key.indexOf(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX)+Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX.length();
        String fileId = key.substring(userKeyIndex,userKeyIndex+20);
        redisComponent.decrementPlayOnlineCount(String.format(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE,fileId));
    }
    public static void main(String[] args){
        System.out.println(Constants.REDIS_KEY_VIDEO_PLAY_COUNT_ONLINE_PREFIX+Constants.REDIS_KEY_VIDEO_PLAY_COUNT_USER_PREFIX);
    }

}
