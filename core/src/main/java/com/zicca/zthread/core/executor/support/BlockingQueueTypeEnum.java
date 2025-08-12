package com.zicca.zthread.core.executor.support;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 阻塞队列类型枚举
 *
 * @author zicca
 */
public enum BlockingQueueTypeEnum {

    /**
     * {@link ArrayBlockingQueue}
     */
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        public <T> BlockingQueue<T> of() {
            return new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        }

        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity);
        }
    },

    /**
     * {@link LinkedBlockingQueue}
     */
    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        @Override
        public <T> BlockingQueue<T> of() {
            return new LinkedBlockingQueue<>();
        }

        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingQueue<>(capacity);
        }
    },

    /**
     * {@link LinkedBlockingDeque}
     */
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque") {
        @Override
        public <T> BlockingQueue<T> of() {
            return new LinkedBlockingDeque<>();
        }

        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingDeque<>(capacity);
        }
    },

    /**
     * {@link SynchronousQueue}
     */
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        public <T> BlockingQueue<T> of() {
            return new SynchronousQueue<>();
        }

        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new SynchronousQueue<>();
        }
    },

    /**
     * {@link LinkedTransferQueue}
     */
    LINKED_TRANSFER_QUEUE("LinkedTransferQueue") {
        @Override
        public <T> BlockingQueue<T> of() {
            return new LinkedTransferQueue<>();
        }

        @Override
        public <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedTransferQueue<>();
        }
    },

    /**
     * {@link PriorityBlockingQueue}
     */
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new PriorityBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new PriorityBlockingQueue<>();
        }
    }
    ;


    @Getter
    private final String name;

    BlockingQueueTypeEnum(String name) {
        this.name = name;
    }

    /**
     * Create the specified implement of BlockingQueue,has no capacity limit.
     * Abstract method, depends on sub override
     *
     * @param <T> the class of the objects in the BlockingQueue
     * @return a BlockingQueue view of the specified T
     */
    abstract <T> BlockingQueue<T> of();

    /**
     * Create the specified implement of BlockingQueue with init capacity.
     * Abstract method, depends on sub override
     *
     * @param capacity the capacity of the queue
     * @param <T>      the class of the objects in the BlockingQueue
     * @return a BlockingQueue view of the specified T
     */
    abstract <T> BlockingQueue<T> of(Integer capacity);

    private static final int DEFAULT_CAPACITY = 4096;

    private static final Map<String, BlockingQueueTypeEnum> NAME_TO_ENUM_MAP;

    static {
        final BlockingQueueTypeEnum[] values = values();
        NAME_TO_ENUM_MAP = new HashMap<>(values.length);
        for (BlockingQueueTypeEnum value : values) {
            NAME_TO_ENUM_MAP.put(value.name, value);
        }
    }

    /**
     * Creates a BlockingQueue with the given {@link BlockingQueueTypeEnum#name BlockingQueueTypeEnum.name}
     * and capacity.
     *
     * @param blockingQueueName {@link BlockingQueueTypeEnum#name BlockingQueueTypeEnum.name}
     * @param capacity          the capacity of the BlockingQueue
     * @param <T>               the class of the objects in the BlockingQueue
     * @return a BlockingQueue view of the specified T
     */
    private static <T> BlockingQueue<T> of(String blockingQueueName, Integer capacity) {
        final BlockingQueueTypeEnum typeEnum = NAME_TO_ENUM_MAP.get(blockingQueueName);
        if (typeEnum == null) {
            return null;
        }
        return Objects.isNull(capacity) ? typeEnum.of() : typeEnum.of(capacity);
    }

    /**
     * Creates a BlockingQueue with the given {@link BlockingQueueTypeEnum#name BlockingQueueTypeEnum.name}
     * and capacity.
     *
     * @param blockingQueueName {@link BlockingQueueTypeEnum#name BlockingQueueTypeEnum.name}
     * @param capacity          the capacity of the BlockingQueue
     * @param <T>               the class of the objects in the BlockingQueue
     * @return a BlockingQueue view of the specified T
     * @throws IllegalArgumentException If no matching queue type is found
     */
    public static <T> BlockingQueue<T> createBlockingQueue(String blockingQueueName, Integer capacity) {
        final BlockingQueue<T> of = of(blockingQueueName, capacity);
        if (of != null) {
            return of;
        }
        throw new IllegalArgumentException("No matching type of blocking queue was found: " + blockingQueueName);
    }


}
