package dete.org.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

final class PcmAudio {
    private PcmAudio() {}

    static AudioInputStream open(String resource) throws IOException, UnsupportedAudioFileException {
        InputStream file = PcmAudio.class.getResourceAsStream(resource);
        if (file == null) {
            throw new IOException("Recurso de áudio ausente: " + resource);
        }

        var buffered = new BufferedInputStream(file);
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(buffered);
            AudioFormat source = stream.getFormat();
            AudioFormat pcm = new AudioFormat(source.getSampleRate(), 16, source.getChannels(), true, false);
            return source.matches(pcm) ? stream : AudioSystem.getAudioInputStream(pcm, stream);
        } catch (IOException | UnsupportedAudioFileException | RuntimeException exception) {
            try {
                buffered.close();
            } catch (IOException closeFailure) {
                exception.addSuppressed(closeFailure);
            }
            throw exception;
        }
    }
}
