package dete.org.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public final class SoundEffects implements AutoCloseable {
    public enum Cue {
        MOVE_SELECTION("/audio/sfx-move-selection.wav"),
        CONFIRM("/audio/sfx-confirm.wav"),
        CANCEL("/audio/sfx-cancel.wav"),
        HIT("/audio/sfx-hit.wav"),
        CRITICAL_HIT("/audio/sfx-critical-hit.wav"),
        DRINK_POTION("/audio/sfx-drink-potion.wav"),
        BATTLE_START("/audio/sfx-battle-start.wav");

        private final String resource;

        Cue(String resource) {
            this.resource = resource;
        }

        public String resource() {
            return resource;
        }
    }

    private static final System.Logger LOGGER = System.getLogger(SoundEffects.class.getName());
    private static final long MIN_RETRIGGER_NANOS = 20_000_000L; // 20 ms
    private static final Object ENGINE_LOCK = new Object();

    private static Engine activeEngine;
    private static volatile boolean shuttingDown;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shuttingDown = true;
            Engine toClose;
            synchronized (ENGINE_LOCK) {
                toClose = activeEngine;
                activeEngine = null;
            }
            if (toClose != null) {
                toClose.close();
            }
        }, "rpg-sfx-shutdown"));
    }

    private final Engine engine;

    private SoundEffects(Engine engine) {
        this.engine = engine;
    }

    public static SoundEffects open() {
        Engine previous;
        Engine created;
        synchronized (ENGINE_LOCK) {
            if (shuttingDown) {
                return new SoundEffects(null);
            }
            previous = activeEngine;
            created = new Engine();
            activeEngine = created;
        }
        if (previous != null) {
            previous.close();
        }
        created.start();
        return new SoundEffects(created);
    }

    public static void play(Cue cue) {
        if (cue == null || shuttingDown) {
            return;
        }
        Engine engine;
        synchronized (ENGINE_LOCK) {
            engine = activeEngine;
        }
        if (engine != null) {
            engine.enqueue(cue);
        }
    }

    @Override
    public void close() {
        if (engine == null) {
            return;
        }
        synchronized (ENGINE_LOCK) {
            if (activeEngine == engine) {
                activeEngine = null;
            }
        }
        engine.close();
    }

    private record AudioData(AudioFormat format, byte[] pcmBytes) {
    }

    private static final class AudioCache {
        private static final Map<Cue, AudioData> DATA = loadAll();

        private static Map<Cue, AudioData> loadAll() {
            EnumMap<Cue, AudioData> loaded = new EnumMap<>(Cue.class);
            for (Cue cue : Cue.values()) {
                try {
                    loaded.put(cue, readWav(cue.resource()));
                } catch (IOException | UnsupportedAudioFileException | IllegalArgumentException exception) {
                    LOGGER.log(System.Logger.Level.DEBUG, "Não foi possível carregar SFX " + cue.resource(), exception);
                }
            }
            return loaded;
        }

        private static AudioData readWav(String resource) throws IOException, UnsupportedAudioFileException {
            try (InputStream raw = SoundEffects.class.getResourceAsStream(resource)) {
                if (raw == null) {
                    throw new IOException("Recurso de áudio ausente: " + resource);
                }
                try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                    AudioFormat sourceFormat = stream.getFormat();
                    if (isPcm16LittleEndian(sourceFormat)) {
                        return new AudioData(sourceFormat, readAllBytes(stream));
                    }
                    AudioFormat targetFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            sourceFormat.getSampleRate(),
                            16,
                            sourceFormat.getChannels(),
                            sourceFormat.getChannels() * 2,
                            sourceFormat.getSampleRate(),
                            false
                    );
                    try (AudioInputStream converted = AudioSystem.getAudioInputStream(targetFormat, stream)) {
                        return new AudioData(targetFormat, readAllBytes(converted));
                    }
                }
            }
        }

        private static boolean isPcm16LittleEndian(AudioFormat format) {
            return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                    && format.getSampleSizeInBits() == 16
                    && !format.isBigEndian();
        }

        private static byte[] readAllBytes(AudioInputStream stream) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }

        private static AudioData get(Cue cue) {
            return DATA.get(cue);
        }
    }

    private static final class Engine {
        private final ReentrantLock queueLock = new ReentrantLock();
        private final Condition hasWork = queueLock.newCondition();
        private final EnumSet<Cue> pendingSet = EnumSet.noneOf(Cue.class);
        private final ArrayDeque<Cue> pendingQueue = new ArrayDeque<>(Cue.values().length);
        private final ArrayDeque<Cue> preloadQueue = new ArrayDeque<>(Arrays.asList(Cue.values()));

        private final Object clipLock = new Object();
        private final EnumMap<Cue, Clip> clips = new EnumMap<>(Cue.class);
        private final EnumMap<Cue, Long> lastPlayedNanos = new EnumMap<>(Cue.class);

        private volatile boolean closed;
        private volatile boolean audioUnavailable;
        private final Thread worker;

        private Engine() {
            this.worker = Thread.ofPlatform()
                    .daemon(true)
                    .name("rpg-sfx")
                    .unstarted(this::runLoop);
        }

        private void start() {
            worker.start();
        }

        private void enqueue(Cue cue) {
            if (closed || audioUnavailable) {
                return;
            }
            queueLock.lock();
            try {
                if (closed) {
                    return;
                }
                if (pendingSet.add(cue)) {
                    pendingQueue.addLast(cue);
                    hasWork.signal();
                }
            } finally {
                queueLock.unlock();
            }
        }

        private void runLoop() {
            try {
                while (!closed && !audioUnavailable) {
                    Cue playNext = null;
                    Cue preloadNext = null;

                    queueLock.lock();
                    try {
                        while (!closed && !audioUnavailable && pendingQueue.isEmpty() && preloadQueue.isEmpty()) {
                            hasWork.await();
                        }
                        if (closed || audioUnavailable) {
                            break;
                        }
                        if (!pendingQueue.isEmpty()) {
                            playNext = pendingQueue.pollFirst();
                            pendingSet.remove(playNext);
                        } else {
                            preloadNext = preloadQueue.pollFirst();
                        }
                    } finally {
                        queueLock.unlock();
                    }

                    if (playNext != null) {
                        playCue(playNext);
                    } else if (preloadNext != null) {
                        getOrOpenClip(preloadNext);
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                closeAllClips();
            }
        }

        private void playCue(Cue cue) {
            if (closed || audioUnavailable) {
                return;
            }
            long now = System.nanoTime();
            Long previous = lastPlayedNanos.get(cue);
            if (previous != null && (now - previous) < MIN_RETRIGGER_NANOS) {
                return;
            }
            synchronized (clipLock) {
                if (closed || audioUnavailable) {
                    return;
                }
                Clip clip = getOrOpenClip(cue);
                if (clip == null) {
                    return;
                }
                try {
                    clip.stop();
                    clip.setFramePosition(0);
                    clip.start();
                    lastPlayedNanos.put(cue, now);
                } catch (IllegalStateException | IllegalArgumentException | SecurityException exception) {
                    LOGGER.log(System.Logger.Level.DEBUG, "Não foi possível tocar SFX " + cue, exception);
                }
            }
        }

        private Clip getOrOpenClip(Cue cue) {
            synchronized (clipLock) {
                if (closed || audioUnavailable) {
                    return null;
                }
                Clip existing = clips.get(cue);
                if (existing != null) {
                    return existing;
                }
                AudioData data = AudioCache.get(cue);
                if (data == null) {
                    return null;
                }
                Clip clip = null;
                try {
                    clip = AudioSystem.getClip();
                    clip.open(data.format(), data.pcmBytes(), 0, data.pcmBytes().length);
                    clips.put(cue, clip);
                    clip = null;
                    return clips.get(cue);
                } catch (LineUnavailableException | IllegalArgumentException | IllegalStateException
                         | SecurityException exception) {
                    if (clips.isEmpty()) {
                        audioUnavailable = true;
                    }
                    LOGGER.log(System.Logger.Level.DEBUG, "Dispositivo de áudio indisponível para SFX " + cue, exception);
                    return null;
                } finally {
                    if (clip != null) {
                        clip.close();
                    }
                }
            }
        }

        private void close() {
            closed = true;
            queueLock.lock();
            try {
                pendingQueue.clear();
                pendingSet.clear();
                preloadQueue.clear();
                hasWork.signalAll();
            } finally {
                queueLock.unlock();
            }
            worker.interrupt();
            closeAllClips();
            try {
                worker.join(200);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        private void closeAllClips() {
            synchronized (clipLock) {
                for (Clip clip : clips.values()) {
                    try {
                        clip.stop();
                        clip.close();
                    } catch (RuntimeException exception) {
                        LOGGER.log(System.Logger.Level.DEBUG, "Erro ao fechar clip de SFX", exception);
                    }
                }
                clips.clear();
            }
        }
    }
}
