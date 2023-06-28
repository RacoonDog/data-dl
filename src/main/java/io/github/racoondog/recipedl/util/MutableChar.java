package io.github.racoondog.recipedl.util;

public class MutableChar {
    private char c;

    public static MutableChar of(char c) {
        MutableChar mutableChar = new MutableChar();
        mutableChar.c = c;
        return mutableChar;
    }

    public char get() {
        return c;
    }

    public void set(char newC) {
        c = newC;
    }

    public char getAndIncrement() {
        return c++;
    }

    public char getAndDecrement() {
        return c--;
    }

    public char incrementAndGet() {
        return ++c;
    }

    public char decrementAndGet() {
        return --c;
    }
}
