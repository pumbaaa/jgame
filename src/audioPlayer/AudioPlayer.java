package audioPlayer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

// Static class to control the game sounds
public class AudioPlayer {

  // A index list of sounds and music in the game
  // Sounds
  public static final int SFX_SSH_SHORT = 100;
  public static final int SFX_SSH = 101;
  public static final int SFX_FIRE = 102;
  public static final int SFX_FIRE2 = 103;
  public static final int SFX_BOOM = 104;
  public static final int SFX_BLOPP = 105;
  public static final int SFX_PICKUP = 106;
  public static final int SFX_WOEW = 107;
  public static final int SFX_SPAWN = 108;
  public static final int SFX_DEATH = 109;
  public static final int SFX_DOOR = 110;
  public static final int SFX_SHOOT = 111;
  public static final int SFX_SELECT = 112;
  public static final int SFX_MENUOPTION = 113;
  public static final int SFX_ENEMYDEATH = 114;
  public static final int SFX_BOSSDEATH = 215;
  // Music
  public static final int MSX_MAIN_MENU = 200;
  public static final int MSX_OVERWORLD = 201;
  public static final int MSX_UNDERWORLD = 202;
  public static final int MSX_BOSS = 203;
  public static final int MSX_END = 204;

  // A indexed list of the sounds and music in the game, along with the files
  private static HashMap<Integer, Clip> soundClips = new HashMap<Integer, Clip>();
  private static HashMap<Integer, String> fileNames = new HashMap<Integer, String>();

  private static String SOUND_PATH = "/assets/sound/";

  public static void init() {
    fileNames.put(SFX_SSH_SHORT, SOUND_PATH + "sfx/ssh_short_mono_8bit.wav");
    fileNames.put(SFX_SSH, SOUND_PATH + "sfx/ssh_mono_8bit.wav");
    fileNames.put(SFX_FIRE, SOUND_PATH + "sfx/fire_mono_8bit.wav");
    fileNames.put(SFX_FIRE2, SOUND_PATH + "sfx/fire2_mono_8bit.wav");
    fileNames.put(SFX_BOOM, SOUND_PATH + "sfx/boom_mono_8bit.wav");
    fileNames.put(SFX_BLOPP, SOUND_PATH + "sfx/blopp_mono_8bit.wav");
    fileNames.put(SFX_SPAWN, SOUND_PATH + "sfx/spawn_mono_8bit.wav");
    fileNames.put(SFX_PICKUP, SOUND_PATH + "sfx/pickup_mono_8bit.wav");
    fileNames.put(SFX_WOEW, SOUND_PATH + "sfx/woew_mono_8bit.wav");
    fileNames.put(SFX_DEATH, SOUND_PATH + "sfx/death_mono_8bit.wav");
    fileNames.put(SFX_DOOR, SOUND_PATH + "sfx/door_mono_8bit.wav");
    fileNames.put(SFX_SHOOT, SOUND_PATH + "sfx/shoot_mono_8bit.wav");
    fileNames.put(SFX_SELECT, SOUND_PATH + "sfx/select_mono_8bit.wav");
    fileNames.put(SFX_MENUOPTION, SOUND_PATH + "sfx/menuoption_mono_8bit.wav");
    fileNames.put(SFX_ENEMYDEATH, SOUND_PATH + "sfx/enemydeath_mono_8bit.wav");
    fileNames.put(SFX_BOSSDEATH, SOUND_PATH + "sfx/boss_death_mono_8bit.wav");

    fileNames.put(MSX_MAIN_MENU,
        SOUND_PATH + "msx/Nature-Absinth_mono_8bit.wav");
    fileNames.put(MSX_OVERWORLD,
        SOUND_PATH + "msx/esau-RideHome_mono_8bit.wav");
    fileNames.put(MSX_UNDERWORLD, SOUND_PATH + "msx/underworld_mono_8bit.wav");
    fileNames.put(MSX_BOSS, SOUND_PATH + "msx/boss_mono_8bit.wav");
    fileNames.put(MSX_END,
        SOUND_PATH + "msx/Esau - Puzzle Master_mono_8bit.wav");
  }

  // When called, loads the specified sound/music from file
  public static void load(Integer n) {
    if (soundClips.get(n) == null) {
      Clip soundClip;
      try {
        InputStream audioSource = AudioPlayer.class
            .getResourceAsStream(fileNames.get(n));
        InputStream bufferedSource = new BufferedInputStream(audioSource);
        AudioInputStream audioInRaw = AudioSystem
            .getAudioInputStream(bufferedSource);
        AudioFormat format = audioInRaw.getFormat();
        /*
         * AudioFormat( AudioFormat.Encoding encoding, float sampleRate, int
         * sampleSizeInBits, int channels, int frameSize, float frameRate,
         * boolean bigEndian)
         */
        AudioFormat decode = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
            format.getSampleRate(), 16, format.getChannels(),
            format.getChannels() * 2, format.getSampleRate(), false);
        AudioInputStream audioInDecoded = AudioSystem
            .getAudioInputStream(decode, audioInRaw);
        try {
          soundClip = AudioSystem.getClip();
          soundClip.open(audioInDecoded);
          soundClips.put(n, soundClip);
        } catch (LineUnavailableException e) {
          e.printStackTrace();
        }
      } catch (UnsupportedAudioFileException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // When called, unloads the sounds and music from memory
  public static void unload(Integer sound) {
    stop(sound);
    soundClips.get(sound).close();
  }

  // When called plays the music/sounds
  public static void play(Integer sound) {
    if (soundClips.get(sound) != null) {
      if (soundClips.get(sound).isRunning()) {
        stop(sound);
      }
      while (!soundClips.get(sound).isRunning()) {
        soundClips.get(sound).setFramePosition(0);
        soundClips.get(sound).start();
      }
    }
  }

  // When called stops the sound/music
  public static void stop(Integer sound) {
    if (soundClips.get(sound) != null) {
      if (soundClips.get(sound).isRunning()) {
        soundClips.get(sound).stop();
      }
    }
  }

  // Loops the sound until stopped.
  public static void playLoop(Integer sound, Integer startAt) {
    if (!soundClips.get(sound).isRunning()) {
      soundClips.get(sound).setLoopPoints(startAt,
          soundClips.get(sound).getFrameLength() - 1);
      soundClips.get(sound).setFramePosition(startAt);
      soundClips.get(sound).loop(Clip.LOOP_CONTINUOUSLY);
    }
  }
}
