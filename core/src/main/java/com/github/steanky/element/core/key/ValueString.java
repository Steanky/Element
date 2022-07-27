package com.github.steanky.element.core.key;

import org.intellij.lang.annotations.Pattern;

/**
 * Equivalent to using @Pattern(Constants.VALUE_PATTERN). Specifies that the annotation target must conform to the
 * VALUE_PATTERN regex.
 */
@Pattern(Constants.VALUE_PATTERN)
public @interface ValueString {}
