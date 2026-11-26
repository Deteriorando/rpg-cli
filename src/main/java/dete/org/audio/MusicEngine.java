package dete.org.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        try (InputStream file = MusicEngine.class.getResourceAsStream(resource)) {
            if (file == null) {
                throw new IOException("Recurso de áudio ausente: " + resource);
            }
            synchronized (this) {
                if (closed) {
                    return;
                }
            }

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(file))) {
                AudioFormat inputFormat = audioStream.getFormat();
                clip = AudioSystem.getClip();
                if (isPcm16LittleEndian(inputFormat)) {
                    clip.open(audioStream);
                } else {
                    AudioFormat pcmFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            inputFormat.getSampleRate(),
                            16,
                            inputFormat.getChannels(),
                            inputFormat.getChannels() * 2,
                            inputFormat.getSampleRate(),
                            false
                    );
                    try (AudioInputStream pcm = AudioSystem.getAudioInputStream(pcmFormat, audioStream)) {
                        clip.open(pcm);
                    }
                }
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

    private static boolean isPcm16LittleEndian(AudioFormat format) {
        return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                && format.getSampleSizeInBits() == 16
                && !format.isBigEndian();
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
