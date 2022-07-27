package com.github.steanky.element.core.key;

import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Equivalent to using @Pattern(Constants.KEY_PATTERN). Specifies that the annotation target must conform to the
 * KEY_PATTERN regex.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ METHOD, FIELD, PARAMETER, LOCAL_VARIABLE, ANNOTATION_TYPE })
@Pattern(Constants.KEY_PATTERN)
public @interface KeyString {}
