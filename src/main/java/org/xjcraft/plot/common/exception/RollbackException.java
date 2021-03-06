package org.xjcraft.plot.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 回滚异常，事务包装遇到这个异常时，会回滚事务并返回 null
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RollbackException extends RuntimeException {
    /**
     * 事务的返回值，注意数据类型必须和声明的返回值类型一致，不然会抛出 {@link ClassCastException}
     */
    private Object data;
}
