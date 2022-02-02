package com.mygdx.heligame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.equations.Back;
import aurelienribon.tweenengine.equations.Sine;

public class PongBoard implements Screen {
    final Pong game;
    private final int HEIGHT = Pong.HEIGHT;
    private final int WIDTH = Pong.WIDTH;
    Paddle paddle1;
    Paddle paddle2;
    Ball ball;
    private OrthographicCamera camera;
    private Array<Paddle> paddleList;

    public PongBoard(final Pong gam) {
        this.game = gam;
        game.ballImage = makeRectImage(12, 12, Color.WHITE);
        setupPaddles();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
    }

    private Texture makeRectImage(int width, int height, Color color) {
        Pixmap ballPixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        ballPixmap.setColor(color);
        ballPixmap.fill();
        return new Texture(ballPixmap);
    }

    public class MainInputProcessor implements InputProcessor{
        private final Vector3 tmpV = new Vector3();

        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

            setPaddleLocationForTouchDown(camera.unproject(tmpV.set(screenX, screenY, 0)));
            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            setPaddleLocationForTouchDragged(camera.unproject(tmpV.set(screenX, screenY, 0)));
            return true;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }

        public void setPaddleLocationForTouchDown(Vector3 pos) {
            if (pos.x < (WIDTH / 2)) {
                paddleMoveTween(paddle1, pos);

            } else if (pos.x > (WIDTH / 2)) {
                paddleMoveTween(paddle2, pos);
            }
        }

        private void setPaddleLocationForTouchDragged (Vector3 pos) {
            if (pos.x < (WIDTH / 2)) {
                if (!paddle1.getTweening()) {
                    paddle1.setCenterY(pos.y);
                }

            } else if (pos.x > (WIDTH / 2)) {
                if (!paddle2.getTweening()) {
                    paddle2.setCenterY(pos.y);
                }
            }
        }
    }

    private void paddleMoveTween(final Paddle paddle, Vector3 pos) {

        TweenCallback tweenCallback = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                paddle.setTweening(false);
            }
        };

        paddle.setTweening(true);
        Tween.to(paddle, PaddleAccessor.POSITION_Y, .15f)
                .ease(Sine.INOUT)
                .target(pos.y)
                .setCallback(tweenCallback)
                .start(game.tweenManager);
    }

    public void setupPaddles() {
        paddle1 = new Paddle("paddle1", 50);
        paddle2 = new Paddle("paddle2", 750);
        paddleList = new Array<Paddle>();
        paddleList.add(paddle1);
        paddleList.add(paddle2);
    }

    @Override
    public void render(float delta) {
        game.tweenManager.update(delta);
        camera.update();
        updateBallMovement(delta);
        checkPaddleOutOfBounds();
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batchDraw();
    }

    private void updateBallMovement(float deltaTime) {
        if (!(ball == null)) {
            ball.moveX(deltaTime);
            checkForPaddleCollision();
            checkForBallOutOfBounds();
            ball.moveY(deltaTime);
            checkForWallCollision();
            checkForCeilingCollision();
        }
    }

    private void checkForCeilingCollision() {
        if (ball.getTop() > HEIGHT) {
            ball.reverseDirectionY();
            ball.setTop(HEIGHT);
        } else if (ball.getBottom() < 0) {
            ball.reverseDirectionY();
            ball.setBottom(0f);
        }
    }

    private void checkForPaddleCollision() {
        for (Paddle hitPaddle : paddleList) {
            if (Intersector.overlaps(hitPaddle, ball)) {
                ball.xVel *= -1;
                if (ball.xVel > 0) {ball.xVel += 20;} else {ball.xVel -= 20;}

                if (hitPaddle.name.equals("paddle1")) {
                    ball.setPosition((hitPaddle.x + hitPaddle.width), ball.y);
                } else if (hitPaddle.name.equals("paddle2")) {
                    ball.setPosition((hitPaddle.x - ball.width), ball.y);
                }
            }
        }
    }

    private void checkForBallOutOfBounds() {
        if (ball.x < 0) {
            ball.resetPosition();
            ball.reverseDirectionX();
            ball.reverseDirectionY();
            ball.resetVelocityX(1);
        } else if (ball.getRight() > WIDTH) {
            ball.resetPosition();
            ball.reverseDirectionX();
            ball.reverseDirectionY();
            ball.resetVelocityX(-1);
        }
    }

    private void checkForWallCollision() {
        if (ball.getTop() > HEIGHT) {
            ball.reverseDirectionY();
            ball.setTop(HEIGHT);
        } else if (ball.getY() < 0) {
            ball.reverseDirectionY();
            ball.setBottom(0f);
        }
    }

    private void checkPaddleOutOfBounds() {
        for (Paddle paddle : paddleList) {
            if (paddle.getTop() > HEIGHT) {
                paddle.setTop(HEIGHT);
            } else if (paddle.y < 0) {
                paddle.setY(0);
            }
        }
    }

    private void batchDraw() {
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(paddle1.paddleImage, paddle1.x, paddle1.y);
        game.batch.draw(paddle2.paddleImage, paddle2.x, paddle2.y);
        if (!(ball == null)) {
            game.batch.draw(ball.ballImage, ball.x, ball.y);
        }
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {}

    @Override
    public void resume() {}

    @Override
    public void pause() {}

    @Override
    public void hide() {}

    @Override
    public void show() {
        beginIntroTween();

    }

    @Override
    public void dispose() {
        paddle1.dispose();
        paddle2.dispose();
    }

    private void beginIntroTween() {
        TweenCallback callBack = new TweenCallback() {
            @Override
            public void onEvent(int type, BaseTween<?> source) {
                ball = new Ball(game);
                Gdx.input.setInputProcessor(new MainInputProcessor());
            }
        };
        camera.position.x += 800;
        Tween.to(camera, CameraAccessor.POSITION_X, 0f)
                .targetRelative(-800)
                .ease(Back.OUT)
                .setCallback(callBack)
                .start(game.tweenManager);
    }

}
