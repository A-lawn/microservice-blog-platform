package com.blog.platform.common.domain;

/**
 * 值对象基类
 * 值对象应该是不可变的，并且基于值相等性
 */
public abstract class ValueObject {
    
    /**
     * 值对象必须实现equals方法来比较值相等性
     */
    @Override
    public abstract boolean equals(Object obj);
    
    /**
     * 值对象必须实现hashCode方法
     */
    @Override
    public abstract int hashCode();
    
    /**
     * 提供有意义的字符串表示
     */
    @Override
    public abstract String toString();
    
    /**
     * 验证值对象的有效性
     * 子类应该重写此方法来实现特定的验证逻辑
     */
    protected void validate() {
        // 默认实现为空，子类可以重写
    }
}