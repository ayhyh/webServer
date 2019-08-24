package com.loop.hyh.sample;

import com.loop.hyh.core.BootStrap;

public class Main {
    public static void main(String[] args) {
        BootStrap.run(args.length > 0 ? args[0] : null);
    }
}
