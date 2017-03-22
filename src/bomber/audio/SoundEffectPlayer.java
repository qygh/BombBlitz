package bomber.audio;

import bomber.game.AudioEvent;
import bomber.game.Constants;
import sun.applet.Main;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Plays sound effects
 * Only to be used by <code>AudioManager</code>
 *
 * @author Alexandru Rosu
 */
class SoundEffectPlayer extends Thread
{

    private HashMap<AudioEvent, Clip> soundClips;

    /**
     * Constructs a sound effect player
     *
     * @param volume The volume percent, ranging from 0 to 100
     */
    SoundEffectPlayer(float volume)
    {
        soundClips = new HashMap<>();
        for(AudioEvent event: AudioEvent.values())
        {
            String fileName;
            switch (event)
            {
                case PLACE_BOMB:
                    fileName = Constants.BOMB_PLACE_FILENAME;
                    break;
                case EXPLOSION:
                    fileName = Constants.EXPLOSION_FILENAME;
                    break;
                case PLAYER_DEATH:
                    fileName = Constants.PLAYER_DEATH_FILENAME;
                    break;
                case MOVEMENT:
                    continue;
                case POWERUP:
                    fileName = Constants.POWERUP_FILENAME;
                    break;
                case MENU_SOUND:
                    fileName = Constants.MENU_SOUND_FILENAME;
                    break;
                case GAME_OVER_WON:
                    fileName = Constants.GAME_OVER_WON_FILENAME;
                    break;
                case GAME_OVER_LOST:
                    fileName = Constants.GAME_OVER_LOST_FILENAME;
                    break;
                default:
                    continue;
            }
            try
            {
                Clip clip = AudioSystem.getClip();
                InputStream rawStream = Main.class.getResourceAsStream(Constants.AUDIO_FILES_PATH + fileName);
                if(rawStream == null)
                    throw new IOException();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(rawStream);
                clip.open(inputStream);
                inputStream.close();
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                AudioManager.setControlVolume(gainControl, volume);
                soundClips.put(event, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Could not load sound: " + Constants.AUDIO_FILES_PATH + fileName);
                e.printStackTrace();
            }
        }
    }

    /**
     * Plays the sound associated with an audio event
     *
     * @param audioEvent The audio event whose corresponding sound will be played
     */
    void play(AudioEvent audioEvent)
    {
        soundClips.get(audioEvent).stop();
        soundClips.get(audioEvent).setFramePosition( 0 );
        soundClips.get(audioEvent).start();
    }

    void setVolume(float percent)
    {
        for(Map.Entry<AudioEvent, Clip> entry:soundClips.entrySet())
        {
            Clip clip = entry.getValue();
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            AudioManager.setControlVolume(gainControl, percent);
        }

    }

    void close()
    {
       soundClips.entrySet().forEach(entry -> entry.getValue().close());
    }

//    /**
//     * Sets a new value for volume
//     *
//     * @param percent The volume percent, ranging from 0 to 100
//     */
//    void setVolume(float percent)
//    {
//        this.volume = percent;
//    }

}
