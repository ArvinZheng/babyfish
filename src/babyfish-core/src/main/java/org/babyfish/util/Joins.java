/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.util;

import org.babyfish.lang.Arguments;
import org.babyfish.lang.Func;

/**
 * @author Tao Chen
 */
public class Joins {
    
    private static final String DEFAULT_SEPERATOR = ", ";

    protected Joins() {
        throw new UnsupportedOperationException();
    }
    
    public static String join(
            Iterable<?> iterable) {
        StringBuilder builder = new StringBuilder();
        join(iterable, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            Iterable<?> iterable,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(iterable, sperator, null, builder);
        return builder.toString();
    }
    
    public static <T> String join(
            Iterable<T> iterable,
            Func<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static <T> String join(
            Iterable<T> iterable,
            String seperator,
            Func<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(iterable, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            Iterable<?> iterable,
            StringBuilder outputStringBuilder) {
        join(iterable, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            Iterable<?> iterable,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(iterable, sperator, null, outputStringBuilder);
    }
    
    public static <T> void join(
            Iterable<T> iterable,
            Func<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            Iterable<?> iterable,
            StringBuffer outputStringBuffer) {
        join(iterable, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            Iterable<?> iterable,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(iterable, sperator, null, outputStringBuffer);
    }
    
    public static <T> void join(
            Iterable<T> iterable,
            Func<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(iterable, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }

    public static <T> void join(
            Iterable<T> iterable,
            String seperator,
            Func<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("iterable", iterable);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : iterable) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : iterable) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }

    public static <T> void join(
            Iterable<T> iterable,
            String seperator,
            Func<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("iterable", iterable);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : iterable) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : iterable) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : iterable) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            T[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static <T> String join(
            T[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static <T> String join(
            T[] arr,
            Func<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static <T> String join(
            T[] arr,
            String seperator,
            Func<T, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static <T> void join(
            T[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static <T> void join(
            T[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static <T> void join(
            T[] arr,
            Func<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static <T> void join(
            T[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static <T> void join(
            T[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static <T> void join(
            T[] arr,
            Func<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }

    public static <T> void join(
            T[] arr,
            String seperator,
            Func<T, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }

    public static <T> void join(
            T[] arr,
            String seperator,
            Func<T, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (T element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (T element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            boolean[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            boolean[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            boolean[] arr,
            Func<Boolean, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            boolean[] arr,
            String seperator,
            Func<Boolean, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            boolean[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            boolean[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            boolean[] arr,
            Func<Boolean, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            boolean[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            boolean[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            boolean[] arr,
            Func<Boolean, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            boolean[] arr,
            String seperator,
            Func<Boolean, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (boolean element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            boolean[] arr,
            String seperator,
            Func<Boolean, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (boolean element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (boolean element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            char[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            char[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            char[] arr,
            Func<Character, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            char[] arr,
            String seperator,
            Func<Character, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            char[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            char[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            char[] arr,
            Func<Character, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            char[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            char[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            char[] arr,
            Func<Character, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            char[] arr,
            String seperator,
            Func<Character, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (char element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (char element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            char[] arr,
            String seperator,
            Func<Character, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (char element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (char element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (char element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            byte[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            byte[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            byte[] arr,
            Func<Byte, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            byte[] arr,
            String seperator,
            Func<Byte, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            byte[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            byte[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            byte[] arr,
            Func<Byte, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            byte[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            byte[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            byte[] arr,
            Func<Byte, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            byte[] arr,
            String seperator,
            Func<Byte, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (byte element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (byte element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            byte[] arr,
            String seperator,
            Func<Byte, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (byte element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (byte element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (byte element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            short[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            short[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            short[] arr,
            Func<Short, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            short[] arr,
            String seperator,
            Func<Short, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            short[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            short[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            short[] arr,
            Func<Short, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            short[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            short[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            short[] arr,
            Func<Short, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            short[] arr,
            String seperator,
            Func<Short, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (short element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (short element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            short[] arr,
            String seperator,
            Func<Short, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (short element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (short element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (short element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            int[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            int[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            int[] arr,
            Func<Integer, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            int[] arr,
            String seperator,
            Func<Integer, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            int[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            int[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            int[] arr,
            Func<Integer, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            int[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            int[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            int[] arr,
            Func<Integer, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            int[] arr,
            String seperator,
            Func<Integer, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (int element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (int element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            int[] arr,
            String seperator,
            Func<Integer, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (int element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (int element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (int element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            long[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            long[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            long[] arr,
            Func<Long, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            long[] arr,
            String seperator,
            Func<Long, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            long[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            long[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            long[] arr,
            Func<Long, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            long[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            long[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            long[] arr,
            Func<Long, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            long[] arr,
            String seperator,
            Func<Long, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (long element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (long element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            long[] arr,
            String seperator,
            Func<Long, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (long element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (long element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (long element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            float[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            float[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            float[] arr,
            Func<Float, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            float[] arr,
            String seperator,
            Func<Float, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            float[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            float[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            float[] arr,
            Func<Float, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            float[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            float[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            float[] arr,
            Func<Float, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            float[] arr,
            String seperator,
            Func<Float, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (float element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (float element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            float[] arr,
            String seperator,
            Func<Float, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (float element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (float element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (float element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static <T> String join(
            double[] arr) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, null, builder);
        return builder.toString();
    }

    public static String join(
            double[] arr,
            String sperator) {
        StringBuilder builder = new StringBuilder();
        join(arr, sperator, null, builder);
        return builder.toString();
    }
    
    public static String join(
            double[] arr,
            Func<Double, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, DEFAULT_SEPERATOR, toStringFunc, builder);
        return builder.toString();
    }

    public static String join(
            double[] arr,
            String seperator,
            Func<Double, String> toStringFunc) {
        StringBuilder builder = new StringBuilder();
        join(arr, seperator, toStringFunc, builder);
        return builder.toString();
    }
    
    public static void join(
            double[] arr,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuilder);
    }

    public static void join(
            double[] arr,
            String sperator,
            StringBuilder outputStringBuilder) {
        join(arr, sperator, null, outputStringBuilder);
    }
    
    public static void join(
            double[] arr,
            Func<Double, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuilder);
    }

    public static void join(
            double[] arr,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, null, outputStringBuffer);
    }

    public static void join(
            double[] arr,
            String sperator,
            StringBuffer outputStringBuffer) {
        join(arr, sperator, null, outputStringBuffer);
    }
    
    public static void join(
            double[] arr,
            Func<Double, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        join(arr, DEFAULT_SEPERATOR, toStringFunc, outputStringBuffer);
    }
    
    public static void join(
            double[] arr,
            String seperator,
            Func<Double, String> toStringFunc,
            StringBuilder outputStringBuilder) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuilder", outputStringBuilder);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (double element : arr) {
                    outputStringBuilder.append(element);
                }
            } else {
                for (double element : arr) {
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(element);
                }
            } else {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuilder.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuilder.append(toStringFunc.run(element));
                }
            }
        }
    }
    
    public static void join(
            double[] arr,
            String seperator,
            Func<Double, String> toStringFunc,
            StringBuffer outputStringBuffer) {
        Arguments.mustNotBeNull("arr", arr);
        Arguments.mustNotBeNull("outputStringBuffer", outputStringBuffer);
        if (seperator == null || seperator.isEmpty()) {
            if (toStringFunc == null) {
                for (double element : arr) {
                    outputStringBuffer.append(element);
                }
            } else {
                for (double element : arr) {
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        } else {
            boolean addSeperator = false;
            if (toStringFunc == null) {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(element);
                }
            } else {
                for (double element : arr) {
                    if (addSeperator) {
                        outputStringBuffer.append(seperator);
                    } else {
                        addSeperator = true;
                    }
                    outputStringBuffer.append(toStringFunc.run(element));
                }
            }
        }
    }
}
