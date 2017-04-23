package net.sleepystudios.ld38;

/**
 * Created by Tudor on 23/04/2017.
 */
public class Packets {
    public static class Join {
        String name;
    }

    public static class Leave {
        int id;
    }

    public static class NewPlayer {
        String name;
        int id;
        float x, y;
    }

    public static class Move {
        int id;
        float x, y;
    }

    public static class Entity {
        String id;
        float x, y, scale;
        int type;
    }

    public static class RemoveEntity {
        String id;
    }
}
