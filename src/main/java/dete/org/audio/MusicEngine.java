package dete.org.audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public final class MusicEngine implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(MusicEngine.class.getName());

    private Clip playingClip;
    private boolean closed;

    private MusicEngine() {
    }

    public static MusicEngine loop(String resource) {
        MusicEngine music = new MusicEngine();
        Thread.ofVirtual().name("rpg-music").start(() -> music.play(resource));
        return music;
    }

    private void play(String resource) {
        Clip clip = null;
        try {
            synchronized (this) {
                if (closed) {
                    return;
                }
            }
            try (var stream = PcmAudio.open(resource)) {
                clip = AudioSystem.getClip();
                clip.open(stream);
            }

            synchronized (this) {
                if (!closed) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                    playingClip = clip;
                    clip = null;
                }
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException
                 | IllegalArgumentException | IllegalStateException | SecurityException exception) {
            LOGGER.log(System.Logger.Level.DEBUG, "Não foi possível reproduzir " + resource, exception);
        } finally {
            if (clip != null) {
                clip.close();
            }
        }
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (playingClip != null) {
            playingClip.stop();
            playingClip.close();
            playingClip = null;
        }
    }
}
