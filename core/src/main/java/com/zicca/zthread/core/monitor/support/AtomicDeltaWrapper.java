package com.zicca.zthread.core.monitor.support;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicDeltaWrapper implements DeltaWrapper{
    private static class ValuePair {
        final Long lastValue;
        final Long currentValue;

        ValuePair(Long lastValue, Long currentValue) {
            this.lastValue = lastValue;
            this.currentValue = currentValue;
        }
    }

    private final AtomicReference<ValuePair> values = new AtomicReference<>(new ValuePair(null, null));

    /**
     * 更新最新值，并记录上一次的值，便于计算 delta
     *
     * @param newValue 当前周期采集到的原始指标值
     */
    @Override
    public void update(long newValue) {
        while (true) {
            ValuePair current = values.get();
            ValuePair updated = new ValuePair(
                    (current.currentValue == null) ? newValue : current.currentValue,
                    newValue
            );

            if (values.compareAndSet(current, updated)) {
                break;
            }
        }
    }

    /**
     * 获取当前周期与上一周期之间的增量值
     *
     * @return 周期内的差值：首次为 0
     */
    @Override
    public long getDelta() {
        ValuePair current = values.get();
        if (current.currentValue == null || current.lastValue == null) {
            return 0;
        }
        return current.currentValue - current.lastValue;
    }
}
