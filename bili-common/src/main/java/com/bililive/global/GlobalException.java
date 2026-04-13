package com.bililive.global;
import com.bililive.pojo.BaseException;
import com.bililive.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class GlobalException{
    @ExceptionHandler
    public Result handleException(BaseException ex){
        log.error(ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
