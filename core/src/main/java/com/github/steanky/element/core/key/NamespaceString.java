package com.github.steanky.element.core.key;

import org.intellij.lang.annotations.Pattern;

/**
 * Equivalent to using @Pattern(Constants.NAMESPACE_PATTERN). Specifies that the annotation target must conform to the
 * NAMESPACE_PATTERN regex.
 */
@Pattern(Constants.NAMESPACE_PATTERN)
public @interface NamespaceString {}
