package com.bililive.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程执行工具类
 * 用于执行系统命令
 */
@Slf4j
public class ProcessUtils {

    /**
     * 执行系统命令
     * @param command 命令字符串
     * @param showLog 是否显示日志
     * @return 命令执行输出结果，失败返回 null
     */
    public static String executeCommand(String command, Boolean showLog) {
        try {
            if (showLog == null) {
                showLog = false;
            }
            
            log.info("执行命令：{}", command);
            
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // 读取输出日志
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (showLog) {
                    log.debug("命令输出：{}", line);
                }
                result.append(line);
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("命令执行成功");
                return result.toString();
            } else {
                log.error("命令执行失败，退出码：{}", exitCode);
                return null;
            }
        } catch (Exception e) {
            log.error("执行命令异常", e);
            return null;
        }
    }

    /**
     * 执行系统命令（不显示日志）
     * @param command 命令字符串
     * @return 命令执行输出结果，失败返回 null
     */
    public static String executeCommand(String command) {
        return executeCommand(command, false);
    }
}
