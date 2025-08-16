package com.zicca.zthread.core.monitor.support;

/**
 * 用于计算两个周期之间的指标差值（如：任务完成数、拒绝次数等）
 * 通常用于 Micrometer Gauge 指标中，暴露单位时间内的变化量
 *
 * @author zicca
 */
public class SynchronizedDeltaWrapper implements DeltaWrapper {

    /**
     * 上一个周期的指标值
     */
    private Long lastValue;

    /**
     * 当前周期的指标值
     */
    private Long currentValue;

    /**
     * 更新最新值，并记录上一次的值，便于计算 delta
     *
     * @param newValue 当前周期采集到的原始指标值，首次调用时 lastValue 和 currentValue 相同
     */
    @Override
    public synchronized void update(long newValue) {
        this.lastValue = (this.currentValue == null) ? newValue : this.currentValue;
        this.currentValue = newValue;
    }

    /**
     * 获取当前周期与上一周期之间的增量值
     *
     * @return 周期内的差值：首次为 0
     */
    @Override
    public synchronized long getDelta() {
        if (currentValue == null || lastValue == null) {
            return 0;
        }
        return currentValue - lastValue;
    }

}
