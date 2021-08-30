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

    public static final String PREFERENCES = "FLAPPYBIRD";
    public static final String BEST_SCORE = "BEST_SCORE";

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
    private int mBotPipeHeight, mUpperPipeHeight;
    private int mBirdWidth, mBirdHeight;

    private boolean mIsCrossedPipe;

    private int xPositionPipe, mPipeXSpeed, yUpPipe;

    private float yInitPosition, xInitPosition, mPipeSpace;
    private float mSpeedX, mSpeedY;

    private SpriteBatch mBatch;
    private ShapeRenderer mShapeRenderer;

    private Texture mBackground, mGameOver, mFlappyLogo;

    private int mBirdXKnockBack = 0;
    private Circle mBirdShape;
    private Texture[] mBird;

    private Rectangle mBotPipeShape, mUpPipeShape;
    private Texture mBottomPipe, mUpperPipe;

    private BitmapFont mScoreText, mRestartText, mBestScoreText;


    private Sound mWingsSound, mCollisionSound, mScoringSound;

	@Override
	public void create () {
        initializeTextures();
        initializeObjects();
	}

    private void initializeTextures(){

	    myPreferences = Gdx.app.getPreferences(PREFERENCES);
	    mBestScore = myPreferences.getInteger(BEST_SCORE, 0);

        mBackground = new Texture("fundo.png");
        mGameOver = new Texture("game_over.png");
        mFlappyLogo = new Texture("flappy_bird_logo.png");

        mBirdShape = new Circle();
        mBird = new Texture[3];
        mBird[0] = new Texture("passaro1.png");
        mBird[1] = new Texture("passaro2.png");
        mBird[2] = new Texture("passaro3.png");

        mBotPipeShape = new Rectangle();
        mBottomPipe = new Texture("cano_baixo_maior.png");

        mUpPipeShape = new Rectangle();
        mUpperPipe = new Texture("cano_topo.png");

        mScoreText = new BitmapFont();
        mScoreText.setColor(Color.WHITE);
        mScoreText.getData().setScale(10);

        mRestartText = new BitmapFont();
        mRestartText.setColor(Color.YELLOW);
        mRestartText.getData().setScale(3);

        mBestScoreText = new BitmapFont();
        mBestScoreText.setColor(Color.RED);
        mBestScoreText.getData().setScale(3);

        mWingsSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        mCollisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        mScoringSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

        mCamera = new OrthographicCamera();
        mCamera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);

        mViewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, mCamera);

    }

    private void initializeObjects() {
	    mIsCollidingDown = false;
	    mVolume = (float) 0.1;

        mDisplayWidth = (int) VIRTUAL_WIDTH;
        mDisplayHeight = (int) VIRTUAL_HEIGHT;

        xPositionPipe = mDisplayWidth; // DEFINES PIPES START X POSITION
        mPipeXSpeed = 150;
        mPipeSpace = 50;

        mBotPipeHeight = (int) (mBottomPipe.getHeight() - mPipeSpace); // UPPER PIPE START HEIGHT
        mUpperPipeHeight = (int) (mUpperPipe.getHeight() - mPipeSpace); // BOTTOM PIPE START HEIGHT

        mBirdWidth = 100;
        mBirdHeight = 75;

        yInitPosition = (float) (mDisplayHeight*0.7);
        xInitPosition = (float) ((mDisplayWidth * 0.5) - (mBirdWidth * 0.5));
        mSpeedX = xInitPosition; // DEFINES BIRD START X POSITION
        mSpeedY = yInitPosition; // DEFINES BIRD START Y POSITION

        yUpPipe = (mDisplayHeight - mUpperPipeHeight);

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
        mPipeSpace = 50;

        mBirdXKnockBack = 0;
        mGravity = 0;
        mScore = 0;

        mBotPipeHeight = (int) (mBottomPipe.getHeight() - mPipeSpace); // UPPER PIPE RESET HEIGHT
        mUpperPipeHeight = (int) (mUpperPipe.getHeight() - mPipeSpace); // BOTTOM PIPE RESET HEIGHT
        yUpPipe = (mDisplayHeight - mUpperPipeHeight);

        xPositionPipe = mDisplayWidth; // DEFINES PIPE RESET X POSITION

        mSpeedX = xInitPosition; // DEFINES BIRD RESET X POSITION
        mSpeedY = yInitPosition; // DEFINES BIRD RESET Y POSITION

        mIsColliding = false;
        mIsCollidingDown = false;
        drawTextures();
    }

    private boolean controlCollision() {

        mBotPipeShape.set(xPositionPipe, 0, mBottomPipe.getWidth(), mBotPipeHeight);
        mUpPipeShape.set(xPositionPipe, yUpPipe, mUpperPipe.getWidth(), mUpperPipeHeight);

        float birdRadius = (float) (mBirdHeight*0.5);
        float birdX = mSpeedX + (float) (mBirdWidth * 0.5);
        float birdY = mSpeedY + birdRadius;

        mBirdShape.set(birdX, birdY, birdRadius);

        boolean collidingUp = Intersector.overlaps(mBirdShape, mUpPipeShape);
        boolean collidingDown = Intersector.overlaps(mBirdShape, mBotPipeShape);

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
        if (!mIsCrossedPipe) {
            if (mSpeedX > xPositionPipe ) {
                mScoringSound.play(mVolume);
                mScore++;

                if (mScore > mBestScore){
                    mBestScore = mScore;
                    myPreferences.putInteger(BEST_SCORE, mBestScore);
                }

                mIsCrossedPipe = true;
            }
        }

        //PIPE MOVEMENT
        if (xPositionPipe < (0-mBottomPipe.getWidth())){
            xPositionPipe = mDisplayWidth;
            mPipeXSpeed++;

            int diff = myRandy();
            mBotPipeHeight = (int) (mBottomPipe.getHeight() - diff - mPipeSpace);
            mUpperPipeHeight = (int) (mUpperPipe.getHeight() + diff - mPipeSpace);

            yUpPipe = (mDisplayHeight - mUpperPipeHeight);

            // PIPE SPACE
            if (mIsCrossedPipe) {
                if (mPipeSpace >= 0) mPipeSpace -= 0.5;
                mIsCrossedPipe = false;
            }

        } else
            xPositionPipe -= (Gdx.graphics.getDeltaTime()*mPipeXSpeed);

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
        mBatch.draw(mBottomPipe, xPositionPipe, 0, mBottomPipe.getWidth(), mBotPipeHeight);

        //UPPER PIPE
        mBatch.draw(mUpperPipe, xPositionPipe, yUpPipe, mUpperPipe.getWidth(), mUpperPipeHeight);

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
