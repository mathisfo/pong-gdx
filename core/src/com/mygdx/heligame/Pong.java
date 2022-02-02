package com.mygdx.heligame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

public class Pong extends Game{
	public static final int WIDTH=800,HEIGHT=480;
	public SpriteBatch batch;
	public TweenManager tweenManager;
	public AssetManager assetManager;
	public Texture ballImage;

	@Override
	public void create () {
		setupTweenManager();
		assetManager = new AssetManager();
		batch = new SpriteBatch();
		this.setScreen(new PongBoard(this));
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	private void setupTweenManager() {
		tweenManager = new TweenManager();
		Tween.registerAccessor(Camera.class, new CameraAccessor());
		Tween.registerAccessor(Table.class, new TableAccessor());
		Tween.registerAccessor(Paddle.class, new PaddleAccessor());
	}

}