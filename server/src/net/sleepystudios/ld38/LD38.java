package net.sleepystudios.ld38;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Tudor on 23/04/2017.
 */
public class LD38 {
    int tcp, udp;
    Server server;

    ArrayList<Player> players = new ArrayList<Player>();
    ArrayList<Entity> entities = new ArrayList<Entity>();

    int botNum, idCount;

    public static final int PLANT = 0, FIRE = 1, WATER = 2;
    public static final int SCREEN_W = 640, SCREEN_H = 480;

    public LD38() throws Exception {
        readConfig();

        server = new Server(8192, 4096);
        server.bind(tcp, udp);
        server.addListener(new Receiver(this));
        server.start();
        register();

        System.out.println("Server running on TCP:" + tcp + ", UDP:" + udp + " with " + botNum + " bots");

        // main loop
        loop();
    }

    public void readConfig() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("server_data.txt"));
        try {
            tcp = Integer.valueOf(br.readLine().split(":")[1]);
            udp = Integer.valueOf(br.readLine().split(":")[1]);
            botNum = Integer.valueOf(br.readLine().split(":")[1]);
        } finally {
            br.close();
        }
    }

    public void loop() {
        boolean running = true;

        double ns = 1000000000.0 / 30.0;
        float delta = 0;

        long lastTime = System.nanoTime();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                update(delta/60f);
                delta--;
            }
        }
    }

    float tmrScale, tmrStats;
    public void update(float delta) {
        tmrScale+=delta;
        if(tmrScale>=0.5 && players.size()>0) {
            for(int i=0; i<entities.size(); i++) {
                entities.get(i).update();
            }
            tmrScale = 0;
        }

        tmrStats+=delta;
        if(tmrStats>=30) {
            if(players.size()>0) {
                System.out.println("[STATS] " + players.size() + " players online");
                if(entities.size()>0) System.out.println("[STATS] " + entities.size() + " entities");
            }

            tmrStats = 0;
        }

        for(int i=0; i<players.size(); i++) {
            if(players.get(i).ai) players.get(i).update(delta);
        }
    }

    public static int rand(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public int chooseType() {
        if(players.size()==0) {
            return rand(PLANT, WATER);
        } else {
            int counts[] = new int[3];

            for(int i=0; i<players.size(); i++) {
                if(players.get(i).type!=-1) {
                    counts[players.get(i).type]++;
                }
            }

            if(counts[PLANT]==0) return PLANT;
            if(counts[FIRE]==0) return FIRE;
            if(counts[WATER]==0) return WATER;

            if(counts[PLANT]<counts[FIRE] && counts[PLANT]<counts[WATER]) return PLANT;
            if(counts[FIRE]<counts[PLANT] && counts[FIRE]<counts[WATER]) return FIRE;
            if(counts[WATER]<counts[PLANT] && counts[WATER]<counts[FIRE]) return WATER;
            return rand(PLANT, WATER);
        }
    }

    public Player getPlayerByID(int id) {
        for(int i=0; i<players.size(); i++) {
            if(players.get(i).id==id) return players.get(i);
        }
        return null;
    }

    public Entity getEntityByID(int id) {
        for(int i=0; i<entities.size(); i++) {
            if(entities.get(i)!=null && entities.get(i).id==id) return entities.get(i);
        }
        return null;
    }

    public int getCount(int t) {
        int c = 0;
        for(int i=0; i<entities.size(); i++) {
            if(entities.get(i).type==t) c++;
        }
        return c;
    }

    private void register() {
        Kryo kryo = server.getKryo();
        kryo.register(Packets.Join.class);
        kryo.register(Packets.Leave.class);
        kryo.register(Packets.NewPlayer.class);
        kryo.register(Packets.Move.class);
        kryo.register(Packets.Entity.class);
        kryo.register(Packets.RemoveEntity.class);
        kryo.register(Packets.AddParticles.class);
        kryo.register(Packets.WaterUpdate.class);
        kryo.register(Packets.Attention.class);
    }

    public static void main(String[] args) {
        try {
            new LD38();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
