package com.github.steanky.element.core.key;

import org.intellij.lang.annotations.Pattern;

/**
 * Equivalent to using @Pattern(Constants.KEY_PATTERN). Specifies that the annotation target must conform to the
 * KEY_PATTERN regex.
 */
@Pattern(Constants.KEY_PATTERN)
public @interface KeyString {}
