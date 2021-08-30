package com.lucasrivaldo.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter {

    private static final String PREFERENCES = "FLAPPYBIRD";
    private static final String BEST_SCORE = "BEST_SCORE";
    private static final int PIPE_NUM = 2;

    private float mVolume;
    private Preferences myPreferences;

    private OrthographicCamera mCamera;
    private Viewport mViewport;
    private final float VIRTUAL_WIDTH = 1080;
    private final float VIRTUAL_HEIGHT = 1920;

    private boolean mIsGameStarted, mIsColliding, mIsCollidingDown;

    private int mGravity, mWingsAnim, mScore = 0;
    private int mBestScore;

    private int mDisplayWidth, mDisplayHeight;
    private int mBirdWidth, mBirdHeight;

    private int mPipeXSpeed, yBotPipe;


    private boolean[] mIsCrossedPipe;
    private float[] mPipeSpace;
    private int[] mBotPipeHeight, mUpperPipeHeight;
    private int[] xPositionPipe, yUpPipe;

    private Rectangle[] mBotPipeShape, mUpPipeShape;
    private Texture[] mBottomPipe, mUpperPipe;

    private float yInitPosition, xInitPosition;
    private float mSpeedX, mSpeedY;

    private SpriteBatch mBatch;
    private ShapeRenderer mShapeRenderer;

    private Texture mBackground, mGameOver, mFlappyLogo;

    private int mBirdXKnockBack = 0;
    private Circle mBirdShape;
    private Texture[] mBird = new Texture[3];;

    private BitmapFont mScoreText, mRestartText, mBestScoreText;

    private Sound mWingsSound, mCollisionSound, mScoringSound;

	@Override
	public void create () {
	    initializeArrays();
        initializeTextures();
        initializeObjects();
	}

	private void initializeArrays(){
        int pipeNum = 2;

        mIsCrossedPipe = new boolean[pipeNum];
        mBotPipeHeight = new int[pipeNum];
        mUpperPipeHeight = new int[pipeNum];

        xPositionPipe = new int[pipeNum];

        yUpPipe = new int[pipeNum];
        mPipeSpace = new float[pipeNum];
        mBotPipeShape = new Rectangle[pipeNum];
        mUpPipeShape = new Rectangle[pipeNum];
        mBottomPipe = new Texture[pipeNum];
        mUpperPipe = new Texture[pipeNum];

        mBird = new Texture[3];
    }

    private void initializeTextures(){

	    myPreferences = Gdx.app.getPreferences(PREFERENCES);
	    mBestScore = myPreferences.getInteger(BEST_SCORE, 0);

        mBackground = new Texture("fundo.png");
        mGameOver = new Texture("game_over.png");
        mFlappyLogo = new Texture("flappy_bird_logo.png");

        mBirdShape = new Circle();

        mBird[0] = new Texture("passaro1.png");
        mBird[1] = new Texture("passaro2.png");
        mBird[2] = new Texture("passaro3.png");

        // INIT PIPES

        mBotPipeShape[0] = new Rectangle();
        mBotPipeShape[1] = new Rectangle();
        mBottomPipe[0] = new Texture("cano_baixo_maior.png");
        mBottomPipe[1] = new Texture("cano_baixo.png");

        mUpPipeShape[0] = new Rectangle();
        mUpPipeShape[1] = new Rectangle();
        mUpperPipe[0] = new Texture("cano_topo.png");
        mUpperPipe[1] = new Texture("cano_topo_maior.png");

        // INIT TEXTS

        mScoreText = new BitmapFont();
        mScoreText.setColor(Color.WHITE);
        mScoreText.getData().setScale(10);

        mRestartText = new BitmapFont();
        mRestartText.setColor(Color.YELLOW);
        mRestartText.getData().setScale(3);

        mBestScoreText = new BitmapFont();
        mBestScoreText.setColor(Color.RED);
        mBestScoreText.getData().setScale(3);

        // INIT SOUNDS

        mWingsSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        mCollisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        mScoringSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

        // INIT VIEWPORT

        mCamera = new OrthographicCamera();
        mCamera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);

        mViewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, mCamera);

    }

    private void initializeObjects() {
	    mIsCollidingDown = false;
	    mVolume = (float) 0.1;

        mDisplayWidth = (int) VIRTUAL_WIDTH;
        mDisplayHeight = (int) VIRTUAL_HEIGHT;

        xPositionPipe[0] = mDisplayWidth; // DEFINES PIPES[0] START X POSITION
        xPositionPipe[1] = (int) (mDisplayWidth + (mDisplayWidth * 0.5)+ (mBottomPipe[0].getWidth() * 0.5)); // DEFINES PIPES[1] START X POSITION

        mPipeXSpeed = 150;
        mPipeSpace[0] = 50;
        mPipeSpace[1] = 50;

        mBotPipeHeight[0] = (int) (mBottomPipe[0].getHeight() - mPipeSpace[0]); // UPPER PIPE[0] START HEIGHT
        mBotPipeHeight[1] = (int) (mBottomPipe[1].getHeight() - mPipeSpace[1]); // UPPER PIPE[1] START HEIGHT

        mUpperPipeHeight[0] = (int) (mUpperPipe[0].getHeight() - mPipeSpace[0]); // BOTTOM PIPE[0] START HEIGHT
        mUpperPipeHeight[1] = (int) (mUpperPipe[1].getHeight() - mPipeSpace[1]); // BOTTOM PIPE[1] START HEIGHT

        mBirdWidth = 100;
        mBirdHeight = 75;

        yInitPosition = (float) (mDisplayHeight*0.7);
        xInitPosition = (float) ((mDisplayWidth * 0.5) - (mBirdWidth * 0.5));
        mSpeedX = xInitPosition; // DEFINES BIRD START X POSITION
        mSpeedY = yInitPosition; // DEFINES BIRD START Y POSITION

        yUpPipe[0] = (mDisplayHeight - mUpperPipeHeight[0]);
        yUpPipe[1] = (mDisplayHeight - mUpperPipeHeight[1]);

        yBotPipe = 0;

        mBatch = new SpriteBatch();
        mShapeRenderer = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {
        mViewport.update(width, height);
    }

    @Override
	public void render () {

	    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

	    mBatch.setProjectionMatrix(mCamera.combined);

	    mBatch.begin();

        boolean clicked = Gdx.input.justTouched();
         if (mWingsAnim > 12) mWingsAnim = 0;


        drawTextures();
        if (!mIsGameStarted) {

            if (mIsColliding) resetGame();

            if (mIsGameStarted = clicked) mWingsSound.play(mVolume);


        }else{

            mIsColliding = controlCollision();
            if (!mIsColliding) {

                if (clicked) {
                    mWingsSound.play(mVolume);
                    mGravity = -15;
                    mSpeedX += 7;
                }

                updateSpeedValues();

            }else {

                mGravity = (mGravity < 0) ? 0 : (int) (mGravity +1.5);



                if (clicked) mIsGameStarted = false;
            }
        }

	    mBatch.end();
	}

    private void resetGame() {

        mPipeXSpeed = 150;
        mPipeSpace[0] = 50;
        mPipeSpace[1] = 50;

        mBirdXKnockBack = 0;
        mGravity = 0;
        mScore = 0;

        mBotPipeHeight[0] = (int) (mBottomPipe[0].getHeight() - mPipeSpace[0]); // UPPER PIPE RESET HEIGHT
        mBotPipeHeight[1] = (int) (mBottomPipe[1].getHeight() - mPipeSpace[1]); // UPPER PIPE RESET HEIGHT

        mUpperPipeHeight[0] = (int) (mUpperPipe[0].getHeight() - mPipeSpace[0]); // BOTTOM PIPE RESET HEIGHT
        mUpperPipeHeight[1] = (int) (mUpperPipe[1].getHeight() - mPipeSpace[1]); // BOTTOM PIPE RESET HEIGHT

        yUpPipe[0] = (mDisplayHeight - mUpperPipeHeight[0]);
        yUpPipe[1] = (mDisplayHeight - mUpperPipeHeight[1]);

        xPositionPipe[0] = mDisplayWidth; // DEFINES PIPE RESET X POSITION
        xPositionPipe[1] = (int) (mDisplayWidth + (mDisplayWidth * 0.5) + (mBottomPipe[0].getWidth() * 0.5)); // DEFINES PIPE RESET X POSITION

        mSpeedX = xInitPosition; // DEFINES BIRD RESET X POSITION
        mSpeedY = yInitPosition; // DEFINES BIRD RESET Y POSITION

        mIsColliding = false;
        mIsCollidingDown = false;
        drawTextures();
    }

    private boolean controlCollision() {

        mBotPipeShape[0].set(xPositionPipe[0], yBotPipe, mBottomPipe[0].getWidth(), mBotPipeHeight[0]);
        mBotPipeShape[1].set(xPositionPipe[1], yBotPipe, mBottomPipe[1].getWidth(), mBotPipeHeight[1]);

        mUpPipeShape[0].set(xPositionPipe[0], yUpPipe[0], mUpperPipe[0].getWidth(), mUpperPipeHeight[0]);
        mUpPipeShape[1].set(xPositionPipe[1], yUpPipe[1], mUpperPipe[1].getWidth(), mUpperPipeHeight[1]);

        float birdRadius = (float) (mBirdHeight*0.5);
        float birdX = mSpeedX + (float) (mBirdWidth * 0.5);
        float birdY = mSpeedY + birdRadius;

        mBirdShape.set(birdX, birdY, birdRadius);

        boolean collidingUp1 = Intersector.overlaps(mBirdShape, mUpPipeShape[0]);
        boolean collidingUp2 = Intersector.overlaps(mBirdShape, mUpPipeShape[1]);

        boolean collidingDown1 = Intersector.overlaps(mBirdShape, mBotPipeShape[0]);
        boolean collidingDown2 = Intersector.overlaps(mBirdShape, mBotPipeShape[1]);

        boolean collidingDown = collidingDown1 || collidingDown2;
        boolean collidingUp = collidingUp1 || collidingUp2;

        boolean birdFell = mSpeedY == 0;

        if (!mIsColliding) {
            if (collidingDown || collidingUp || birdFell)
                mCollisionSound.play(mVolume);

            mIsColliding = collidingDown || collidingUp || birdFell;
        }

        if (mSpeedY < (mBirdHeight * 0.5)) {

            mSpeedY = 0;
            mBirdXKnockBack = 0;

        }else{

            if (!collidingDown) mSpeedY -= mGravity;
            if (mIsColliding){
                if (!mIsCollidingDown) {
                    mBirdXKnockBack = (int)( -1 * (Gdx.graphics.getDeltaTime() * mPipeXSpeed));

                }else
                    mBirdXKnockBack = 0;
                mIsCollidingDown = collidingDown;
            }
        }
        return mIsColliding;
    }

    private void updateSpeedValues(){
        // UPDATE VALUES

        // UPDATE SCORE
        if (!mIsCrossedPipe[0]) {
            if (mSpeedX > xPositionPipe[0] ) {
                mScoringSound.play(mVolume);
                mScore++;

                if (mScore > mBestScore){
                    mBestScore = mScore;
                    myPreferences.putInteger(BEST_SCORE, mBestScore);
                }
                mIsCrossedPipe[0] = true;
            }
        }


        if (!mIsCrossedPipe[1]) {
            if (mSpeedX > xPositionPipe[1] ) {
                mScoringSound.play(mVolume);
                mScore++;

                if (mScore > mBestScore){
                    mBestScore = mScore;
                    myPreferences.putInteger(BEST_SCORE, mBestScore);
                }
                mIsCrossedPipe[1] = true;
            }
        }


        //PIPE MOVEMENT
        if (xPositionPipe[0] < (0-mBottomPipe[0].getWidth())){
            xPositionPipe[0] = (int) (mDisplayWidth + (mBottomPipe[0].getWidth() * 0.5) );//+ (mDisplayWidth * 0.2)

            int diff = myRandy();
            mBotPipeHeight[0] = (int) (mBottomPipe[0].getHeight() - diff - mPipeSpace[0]);
            mUpperPipeHeight[0] = (int) (mUpperPipe[0].getHeight() + diff - mPipeSpace[0]);

            yUpPipe[0] = (mDisplayHeight - mUpperPipeHeight[0]);

            // PIPE SPACE
            if (mIsCrossedPipe[0]) {
                if (mPipeSpace[0] >= 0) mPipeSpace[0] -= 0.5;
                mIsCrossedPipe[0] = false;
            }

        } else
            xPositionPipe[0] -= (Gdx.graphics.getDeltaTime()*mPipeXSpeed);


        if (xPositionPipe[1] < (0-mBottomPipe[1].getWidth())){
            xPositionPipe[1] = (int) (mDisplayWidth + (mBottomPipe[0].getWidth() * 0.5));//+ (mDisplayWidth * 0.2)
            mPipeXSpeed++;

            int diff = myRandy();
            mBotPipeHeight[1] = (int) (mBottomPipe[1].getHeight() - diff - mPipeSpace[1]);
            mUpperPipeHeight[1] = (int) (mUpperPipe[1].getHeight() + diff - mPipeSpace[1]);

            yUpPipe[1] = (mDisplayHeight - mUpperPipeHeight[1]);

            // PIPE SPACE
            if (mIsCrossedPipe[1]) {
                if (mPipeSpace[1] >= 0) mPipeSpace[1] -= 0.5;
                mIsCrossedPipe[1] = false;
            }

        } else
            xPositionPipe[1] -= (Gdx.graphics.getDeltaTime()*mPipeXSpeed);


        // BIRD SPEED
        mGravity += 1.5;

        if  (mSpeedX <= (mDisplayWidth * 0.1)) mSpeedX =  (int) (mDisplayWidth * 0.1);
        else if (mSpeedX >= (mDisplayWidth * 0.8)) mSpeedX =  (int) (mDisplayWidth * 0.8);
        else mSpeedX -= 0.5;

    }

	private void drawTextures() {
        // ################################   DRAWING BACKGROUND   ################################

        mBatch.draw(mBackground, 0, 0, mDisplayWidth, mDisplayHeight);

        // #################################   DRAWING PIPES   ####################################

        // BOTTOM PIPE
        mBatch.draw(mBottomPipe[0], xPositionPipe[0], yBotPipe, mBottomPipe[0].getWidth(), mBotPipeHeight[0]);
        mBatch.draw(mBottomPipe[1], xPositionPipe[1], yBotPipe, mBottomPipe[1].getWidth(), mBotPipeHeight[1]);

        //UPPER PIPE
        mBatch.draw(mUpperPipe[0], xPositionPipe[0], yUpPipe[0], mUpperPipe[0].getWidth(), mUpperPipeHeight[0]);
        mBatch.draw(mUpperPipe[1], xPositionPipe[1], yUpPipe[1], mUpperPipe[1].getWidth(), mUpperPipeHeight[1]);

        // ###################################   DRAWING BIRD   ###################################

        mSpeedX = mSpeedX + mBirdXKnockBack;
        mBatch.draw(mBird[calmWings(mWingsAnim++)], mSpeedX, mSpeedY, mBirdWidth, mBirdHeight);

        // ##################################   DRAWING TEXTS   ###################################
        if (mIsGameStarted) {

            mScoreText.draw(mBatch,
                    String.valueOf(mScore),
                    (float) ((mDisplayWidth * 0.5)-(mScoreText.getSpaceXadvance() * 0.5)),
                    (float) (mDisplayHeight * 0.98));


            if (mIsColliding){

                mBatch.draw(mGameOver,
                        (float) ((mDisplayWidth * 0.5)-(mGameOver.getWidth() * 0.5)),
                        (float) ((mDisplayHeight * 0.7)) );

                mBestScoreText.draw(mBatch,
                        "BEST SCORE : "+ mBestScore,
                        (float) ((mDisplayWidth * 0.5) - 180),
                        (float) ((mDisplayHeight * 0.685)) );

                mRestartText.draw(mBatch,
                        "Touch to restart",
                        (float) ((mDisplayWidth * 0.5) - 150 ),
                        (float) ((mDisplayHeight * 0.65)) );

            }

        }else {  // STARTING SCREEN ELSE

            mBatch.draw(mFlappyLogo,
                    (float) ((mDisplayWidth * 0.5)-(mDisplayWidth * 0.25)),
                    (float) ((mDisplayHeight * 0.8)),
                    (float) (mDisplayWidth * 0.5),
                    (float) (mDisplayHeight * 0.1));

            mRestartText.draw(mBatch,
                    "Touch to start",
                    (float) ((mDisplayWidth * 0.5) - 140 ),
                    (float) ((mDisplayHeight * 0.65)) );
        }
    }

	@Override
	public void dispose () {

    }

    /** ####################################   HELPERS   #####################################  **/

    private int calmWings(int counter){
        if (counter >4) return 0;
        else if (counter >8)return 1;
        else return 2;
    }

    private int myRandy(){
        Random randy = new Random();

        int maxHeightDiff = (mDisplayHeight/3) - 50;
        int i = randy.nextInt(maxHeightDiff);
        i = ((i % 2) == 0) ? i : i * (-1);
        return i;
    }

     /*
                mShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                mShapeRenderer.setColor(Color.BLUE);

                mShapeRenderer.rect(
                        (float) ((mDisplayWidth * 0.5) - (mRestartText.getSpaceXadvance() * 0.5)),//(mGameOver.getWidth() * 0.4));
                        (float) ((mDisplayHeight * 0.78)  - mBestScoreText.getLineHeight() ),
                        mBestScoreText.getRegions().get(0).;
                        mBestScoreText.getRegion().getRegionY());

                mShapeRenderer.end();

*/
}
