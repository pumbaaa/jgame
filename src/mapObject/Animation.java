package mapObject;

public class Animation {
  private int[] frames;
  private int currentFrame;
  private int numFrames;
  private boolean loop;
  private boolean playedOnce;
  
  private int delay;
  private int delayLimit;

  public Animation() {
  }

  public Animation(int[] frames) {
    this.frames = frames;
    currentFrame = 0;
    numFrames = frames.length;
    delayLimit = 20;
    loop = true;
  }

  public Animation(int firstFrame, int numFrames) {
    this.numFrames = numFrames;
    frames = new int[numFrames];

    // Populate the animation with the specified range of frames
    for(int i = 0; i < numFrames; ++i){
      frames[i] = firstFrame + i;
    }
    
    currentFrame = 0;
    delayLimit = 20;
    loop = true;
  }

  public void playOnce() {
    loop = false;
  }

  public void playLoop() {
    loop = true;
  }

  public void setDelayLimit(int delayLimit) {
    this.delayLimit = delayLimit;
  }

  public void loadFrames(int[] frames) {
    this.frames = frames;
    currentFrame = 0;
    numFrames = frames.length;
    delayLimit = 20;
  }

  public void loadFrames(int firstFrame, int numFrames) {
    this.numFrames = numFrames;
    frames = new int[numFrames];

    // Populate the animation with the specified range of frames
    for(int i = 0; i < numFrames; ++i){
      frames[i] = firstFrame + i;
    }

    currentFrame = 0;
    delayLimit = 20;
  }

  public int getFrame() {
    nextFrame();
    return frames[currentFrame];
  }

  private void nextFrame() {
    ++delay;
    if (delay >= delayLimit) {
      ++currentFrame;
      if (loop) {
        currentFrame = currentFrame % numFrames;
      } else {
        if (currentFrame >= numFrames) {
          currentFrame = numFrames - 1;
          playedOnce = true;
        }
      }
      delay = 0;
    }
  }

  public boolean hasPlayedOnce(){
    return playedOnce;
  }
  
  public void reset() {
    currentFrame = 0;
  };
}
