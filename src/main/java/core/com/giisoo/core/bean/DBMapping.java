package com.giisoo.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * the {@code DBMapping} Class used to annotate the Bean, define the
 * collection/table mapping with the Bean
 * 
 * <pre>
 * db, default empty="prod", used to define the db name which defined in giisoo.properties
 * table, the table of mapped
 * collection, the collection of mapped, when collection defined, will ignore the table
 * </pre>
 * 
 * @author joe
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DBMapping {

    /**
     * the db name, default is EMPTY="prod"
     * 
     * @return String
     */
    String db() default X.EMPTY;

    /**
     * the table name
     * 
     * @return String
     */
    String table() default X.EMPTY;

    /**
     * the collection name
     * 
     * @return String
     */
    String collection() default X.EMPTY;

}
