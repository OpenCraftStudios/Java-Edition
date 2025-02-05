package net.opencraft.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Do not call this method directly! This method may use too much CPU.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface MayConsumeCPU {
}
