package io.github.racoondog.recipedl.util;

public class MutableChar {
    private char c;

    public static MutableChar of(char c) {
        MutableChar mutableChar = new MutableChar();
        mutableChar.c = c;
        return mutableChar;
    }

    public char getAndIncrement() {
        return c++;
    }
}
