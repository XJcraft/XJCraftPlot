package org.xjcraft.plot.common.exception;

/**
 * 回滚异常，事务包装遇到这个异常时，会回滚事务并返回 null
 */
public class RollbackException extends RuntimeException {}
