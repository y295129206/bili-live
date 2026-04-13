package com.bililive.web.controller;

import com.bililive.component.RedisComponent;
import com.bililive.pojo.Result;
import com.bililive.pojo.SysSettingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sysSetting")
@Slf4j
@Validated
public class SysSettingController {
    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/getSetting")
    public Result<SysSettingDto> getSetting() {
        return Result.success(redisComponent.getSysSettingDto());
    }

}
