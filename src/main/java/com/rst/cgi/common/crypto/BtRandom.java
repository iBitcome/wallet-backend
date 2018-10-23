package com.rst.cgi.common.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.prng.DigestRandomGenerator;
import org.spongycastle.crypto.prng.ThreadedSeedGenerator;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;

/**
 * @author hujia
 */
public class  BtRandom extends SecureRandom {
    private final Logger logger = LoggerFactory.getLogger(BtRandom.class);
    private final DigestRandomGenerator generator = new DigestRandomGenerator(new SHA256Digest());
    private static final FileInputStream urandom;

    static {
        try {
            File file = new File("/dev/urandom");
            if (file.exists()) {
                //deliberately leaked.
                urandom = new FileInputStream(file);
            } else {
                urandom = null;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private final DataInputStream dis = new DataInputStream(urandom);

    @Override
    public synchronized void nextBytes(byte[] bytes) {
        long start = System.currentTimeMillis();
        int length = bytes.length;
        ThreadedSeedGenerator threadedSeedGenerator = new ThreadedSeedGenerator();
        do {
            generator.addSeedMaterial(threadedSeedGenerator.generateSeed(64, true));
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
            generator.addSeedMaterial(threadedSeedGenerator.generateSeed(32, false));
        } while (Math.abs(System.currentTimeMillis() - start) < 100);

        generator.addSeedMaterial(System.nanoTime());
        generator.addSeedMaterial(System.currentTimeMillis());

        Random random = new Random();
        byte[] randomBytes = new byte[128];
        random.nextBytes(randomBytes);
        generator.addSeedMaterial(randomBytes);

        if (urandom != null) {
            byte[] urandomBytes = new byte[length];
            try {
                dis.readFully(urandomBytes);
                generator.addSeedMaterial(urandomBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        generator.addSeedMaterial(System.nanoTime());

        generator.nextBytes(bytes);
    }

    public byte[] nextBytes(int length) {
        byte[] bytes = new byte[length];
        nextBytes(bytes);
        return bytes;
    }
}
