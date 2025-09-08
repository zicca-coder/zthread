package com.zicca.zthread.config.common.starter.configuration;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;
import org.springframework.boot.info.BuildProperties;

/**
 * 启动时打印 banner
 *
 * @author zicca
 */
@Slf4j
public class ZThreadBannerHandler implements InitializingBean {

    private static final String DYNAMIC_THREAD_POOL = " :: Dynamic ThreadPool :: ";
    private static final String ZTHREAD_DASHBOARD = "Git:    https://github.com/zicca-coder/zthread";
    private static final int STRAP_LINE_SIZE = 50;
    private final String version;

    public ZThreadBannerHandler(BuildProperties buildProperties) {
        this.version = buildProperties != null ? buildProperties.getVersion() : "";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String banner = """
                          __  .__                              .___
                _________/  |_|  |_________   ____ _____     __| _/
                \\___   /\\   __\\  |  \\_  __ \\_/ __ \\\\__  \\   / __ |\s
                 /    /  |  | |   Y  \\  | \\/\\  ___/ / __ \\_/ /_/ |\s
                /_____ \\ |__| |___|  /__|    \\___  >____  /\\____ |\s
                      \\/           \\/            \\/     \\/      \\/\s
                """;
        String bannerVersion = StrUtil.isNotEmpty(version) ? " (v" + version + ")" : "no version.";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE - (bannerVersion.length() + DYNAMIC_THREAD_POOL.length())) {
            padding.append(" ");
        }
        System.out.println(AnsiOutput.toString(banner, AnsiColor.GREEN, DYNAMIC_THREAD_POOL, AnsiColor.DEFAULT,
                padding.toString(), AnsiStyle.FAINT, bannerVersion, "\n", ZTHREAD_DASHBOARD, "\n"));
    }
}
